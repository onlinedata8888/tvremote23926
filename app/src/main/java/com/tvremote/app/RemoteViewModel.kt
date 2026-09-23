package com.tvremote.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tvremote.app.protocol.crypto.CertificateManager
import com.tvremote.app.protocol.discovery.DiscoveredTv
import com.tvremote.app.protocol.discovery.TvDiscovery
import com.tvremote.app.protocol.pairing.AndroidTvPairingClient
import com.tvremote.app.protocol.remote.AndroidTvRemoteClient
import com.tvremote.app.proto.remote.RemoteKeyCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

enum class ConnectionState { DISCONNECTED, SCANNING, CONNECTING, NEEDS_PAIRING_CODE, PAIRING, CONNECTED, ERROR }

/** The 7 app shortcuts from the design. `link` is the App Link URI sent via
 *  RemoteAppLinkLaunchRequest — the real mechanism the protocol supports for
 *  launching a specific app (there's no raw "launch by package name" message
 *  in this protocol, only app-link resolution). */
data class AppShortcut(val id: String, val label: String, val link: String)

val ALL_APP_SHORTCUTS = listOf(
    AppShortcut("youtube", "YouTube", "https://www.youtube.com/tv"),
    AppShortcut("netflix", "Netflix", "https://www.netflix.com/title"),
    AppShortcut("prime", "Prime Video", "https://app.primevideo.com"),
    AppShortcut("hotstar", "Hotstar", "https://www.hotstar.com"),
    AppShortcut("zee5", "ZEE5", "https://www.zee5.com"),
    AppShortcut("sonyliv", "SonyLIV", "https://www.sonyliv.com"),
    AppShortcut("jiocinema", "JioCinema", "https://www.jiocinema.com"),
)

data class RemoteUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val discoveredTvs: List<DiscoveredTv> = emptyList(),
    val selectedTvName: String = "New TV",
    val pickerOpen: Boolean = false,
    val online: Boolean = false,
    val powerOn: Boolean = true,
    val muted: Boolean = false,
    val volumePercent: Int = 60,
    val errorMessage: String? = null,
    // App shortcuts row: which apps are currently pinned (in order), and
    // which were removed (available again from the "+" add-app sheet).
    val pinnedApps: List<AppShortcut> = ALL_APP_SHORTCUTS,
    val removedApps: List<AppShortcut> = emptyList(),
    val addAppSheetOpen: Boolean = false,
    // Sliding "more options" page — replaces the touchpad area when open.
    val moreOptionsOpen: Boolean = false,
    val captionsOn: Boolean = false,
    val keyboardOpen: Boolean = false,
    val keyboardText: String = ""
)

class RemoteViewModel(app: Application) : AndroidViewModel(app) {

    private val certManager = CertificateManager(app)
    private val remote = AndroidTvRemoteClient(certManager)
    private val pairingClient = AndroidTvPairingClient(certManager)
    private val discovery = TvDiscovery(app)

    private var pendingHost: String? = null

    // The pairing handshake (below) suspends right after the TV starts showing
    // its code, waiting on this to be completed with whatever the person types
    // into the dialog. This is what lets us keep the SAME pairing socket open
    // between "TV shows code" and "person typed it in" instead of guessing the
    // code before the TV has even generated one.
    private var codeDeferred: CompletableDeferred<String>? = null
    private var pairingJob: Job? = null

    // Android's volume stream has 15 discrete steps. Real remotes always move one
    // step at a time with a real VOLUME_UP/DOWN key press, which is what makes the
    // TV draw its native on-screen volume bar — so we mirror that here too.
    private var volumeStep: Int = 9 // ~60%, matches the default volumePercent below

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    fun togglePicker() {
        _uiState.value = _uiState.value.copy(pickerOpen = !_uiState.value.pickerOpen)
        if (_uiState.value.pickerOpen) scanForTvs()
    }

    fun closePicker() {
        _uiState.value = _uiState.value.copy(pickerOpen = false)
    }

    private fun scanForTvs() {
        _uiState.value = _uiState.value.copy(connectionState = ConnectionState.SCANNING)
        viewModelScope.launch {
            val found = discovery.scan()
            _uiState.value = _uiState.value.copy(
                discoveredTvs = found,
                connectionState = if (remote.isConnected) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
            )
        }
    }

    fun connectTo(tv: DiscoveredTv) {
        pendingHost = tv.host
        _uiState.value = _uiState.value.copy(
            connectionState = ConnectionState.CONNECTING,
            selectedTvName = tv.name,
            pickerOpen = false
        )
        viewModelScope.launch { attemptConnect(tv.host) }
    }

    private suspend fun attemptConnect(host: String) {
        val result = remote.connect(host)
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(
                connectionState = ConnectionState.CONNECTED,
                online = true,
                errorMessage = null
            )
        } else {
            // Most likely cause: this TV has never seen our certificate before.
            startPairing(host)
        }
    }

    /**
     * Opens the pairing socket and runs the handshake ourselves. The TV only
     * puts the code on screen once we reach the PairingConfiguration step
     * inside [AndroidTvPairingClient.pair] — so we must already be mid-handshake,
     * with that same socket held open, before we ever ask the person to type
     * anything. That's why the code is read via a suspending [CompletableDeferred]
     * instead of being collected from the dialog up front: the dialog is only
     * shown once the callback below actually runs, i.e. once the TV is already
     * displaying a fresh code.
     */
    private fun startPairing(host: String) {
        pendingHost = host
        pairingJob?.cancel()
        pairingJob = viewModelScope.launch {
            val result = pairingClient.pair(host) {
                val deferred = CompletableDeferred<String>()
                codeDeferred = deferred
                _uiState.value = _uiState.value.copy(
                    connectionState = ConnectionState.NEEDS_PAIRING_CODE,
                    errorMessage = null
                )
                deferred.await()
            }
            codeDeferred = null
            when (result) {
                is AndroidTvPairingClient.PairingResult.Success -> attemptConnect(host)
                is AndroidTvPairingClient.PairingResult.WrongCode -> _uiState.value = _uiState.value.copy(
                    connectionState = ConnectionState.ERROR,
                    errorMessage = "Code galat tha ya TV pe expire ho gaya. \"Try Again\" dabao — TV nayi screen dikhayega."
                )
                is AndroidTvPairingClient.PairingResult.Error -> _uiState.value = _uiState.value.copy(
                    connectionState = ConnectionState.ERROR,
                    errorMessage = result.message
                )
            }
        }
    }

    /** Called once the person has read the code that's currently on the TV screen and typed it in. */
    fun submitPairingCode(code: String) {
        _uiState.value = _uiState.value.copy(connectionState = ConnectionState.PAIRING)
        codeDeferred?.complete(code)
    }

    /**
     * After any pairing/connection failure: start over from the top (not just
     * re-open the pairing socket), so a plain connect error gets a real retry
     * too, not just a wrong-code retry.
     */
    fun retryPairing() {
        val host = pendingHost ?: return
        _uiState.value = _uiState.value.copy(connectionState = ConnectionState.CONNECTING, errorMessage = null)
        viewModelScope.launch { attemptConnect(host) }
    }

    fun cancelPairing() {
        pairingJob?.cancel()
        codeDeferred = null
        _uiState.value = _uiState.value.copy(connectionState = ConnectionState.DISCONNECTED, errorMessage = null)
    }

    fun togglePower() = act {
        remote.sendKey(RemoteKeyCode.KEYCODE_POWER)
        _uiState.value = _uiState.value.copy(powerOn = !_uiState.value.powerOn)
    }

    fun toggleMute() = act {
        remote.sendKey(RemoteKeyCode.KEYCODE_VOLUME_MUTE)
        _uiState.value = _uiState.value.copy(muted = !_uiState.value.muted)
    }

    /** Slider drag: move to the nearest step for the dragged percent, one real key per step. */
    fun setVolume(percent: Int) = act {
        val targetStep = ((percent.coerceIn(0, 100) * 15) + 50) / 100
        moveVolumeToStep(targetStep)
    }

    /** +/- buttons and the volume rocker: exactly one real step per tap. */
    fun stepVolume(deltaPercent: Int) = act {
        moveVolumeToStep(volumeStep + if (deltaPercent > 0) 1 else -1)
    }

    private suspend fun moveVolumeToStep(target: Int) {
        val clamped = target.coerceIn(0, 15)
        val diff = clamped - volumeStep
        if (diff > 0) repeat(diff) { remote.sendKey(RemoteKeyCode.KEYCODE_VOLUME_UP) }
        if (diff < 0) repeat(-diff) { remote.sendKey(RemoteKeyCode.KEYCODE_VOLUME_DOWN) }
        volumeStep = clamped
        _uiState.value = _uiState.value.copy(volumePercent = volumeStep * 100 / 15)
    }

    fun back() = act { remote.sendKey(RemoteKeyCode.KEYCODE_BACK) }
    fun home() = act { remote.sendKey(RemoteKeyCode.KEYCODE_HOME) }
    fun recentApps() = act { remote.sendKey(RemoteKeyCode.KEYCODE_APP_SWITCH) }
    fun assistant() = act { remote.sendKey(RemoteKeyCode.KEYCODE_VOICE_ASSIST) }
    fun menu() = act { remote.sendKey(RemoteKeyCode.KEYCODE_MENU) }
    fun openSettings() = act { remote.sendKey(RemoteKeyCode.KEYCODE_SETTINGS) }

    fun dpadTap() = act { remote.sendKey(RemoteKeyCode.KEYCODE_DPAD_CENTER) }
    fun dpadDirection(dx: Int, dy: Int) = act {
        val key = when {
            kotlin.math.abs(dx) > kotlin.math.abs(dy) && dx > 0 -> RemoteKeyCode.KEYCODE_DPAD_RIGHT
            kotlin.math.abs(dx) > kotlin.math.abs(dy) && dx < 0 -> RemoteKeyCode.KEYCODE_DPAD_LEFT
            dy > 0 -> RemoteKeyCode.KEYCODE_DPAD_DOWN
            dy < 0 -> RemoteKeyCode.KEYCODE_DPAD_UP
            else -> null
        }
        key?.let { remote.sendKey(it) }
    }

    fun launchApp(app: AppShortcut) = act { remote.launchAppLink(app.link) }

    // --- Number pad / channel rocker / colored keys / media transport ---
    // (the sliding "more options" page — MoreOptionsPanel)

    private val digitKeys = mapOf(
        0 to RemoteKeyCode.KEYCODE_0, 1 to RemoteKeyCode.KEYCODE_1, 2 to RemoteKeyCode.KEYCODE_2,
        3 to RemoteKeyCode.KEYCODE_3, 4 to RemoteKeyCode.KEYCODE_4, 5 to RemoteKeyCode.KEYCODE_5,
        6 to RemoteKeyCode.KEYCODE_6, 7 to RemoteKeyCode.KEYCODE_7, 8 to RemoteKeyCode.KEYCODE_8,
        9 to RemoteKeyCode.KEYCODE_9
    )

    fun pressDigit(n: Int) = act { digitKeys[n]?.let { remote.sendKey(it) } }
    fun channelUp() = act { remote.sendKey(RemoteKeyCode.KEYCODE_CHANNEL_UP) }
    fun channelDown() = act { remote.sendKey(RemoteKeyCode.KEYCODE_CHANNEL_DOWN) }
    fun pressRed() = act { remote.sendKey(RemoteKeyCode.KEYCODE_PROG_RED) }
    fun pressGreen() = act { remote.sendKey(RemoteKeyCode.KEYCODE_PROG_GREEN) }
    fun pressYellow() = act { remote.sendKey(RemoteKeyCode.KEYCODE_PROG_YELLOW) }
    fun pressBlue() = act { remote.sendKey(RemoteKeyCode.KEYCODE_PROG_BLUE) }
    fun mediaPrevious() = act { remote.sendKey(RemoteKeyCode.KEYCODE_MEDIA_PREVIOUS) }
    fun mediaRewind() = act { remote.sendKey(RemoteKeyCode.KEYCODE_MEDIA_REWIND) }
    fun mediaPlayPause() = act { remote.sendKey(RemoteKeyCode.KEYCODE_MEDIA_PLAY_PAUSE) }
    fun mediaFastForward() = act { remote.sendKey(RemoteKeyCode.KEYCODE_MEDIA_FAST_FORWARD) }
    fun mediaNext() = act { remote.sendKey(RemoteKeyCode.KEYCODE_MEDIA_NEXT) }
    fun tvInput() = act { remote.sendKey(RemoteKeyCode.KEYCODE_TV_INPUT) }
    /** "TV" button in the more-options page — switches to the tuner. Uses
     *  the same underlying key as [tvInput] since the protocol has no
     *  separate "switch to tuner" vs "open input list" key code. */
    fun switchToTv() = act { remote.sendKey(RemoteKeyCode.KEYCODE_TV_INPUT) }
    fun toggleCaptions() = act {
        remote.sendKey(RemoteKeyCode.KEYCODE_CAPTIONS)
        _uiState.value = _uiState.value.copy(captionsOn = !_uiState.value.captionsOn)
    }

    fun toggleMoreOptions() {
        _uiState.value = _uiState.value.copy(moreOptionsOpen = !_uiState.value.moreOptionsOpen)
    }

    // --- App shortcuts row: hold-to-wiggle → drag delete via long-press, add-back sheet ---

    fun removeApp(app: AppShortcut) {
        val s = _uiState.value
        _uiState.value = s.copy(
            pinnedApps = s.pinnedApps.filterNot { it.id == app.id },
            removedApps = s.removedApps + app
        )
    }

    fun addBackApp(app: AppShortcut) {
        val s = _uiState.value
        _uiState.value = s.copy(
            pinnedApps = s.pinnedApps + app,
            removedApps = s.removedApps.filterNot { it.id == app.id }
        )
    }

    fun toggleAddAppSheet() {
        _uiState.value = _uiState.value.copy(addAppSheetOpen = !_uiState.value.addAppSheetOpen)
    }

    // --- On-screen keyboard ---
    // NOTE: the real Android TV Remote v2 protocol used here has no public,
    // reliably-documented "inject arbitrary text" message (that's an
    // ADB-only capability: `input text "..."`). So this keeps the same
    // local-only demo behaviour noted in the design doc — it doesn't
    // actually send characters to the TV yet.

    fun toggleKeyboard() {
        _uiState.value = _uiState.value.copy(keyboardOpen = !_uiState.value.keyboardOpen)
    }

    fun keyboardType(char: String) {
        _uiState.value = _uiState.value.copy(keyboardText = _uiState.value.keyboardText + char)
    }

    fun keyboardBackspace() {
        val t = _uiState.value.keyboardText
        if (t.isNotEmpty()) _uiState.value = _uiState.value.copy(keyboardText = t.dropLast(1))
    }

    // --- Hold-to-repeat: used by the volume rocker and the touchpad's edge
    // scroll wheels. Fires [action] immediately, then every [intervalMs]
    // while the coroutine stays alive; cancel the returned Job on pointer-up. ---

    /** Used by the touchpad and its edge scroll wheels: holding a direction
     *  repeats that DPAD key every ~180ms until the finger lifts. */
    fun repeatDpad(dx: Int, dy: Int): Job = startRepeating { dpadDirection(dx, dy) }

    /** Used by the touchpad's right-edge scroll wheel and the volume rocker. */
    fun repeatVolume(delta: Int): Job = startRepeating { stepVolume(delta) }

    fun startRepeating(intervalMs: Long = 180L, action: () -> Unit): Job = viewModelScope.launch {
        action()
        while (isActive) {
            delay(intervalMs)
            action()
        }
    }

    /** Runs a remote action only if connected; silently no-ops otherwise (mirrors a real remote). */
    private fun act(block: suspend () -> Unit) {
        if (!remote.isConnected) return
        viewModelScope.launch { block() }
    }

    override fun onCleared() {
        super.onCleared()
        remote.close()
    }
}
