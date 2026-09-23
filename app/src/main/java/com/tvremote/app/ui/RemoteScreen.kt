package com.tvremote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tvremote.app.ConnectionState
import com.tvremote.app.RemoteViewModel
import com.tvremote.app.ui.components.AddAppSheet
import com.tvremote.app.ui.components.AppsRow
import com.tvremote.app.ui.components.BottomControls
import com.tvremote.app.ui.components.KeyboardOverlay
import com.tvremote.app.ui.components.MoreOptionsPanel
import com.tvremote.app.ui.components.NavBar
import com.tvremote.app.ui.components.PairingDialog
import com.tvremote.app.ui.components.PairingErrorDialog
import com.tvremote.app.ui.components.TopBar
import com.tvremote.app.ui.components.Touchpad
import com.tvremote.app.ui.components.VolumeSlider
import com.tvremote.app.ui.theme.RemoteColors

@Composable
fun RemoteScreen(viewModel: RemoteViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RemoteColors.ScreenBg1, RemoteColors.ScreenBg2)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopBar(
                powerOn = state.powerOn,
                onPowerClick = viewModel::togglePower,
                online = state.online,
                tvName = state.selectedTvName,
                pickerOpen = state.pickerOpen,
                discoveredTvs = state.discoveredTvs,
                onTogglePicker = viewModel::togglePicker,
                onSelectTv = viewModel::connectTo,
                onMenuClick = viewModel::menu
            )

            if (state.connectionState == ConnectionState.DISCONNECTED || state.connectionState == ConnectionState.SCANNING) {
                Text(
                    text = if (state.connectionState == ConnectionState.SCANNING)
                        "Scanning your Wi-Fi for a TV\u2026"
                    else
                        "Tap the TV name above to find your Android TV",
                    color = RemoteColors.MutedText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            VolumeSlider(
                percent = state.volumePercent,
                onChange = viewModel::setVolume,
                onStep = viewModel::stepVolume
            )

            Divider()

            AppsRow(
                apps = state.pinnedApps,
                onLaunch = viewModel::launchApp,
                onRemove = viewModel::removeApp,
                onAddTap = viewModel::toggleAddAppSheet
            )

            if (state.addAppSheetOpen) {
                AddAppSheet(
                    removedApps = state.removedApps,
                    onAdd = viewModel::addBackApp,
                    onClose = viewModel::toggleAddAppSheet
                )
            }

            Divider()

            // The sliding "pane": nav-bar + touchpad, or the more-options page.
            if (state.moreOptionsOpen) {
                MoreOptionsPanel(
                    captionsOn = state.captionsOn,
                    onChannelUp = viewModel::channelUp,
                    onChannelDown = viewModel::channelDown,
                    onDigit = viewModel::pressDigit,
                    onTv = viewModel::switchToTv,
                    onInput = viewModel::tvInput,
                    onCaptions = viewModel::toggleCaptions,
                    onRed = viewModel::pressRed,
                    onGreen = viewModel::pressGreen,
                    onYellow = viewModel::pressYellow,
                    onBlue = viewModel::pressBlue,
                    onPrevious = viewModel::mediaPrevious,
                    onRewind = viewModel::mediaRewind,
                    onPlayPause = viewModel::mediaPlayPause,
                    onFastForward = viewModel::mediaFastForward,
                    onNext = viewModel::mediaNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NavBar(
                        onLeft = { viewModel.dpadDirection(-1, 0) },
                        onRight = { viewModel.dpadDirection(1, 0) },
                        onUp = { viewModel.dpadDirection(0, -1) },
                        onDown = { viewModel.dpadDirection(0, 1) },
                        onOk = viewModel::dpadTap,
                        onPlayPause = viewModel::mediaPlayPause,
                        modifier = Modifier.height(120.dp)
                    )
                    Touchpad(
                        onTap = viewModel::dpadTap,
                        onRepeatDirection = viewModel::repeatDpad,
                        onRepeatVolume = viewModel::repeatVolume,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
            }

            Divider()

            BottomControls(
                muted = state.muted,
                moreOptionsOpen = state.moreOptionsOpen,
                onRepeatVolumeUp = { viewModel.repeatVolume(1) },
                onRepeatVolumeDown = { viewModel.repeatVolume(-1) },
                onBack = viewModel::back,
                onHome = viewModel::home,
                onMore = viewModel::toggleMoreOptions,
                onMute = viewModel::toggleMute,
                onMic = viewModel::assistant,
                onRecents = viewModel::recentApps,
                onKeyboard = viewModel::toggleKeyboard,
                modifier = Modifier.height(100.dp)
            )
        }

        if (state.keyboardOpen) {
            KeyboardOverlay(
                text = state.keyboardText,
                onType = viewModel::keyboardType,
                onBackspace = viewModel::keyboardBackspace,
                onClose = viewModel::toggleKeyboard
            )
        }
    }

    if (state.connectionState == ConnectionState.NEEDS_PAIRING_CODE ||
        state.connectionState == ConnectionState.PAIRING
    ) {
        PairingDialog(
            isSubmitting = state.connectionState == ConnectionState.PAIRING,
            errorMessage = null,
            onSubmit = viewModel::submitPairingCode,
            onCancel = viewModel::cancelPairing
        )
    } else {
        val errorMessage = state.errorMessage
        if (state.connectionState == ConnectionState.ERROR && errorMessage != null) {
            PairingErrorDialog(
                message = errorMessage,
                onRetry = viewModel::retryPairing,
                onCancel = viewModel::cancelPairing
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Brush.horizontalGradient(listOf(RemoteColors.ScreenBg1, RemoteColors.Divider, RemoteColors.ScreenBg1)))
    )
}
