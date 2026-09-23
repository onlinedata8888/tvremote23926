package com.tvremote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tvremote.app.ui.theme.RemoteColors

/**
 * The sliding "more options" page (`.pane-page`) — replaces the touchpad
 * area when the 3-dot button is tapped. Matches the HTML section order:
 * modes-row, cast-row, num-pad (with the channel pill), fn-row, color-row,
 * media-row.
 *
 * P.Mode / S.Mode / Bluetooth / Wi-Fi / Cast / Mirroring are shown for
 * visual completeness but aren't wired to a real action — the real Android
 * TV Remote protocol this app speaks has no message for opening arbitrary
 * system settings screens (that needs ADB's `am start`), so they're left
 * inert rather than faked. See README.
 */
@Composable
fun MoreOptionsPanel(
    captionsOn: Boolean,
    onChannelUp: () -> Unit,
    onChannelDown: () -> Unit,
    onDigit: (Int) -> Unit,
    onTv: () -> Unit,
    onInput: () -> Unit,
    onCaptions: () -> Unit,
    onRed: () -> Unit,
    onGreen: () -> Unit,
    onYellow: () -> Unit,
    onBlue: () -> Unit,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    onFastForward: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Picture mode / Sound mode / Bluetooth / Wi-Fi
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CastPill(label = "P.Mode", modifier = Modifier.weight(1f), onClick = {})
            CastPill(label = "S.Mode", modifier = Modifier.weight(1f), onClick = {})
            CastPill(label = "Bluetooth", icon = Icons.Filled.Bluetooth, modifier = Modifier.weight(1f), onClick = {})
            CastPill(label = "Wi-Fi", icon = Icons.Filled.Wifi, modifier = Modifier.weight(1f), onClick = {})
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CastPill(label = "Cast", icon = Icons.Filled.Cast, modifier = Modifier.weight(1f), onClick = {})
            CastPill(label = "Mirroring", icon = Icons.Filled.ScreenShare, modifier = Modifier.weight(1f), onClick = {})
        }

        // Number pad + channel pill
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(168.dp)
                    .background(RemoteColors.BtnBg, RoundedCornerShape(16.dp))
            ) {
                ChBtn(onClick = onChannelUp, up = true, modifier = Modifier.weight(1f).fillMaxWidth())
                Text(
                    "CH", color = RemoteColors.Icon, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                ChBtn(onClick = onChannelDown, up = false, modifier = Modifier.weight(1f).fillMaxWidth())
            }
            Box(
                modifier = Modifier
                    .weight(3f)
                    .height(168.dp)
                    .background(RemoteColors.PadBg, RoundedCornerShape(16.dp))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items((1..9).toList()) { n ->
                        NumButton(text = "$n", onClick = { onDigit(n) }, modifier = Modifier.fillMaxWidth().height(50.dp))
                    }
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                        NumButton(text = "0", onClick = { onDigit(0) }, modifier = Modifier.fillMaxWidth().height(50.dp))
                    }
                }
            }
        }

        // TV / INPUT / CC TXT / 123
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FnBtn(text = "TV", modifier = Modifier.weight(1f), onClick = onTv)
            FnBtn(text = "INPUT", modifier = Modifier.weight(1f), onClick = onInput)
            FnBtn(
                text = "CC TXT",
                modifier = Modifier.weight(1f),
                highlighted = captionsOn,
                onClick = onCaptions
            )
            FnBtn(text = "123", modifier = Modifier.weight(1f), onClick = {})
        }

        // Colored remote keys
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColorPill(RemoteColors.ColorRed, Modifier.weight(1f), onRed)
            ColorPill(RemoteColors.ColorGreen, Modifier.weight(1f), onGreen)
            ColorPill(RemoteColors.ColorYellow, Modifier.weight(1f), onYellow)
            ColorPill(RemoteColors.ColorBlue, Modifier.weight(1f), onBlue)
        }

        // Media transport, pinned toward the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaBtn(Icons.Filled.SkipPrevious, "Previous", Modifier.weight(1f), onPrevious)
            MediaBtn(Icons.Filled.FastRewind, "Rewind", Modifier.weight(1f), onRewind)
            MediaBtn(Icons.Filled.PlayArrow, "Play/Pause", Modifier.weight(1f), onPlayPause)
            MediaBtn(Icons.Filled.FastForward, "Forward", Modifier.weight(1f), onFastForward)
            MediaBtn(Icons.Filled.SkipNext, "Next", Modifier.weight(1f), onNext)
        }
    }
}

@Composable
private fun CastPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .background(RemoteColors.BtnBg, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = label, tint = RemoteColors.Icon, modifier = Modifier.height(16.dp))
        }
        Text(label, color = RemoteColors.Icon, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun ChBtn(onClick: () -> Unit, up: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(
            if (up) Icons.Filled.SkipPrevious else Icons.Filled.SkipNext,
            contentDescription = if (up) "Channel up" else "Channel down",
            tint = RemoteColors.IconDim,
            modifier = Modifier.height(20.dp).width(20.dp)
                .then(Modifier)
        )
    }
}

@Composable
private fun NumButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, color = RemoteColors.Icon, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FnBtn(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(
                if (highlighted) RemoteColors.SliderFill1.copy(alpha = 0.18f) else RemoteColors.BtnBg,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp,
            color = if (highlighted) RemoteColors.SliderFill1 else RemoteColors.Icon
        )
    }
}

@Composable
private fun ColorPill(color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(24.dp)
            .background(color, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
    )
}

@Composable
private fun MediaBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(RemoteColors.BtnBg, androidx.compose.foundation.shape.CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = RemoteColors.Icon, modifier = Modifier.height(18.dp))
    }
}
