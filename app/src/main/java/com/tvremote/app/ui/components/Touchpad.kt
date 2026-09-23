package com.tvremote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tvremote.app.ui.theme.RemoteColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope

/**
 * The touchpad, matching the HTML's `.touchpad` + 3 edge "scroll wheels":
 * a dark rounded surface with centered "touch pad" hint text, a metallic
 * vertical wheel on the left edge (up/down), a red vertical wheel on the
 * right edge (volume), and a metallic horizontal wheel along the bottom
 * (left/right) — each one a jog-dial that repeats its key while held.
 */
@Composable
fun Touchpad(
    onTap: () -> Unit,
    onRepeatDirection: (dx: Int, dy: Int) -> Job,
    onRepeatVolume: (delta: Int) -> Job,
    modifier: Modifier = Modifier,
    thresholdPx: Float = 55f
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RemoteColors.PadBg, RoundedCornerShape(22.dp))
                .pointerInput(Unit) {
                    var repeatJob: Job? = null
                    detectDragGestures(
                        onDragEnd = { repeatJob?.cancel(); repeatJob = null },
                        onDragCancel = { repeatJob?.cancel(); repeatJob = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (repeatJob != null) return@detectDragGestures
                            val dx = dragAmount.x
                            val dy = dragAmount.y
                            if (kotlin.math.abs(dx) > thresholdPx || kotlin.math.abs(dy) > thresholdPx) {
                                val stepDx = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) dx.toInt() else 0
                                val stepDy = if (kotlin.math.abs(dy) >= kotlin.math.abs(dx)) dy.toInt() else 0
                                repeatJob = onRepeatDirection(stepDx, stepDy)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onTap() })
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "touch pad", color = RemoteColors.MutedText, fontSize = 11.sp, letterSpacing = 0.3.sp)
        }

        // Left edge: metallic vertical wheel -> up/down repeat
        EdgeWheel(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(20.dp)
                .fillMaxHeight(),
            vertical = true,
            gradient = Brush.linearGradient(listOf(RemoteColors.WheelTopStart, RemoteColors.WheelTopEnd)),
            shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp),
            onHoldNegative = { onRepeatDirection(0, -1) },
            onHoldPositive = { onRepeatDirection(0, 1) }
        )

        // Right edge: red metallic vertical wheel -> volume
        EdgeWheel(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(20.dp)
                .fillMaxHeight(),
            vertical = true,
            gradient = Brush.linearGradient(listOf(RemoteColors.WheelRedStart, RemoteColors.WheelRedEnd)),
            shape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp),
            onHoldNegative = { onRepeatVolume(1) },
            onHoldPositive = { onRepeatVolume(-1) }
        )

        // Bottom edge: metallic horizontal wheel -> left/right repeat
        EdgeWheel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(26.dp)
                .padding(horizontal = 20.dp),
            vertical = false,
            gradient = Brush.linearGradient(listOf(RemoteColors.WheelTopStart, RemoteColors.WheelTopEnd)),
            shape = RoundedCornerShape(0.dp),
            onHoldNegative = { onRepeatDirection(-1, 0) },
            onHoldPositive = { onRepeatDirection(1, 0) }
        )
    }
}

/**
 * A thin drag strip styled like a real jog-wheel. Dragging/holding toward
 * the "negative" end (up for vertical, left for horizontal) repeats
 * [onHoldNegative]; the other end repeats [onHoldPositive].
 */
@Composable
private fun EdgeWheel(
    modifier: Modifier = Modifier,
    vertical: Boolean,
    gradient: Brush,
    shape: RoundedCornerShape,
    onHoldNegative: () -> Job,
    onHoldPositive: () -> Job
) {
    Box(
        modifier = modifier
            .background(gradient, shape)
            .pointerInput(Unit) {
                coroutineScope {
                    var job: Job? = null
                    detectDragGestures(
                        onDragEnd = { job?.cancel(); job = null },
                        onDragCancel = { job?.cancel(); job = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (job != null) return@detectDragGestures
                            val delta = if (vertical) dragAmount.y else dragAmount.x
                            if (kotlin.math.abs(delta) > 6f) {
                                job = if (delta < 0) onHoldNegative() else onHoldPositive()
                            }
                        }
                    )
                }
            }
    )
}
