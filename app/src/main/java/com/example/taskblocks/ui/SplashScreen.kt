package com.example.taskblocks.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskblocks.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

private val SplashBackground = Color(0xFFF9F5EF)
private val LogoPurple = Color(0xFF9B7BDB)
private val LogoGreen = Color(0xFF4CAF50)
private val LogoBlue = Color(0xFF4A90E2)
private val LogoOrange = Color(0xFFFF9800)
private val LogoPink = Color(0xFFFF6FA8)
private val LogoYellow = Color(0xFFFFC928)
private val TextColor = Color(0xFF3D3229)

@Composable
fun SplashScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val block1Scale = remember { Animatable(0f) }
    val block1Alpha = remember { Animatable(0f) }
    val block2Scale = remember { Animatable(0f) }
    val block2Alpha = remember { Animatable(0f) }
    val block3Scale = remember { Animatable(0f) }
    val block3Alpha = remember { Animatable(0f) }
    val block4Scale = remember { Animatable(0f) }
    val block4Alpha = remember { Animatable(0f) }
    val block5Scale = remember { Animatable(0f) }
    val block5Alpha = remember { Animatable(0f) }
    val block6Scale = remember { Animatable(0f) }
    val block6Alpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch {
                block1Scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                block1Alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                delay(80)
                block2Scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                block2Alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                delay(160)
                block3Scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                block3Alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                delay(240)
                block4Scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                block4Alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                delay(320)
                block5Scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                block5Alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                delay(400)
                block6Scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                block6Alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                delay(520)
                titleAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            }
            launch {
                delay(670)
                taglineAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            }
        }
        delay(1100)
        onFinish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 方块从中间拼成 T：先出现中间的竖杆，再补齐左右横杆
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 顶部横向 3 个方块，组成 T 的横杆
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .graphicsLayer(
                                scaleX = block2Scale.value,
                                scaleY = block2Scale.value,
                                alpha = block2Alpha.value
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(LogoOrange)
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .graphicsLayer(
                                scaleX = block1Scale.value,
                                scaleY = block1Scale.value,
                                alpha = block1Alpha.value
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(LogoGreen)
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .graphicsLayer(
                                scaleX = block3Scale.value,
                                scaleY = block3Scale.value,
                                alpha = block3Alpha.value
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(LogoBlue)
                    )
                }
                // 中间竖向的方块，组成 T 的竖杆
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左右占位用的空白，保证竖杆在中间
                    Box(modifier = Modifier.size(48.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .graphicsLayer(
                                scaleX = block4Scale.value,
                                scaleY = block4Scale.value,
                                alpha = block4Alpha.value
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(LogoPurple)
                    )
                    Box(modifier = Modifier.size(48.dp))
                }
                // 竖线下面再增加两个不同颜色的方块
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .graphicsLayer(
                                scaleX = block5Scale.value,
                                scaleY = block5Scale.value,
                                alpha = block5Alpha.value
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(LogoPink)
                    )
                    Box(modifier = Modifier.size(48.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .graphicsLayer(
                                scaleX = block6Scale.value,
                                scaleY = block6Scale.value,
                                alpha = block6Alpha.value
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(LogoYellow)
                    )
                    Box(modifier = Modifier.size(48.dp))
                }
            }

            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier
                    .padding(top = 24.dp)
                    .graphicsLayer(alpha = titleAlpha.value),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = TextColor
            )
            Text(
                text = stringResource(R.string.app_tagline),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .graphicsLayer(alpha = taglineAlpha.value),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp
                ),
                color = TextColor.copy(alpha = 0.7f)
            )
        }
    }
}
