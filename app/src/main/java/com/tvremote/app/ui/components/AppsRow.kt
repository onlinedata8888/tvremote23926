package com.tvremote.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tvremote.app.AppShortcut
import com.tvremote.app.ui.theme.RemoteColors

/**
 * App shortcuts row — matches the HTML's `.app-icon`: a 70×29 rounded-rect
 * chip (not a circle), one per app, each with its real brand color. Tap =
 * launch (App Link). Long-press = remove — the design's "hold → wiggle →
 * X-badge" flow, simplified to a direct long-press since full drag-reorder
 * is out of scope. Removed apps return via the "+" tile's add-back sheet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsRow(
    apps: List<AppShortcut>,
    onLaunch: (AppShortcut) -> Unit,
    onRemove: (AppShortcut) -> Unit,
    onAddTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        apps.forEach { app ->
            AppChip(
                brush = brushFor(app.id),
                onClick = { onLaunch(app) },
                onLongClick = { onRemove(app) }
            ) {
                AppGlyph(app.id)
            }
        }
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(29.dp)
                .border(1.4.dp, Color(0x38FFFFFF), RoundedCornerShape(8.dp))
                .combinedClickable(onClick = onAddTap, onLongClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add app", tint = Color(0xFF9A9EA6), modifier = Modifier.height(11.dp))
        }
    }
}

@Composable
private fun brushFor(id: String): Brush = when (id) {
    "youtube" -> Brush.linearGradient(listOf(RemoteColors.YoutubeRed, RemoteColors.YoutubeRed))
    "netflix" -> Brush.linearGradient(listOf(RemoteColors.NetflixBlack, RemoteColors.NetflixBlack))
    "prime" -> Brush.linearGradient(listOf(RemoteColors.PrimeBlue, RemoteColors.PrimeBlue))
    "hotstar" -> Brush.linearGradient(listOf(RemoteColors.HotstarBlue, RemoteColors.HotstarBlue))
    "zee5" -> Brush.linearGradient(listOf(RemoteColors.Zee5Purple, RemoteColors.Zee5Purple))
    "sonyliv" -> Brush.linearGradient(listOf(RemoteColors.SonyLivStart, RemoteColors.SonyLivEnd))
    "jiocinema" -> Brush.linearGradient(listOf(RemoteColors.JioRed, RemoteColors.JioRed))
    else -> Brush.linearGradient(listOf(RemoteColors.CustomAppBg, RemoteColors.CustomAppBg))
}

@Composable
private fun AppGlyph(id: String) {
    when (id) {
        "youtube", "prime" -> Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.height(12.dp))
        "netflix" -> Text("N", color = RemoteColors.NetflixRed, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        "hotstar" -> Text("Hotstar", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        "zee5" -> Text("ZEE5", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        "sonyliv" -> Text("SonyLIV", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        "jiocinema" -> Text("Jio", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        else -> Text(id.take(1).uppercase(), color = Color(0xFFC7CAD0), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppChip(
    brush: Brush,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(29.dp)
            .background(brush, RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** The overlay sheet opened by the "+" tile: shows removed apps to add back. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddAppSheet(
    removedApps: List<AppShortcut>,
    onAdd: (AppShortcut) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0xFF1A1C22), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add app", color = RemoteColors.IconDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(Color(0x14FFFFFF), CircleShape)
                        .combinedClickable(onClick = onClose, onLongClick = {})
                        .padding(6.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = RemoteColors.IconDim, modifier = Modifier.height(14.dp))
                }
            }
            if (removedApps.isEmpty()) {
                Text(
                    "Sab apps already row me hain.",
                    color = RemoteColors.MutedText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    removedApps.forEach { app ->
                        AppChip(brush = brushFor(app.id), onClick = { onAdd(app) }, onLongClick = {}) {
                            AppGlyph(app.id)
                        }
                    }
                }
            }
        }
    }
}
