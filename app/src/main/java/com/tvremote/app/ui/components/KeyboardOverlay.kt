package com.tvremote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
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

private val NUM_ROW = "1234567890".map { it.toString() }
private val ROW1 = "qwertyuiop".map { it.toString() }
private val ROW2 = "asdfghjkl".map { it.toString() }
private val ROW3 = "zxcvbnm".map { it.toString() }

/**
 * On-screen keyboard overlay — matches the HTML's `.kb-overlay`: close
 * button, a hint line, a search field box, then a full QWERTY grid. Local
 * demo only for now (see the note in RemoteViewModel.kt: no documented
 * "inject text" message in this protocol).
 */
@Composable
fun KeyboardOverlay(
    text: String,
    onType: (String) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE1060709))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .background(Color(0x14FFFFFF), CircleShape)
                    .clickable(onClick = onClose)
                    .padding(7.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = RemoteColors.IconDim, modifier = Modifier.height(15.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = RemoteColors.MutedText, modifier = Modifier.height(15.dp))
                Text(
                    "Search expects TV keyboard to be opened already.",
                    color = RemoteColors.MutedText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RemoteColors.BtnBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = RemoteColors.MutedText, modifier = Modifier.height(16.dp))
                Text(
                    text.ifEmpty { "Start typing\u2026" },
                    color = if (text.isEmpty()) RemoteColors.MutedText else RemoteColors.IconDim,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeyRow(NUM_ROW, onType)
                KeyRow(ROW1, onType)
                KeyRow(ROW2, onType, indent = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Key(icon = Icons.Filled.KeyboardArrowUp, desc = "Shift", onClick = {}, modifier = Modifier.weight(1.5f))
                    ROW3.forEach { k -> Key(label = k, onClick = { onType(k) }, modifier = Modifier.weight(1f)) }
                    Key(icon = Icons.Filled.Backspace, desc = "Backspace", onClick = onBackspace, modifier = Modifier.weight(1.5f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Key(label = "!#1", onClick = { onType("!") }, modifier = Modifier.weight(1f))
                    Key(label = "English(India)", onClick = {}, modifier = Modifier.weight(2f))
                    Key(label = "space", onClick = { onType(" ") }, modifier = Modifier.weight(2.5f))
                    Key(label = ".", onClick = { onType(".") }, modifier = Modifier.weight(1f))
                    Key(
                        label = "Done", onClick = onClose, modifier = Modifier.weight(1.4f),
                        background = RemoteColors.AccentGreen, textColor = Color(0xFF08110D)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyRow(keys: List<String>, onType: (String) -> Unit, indent: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = if (indent) 14.dp else 0.dp, end = if (indent) 14.dp else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { k -> Key(label = k, onClick = { onType(k) }, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun Key(
    label: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    desc: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = RemoteColors.BtnBg,
    textColor: Color = RemoteColors.Icon
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(background, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = desc, tint = textColor, modifier = Modifier.height(16.dp))
        } else if (label != null) {
            Text(label, color = textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}
