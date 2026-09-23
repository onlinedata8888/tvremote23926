# TV Remote — Android app (v10.8)

A real Android app (Kotlin + Jetpack Compose) that controls an **Android TV
/ Google TV** device using Google's own **Android TV Remote Service v2
protocol** — the same protocol the official Google TV app and Google Home
app use. No ADB, no developer mode, no root — same Wi-Fi is all it needs.

This build merges the original `tvRemote19` protocol implementation with
the **v10.8 design** and the full **BUTTON_FUNCTIONS.md** button list.

## How it works
1. Your phone finds the TV over Wi-Fi via mDNS (`_androidtvremote2._tcp`).
2. First connection: a mutual-TLS handshake on port 6467, the TV shows a
   6-character code on screen, you type it into the app once. The app's
   self-signed certificate is then remembered by the TV forever.
3. Every reconnect after that goes straight to port 6466 — a persistent
   session where every button press is a few protobuf bytes on an already
   open socket. No process spawned per key, which is what makes it instant.

## What's implemented for real (sends an actual command to the TV)
- Power, Home, Back, Menu, Settings, Recent apps, Assistant/Mic
- D-pad (touchpad drag/tap) + **hold-to-repeat** while a direction is held
- Volume: slider drag, +/- buttons, side rocker (all hold-to-repeat) and mute
- **3 edge scroll wheels** on the touchpad (left = up/down repeat, right =
  volume repeat, bottom = left/right repeat) — jog-dial style, matching the
  design's "hold = continuous repeat" spec
- App shortcuts (YouTube/Netflix/Prime/Hotstar/ZEE5/SonyLIV/JioCinema) —
  launched via **App Link** (`RemoteAppLinkLaunchRequest`), the real
  mechanism this protocol supports for launching a specific app. Long-press
  an icon to remove it from the row; the "+" tile brings it back.
- Sliding "more options" page (the 3-dot button): channel up/down, number
  pad 0-9, 4 colored remote keys, media transport (prev/rewind/play-pause/
  forward/next), captions toggle, TV-input switch

## What's UI-only / demo (matches what BUTTON_FUNCTIONS.md itself marks as "not real yet")
- **On-screen keyboard**: types locally in the app only. The real Android TV
  Remote v2 protocol has no publicly documented "inject arbitrary text"
  message — that's an ADB-only capability (`input text "..."`). Wiring this
  up for real would mean adding ADB as a second, optional connection path.
- **App reorder (drag to rearrange)**: not implemented — long-press
  removes an icon instead of triggering drag; full drag-and-drop reordering
  in Compose was out of scope for this pass.

## Not possible with this protocol at all (left out rather than faked)
Bluetooth settings, Wi-Fi settings, Cast, screen mirroring, and picture/
sound-mode shortcuts from the button-functions doc all rely on Android
**system intents** (`am start -a ...`), which only ADB can send. The real
Google TV Remote protocol only supports key-code presses and app-link
launches — there is no message for launching arbitrary settings screens.
These buttons were intentionally left out rather than wired to something
that silently does nothing.

## Design fidelity note
This build follows the v10.8 HTML mockup's **structure and exact colors**
closely: dark theme (`:root`'s default palette, not the light override),
rounded-rect app chips (not circles), the segmented nav-bar (left/up-down/
right arrows in one tile + separate OK/Play-Pause tiles) instead of a
classic circular D-pad, the 3 edge scroll-wheels on the touchpad, the
vertical +/- volume pill, and the more-options page's exact section order
(modes-row → cast-row → number pad+channel pill → fn-row → color-row →
media-row). It is a faithful Compose recreation, not a pixel-identical
render — exact corner radii, shadows, and spacing were approximated with
standard Compose values rather than matching every CSS `clamp()` value.
Even Google's own protocol has **no free-moving mouse pointer message** —
only key events. The touchpad's "cursor" is DPAD focus navigation, not a
true pixel-following pointer.

## Build it
Open in Android Studio, let Gradle sync (pulls in the protobuf compiler
and Bouncy Castle automatically), run on a phone on the same Wi-Fi as the
TV. Or push to GitHub — the included `.github/workflows/build.yml` builds
a debug APK automatically; download it from the Actions tab's Artifacts.

⚠️ **I could not compile/run this build myself** — this sandbox has no
Android SDK or access to Google's Maven repo, so this was written and
reviewed carefully but not machine-verified. If GitHub Actions reports an
error, paste the failing step's log back and it can be fixed quickly —
that's normal for a first build of a project this size, not a sign
something is fundamentally wrong.

## First-time pairing
1. Tap the TV name at the top → app scans Wi-Fi (few seconds).
2. Tap your TV → a 6-character code appears on the TV screen.
3. Type it into the app's dialog → done. Every future connect skips this.

## Project structure
```
app/src/main/proto/
├── pairing.proto          # pairing handshake schema (port 6467)
└── remotemessage.proto    # live key-injection schema (port 6466) — extended
                            #   with channel/number/color/media/caption keys

app/src/main/java/com/tvremote/app/
├── RemoteViewModel.kt      # all state + actions, incl. hold-repeat helper
├── protocol/
│   ├── crypto/
│   │   ├── CertificateManager.kt   # self-signed client cert (Bouncy Castle)
│   │   └── TlsSupport.kt           # mutual-TLS + pairing secret hash
│   ├── pairing/AndroidTvPairingClient.kt
│   ├── remote/AndroidTvRemoteClient.kt   # persistent session + key/app-link sends
│   └── discovery/TvDiscovery.kt          # mDNS discovery
└── ui/
    ├── RemoteScreen.kt               # top-level layout/wiring
    ├── theme/Color.kt, Theme.kt      # v10.8 light theme palette
    └── components/
        ├── TopBar.kt, VolumeSlider.kt, CircleIconButton.kt   (unchanged)
        ├── AppsRow.kt                # 7 apps, remove + add-back sheet
        ├── MoreOptionsPanel.kt       # NEW — channel/numbers/colors/media
        ├── Touchpad.kt               # NEW — 3 edge scroll wheels + hold-repeat
        ├── BottomControls.kt         # hold-repeat volume rocker
        ├── KeyboardOverlay.kt        # NEW — local-only demo keyboard
        └── PairingDialog.kt          (unchanged)
```

## Push to your own GitHub repo
```bash
cd TVRemote
git init
git add .
git commit -m "v10.8 remote"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```
