package com.tvremote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tvremote.app.ui.theme.RemoteColors

/**
 * The v10.8 design's navigation row above the touchpad — NOT a classic
 * circular D-pad. It's a horizontal bar: a single unified dark tile split
 * into 3 equal columns (Left arrow | Up/Down stacked | Right arrow, with
 * thin divider lines between segments — matches `.nav-arrows`), next to a
 * second block of 2 stacked standalone tiles (OK, Play/Pause — matches
 * `.nav-mid.nav-action`).
 */
@Composable
fun NavBar(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onOk: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Unified arrows block: 3 equal columns inside ONE rounded tile
        Row(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .background(RemoteColors.PadBg, RoundedCornerShape(16.dp))
        ) {
            SegmentButton(onClick = onLeft, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Left", tint = RemoteColors.Icon)
            }
            DividerLine(vertical = true)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                SegmentButton(onClick = onUp, modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Up", tint = RemoteColors.Icon)
                }
                DividerLine(vertical = false)
                SegmentButton(onClick = onDown, modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Down", tint = RemoteColors.Icon)
                }
            }
            DividerLine(vertical = true)
            SegmentButton(onClick = onRight, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Right", tint = RemoteColors.Icon)
            }
        }

        // OK / Play-Pause: two separate standalone tiles, stacked
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NavTile(onClick = onOk, modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text("OK", color = RemoteColors.Icon, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.6.sp)
            }
            NavTile(onClick = onPlayPause, modifier = Modifier.weight(1f).fillMaxWidth()) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play/Pause", tint = RemoteColors.Icon)
            }
        }
    }
}

@Composable
private fun SegmentButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun NavTile(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .background(RemoteColors.PadBg, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun DividerLine(vertical: Boolean) {
    Box(
        modifier = if (vertical) Modifier.width(1.dp).fillMaxHeight().background(RemoteColors.PadBorder)
                    else Modifier.height(1.dp).fillMaxWidth().background(RemoteColors.PadBorder)
    )
}
