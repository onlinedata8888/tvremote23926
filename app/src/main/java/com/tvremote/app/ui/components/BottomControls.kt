package com.tvremote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tvremote.app.ui.theme.RemoteColors
import kotlinx.coroutines.Job

@Composable
fun BottomControls(
    muted: Boolean,
    moreOptionsOpen: Boolean,
    onRepeatVolumeUp: () -> Job,
    onRepeatVolumeDown: () -> Job,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onMore: () -> Unit,
    onMute: () -> Unit,
    onMic: () -> Unit,
    onRecents: () -> Unit,
    onKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Vertical +/- volume pill
        Column(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .background(RemoteColors.BtnBg, RoundedCornerShape(25.dp))
        ) {
            HoldRepeatIcon(Icons.Filled.Add, "Volume up", Modifier.weight(1f), onRepeatVolumeUp)
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 8.dp).background(Color(0x0FFFFFFF)))
            HoldRepeatIcon(Icons.Filled.Remove, "Volume down", Modifier.weight(1f), onRepeatVolumeDown)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircleIconButton(
                    icon = Icons.Filled.ArrowBack, contentDescription = "Back",
                    size = 46.dp,
                    width = 90.dp,
                    shape = RoundedCornerShape(23.dp),
                    onClick = onBack
                )
                CircleIconButton(Icons.Filled.Home, "Home", size = 46.dp, onClick = onHome)
                CircleIconButton(
                    icon = Icons.Filled.MoreHoriz,
                    contentDescription = "More options",
                    size = 46.dp,
                    background = if (moreOptionsOpen) RemoteColors.SliderFill1.copy(alpha = 0.18f) else RemoteColors.BtnBg,
                    tint = if (moreOptionsOpen) RemoteColors.SliderFill1 else RemoteColors.Icon,
                    onClick = onMore
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircleIconButton(
                    icon = Icons.Filled.VolumeOff,
                    contentDescription = "Mute",
                    size = 46.dp,
                    tint = if (muted) RemoteColors.AccentRed else RemoteColors.Icon,
                    onClick = onMute
                )
                CircleIconButton(Icons.Filled.Mic, "Microphone", size = 46.dp, onClick = onMic)
                CircleIconButton(Icons.Filled.Apps, "Recent apps", size = 46.dp, onClick = onRecents)
                CircleIconButton(Icons.Filled.Keyboard, "Keyboard", size = 46.dp, onClick = onKeyboard)
            }
        }
    }
}

@Composable
private fun HoldRepeatIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    modifier: Modifier,
    onRepeat: () -> Job
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val job = onRepeat()
                        try {
                            tryAwaitRelease()
                        } finally {
                            job.cancel()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = RemoteColors.IconDim, modifier = Modifier.height(16.dp))
    }
}
