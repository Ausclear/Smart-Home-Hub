package com.smithmk.smarthomehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// ── Colours ───────────────────────────────────────────────────────────────────
val BgColour    = Color(0xFF0A0E14)
val CardTop     = Color(0xFF0D1825)
val CardBottom  = Color(0xFF070B12)
val Gold        = Color(0xFFC4A96B)
val Amber       = Color(0xFFFFB84D)
val SolarYellow = Color(0xFFFFEB3B)
val WeatherBlue = Color(0xFF2196F3)
val AlarmGreen  = Color(0xFF4CAF50)
val BlindPurple = Color(0xFF9C27B0)
val TextPrimary = Color(0xFFFFFFFF)
val TextDim     = Color(0x55FFFFFF)
val TextLabel   = Color(0x88FFFFFF)
val TopHighlight= Color(0x20FFFFFF)
val TileBorder  = Color(0x15FFFFFF)
val Inactive    = Color(0x44FFFFFF)

val LabelStyle  = TextStyle(fontSize = 9.sp,  fontWeight = FontWeight.Medium, letterSpacing = 1.8.sp)
val ValueStyle  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.W100)
val StatusStyle = TextStyle(fontSize = 9.sp,  fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp)

// ── Glow ──────────────────────────────────────────────────────────────────────
enum class Glow(val colour: Color) {
    None(Color.Transparent),
    Lights(Color(0xBBC4A96B)),
    Security(Color(0xBB4CAF50)),
    Blinds(Color(0xBB9C27B0)),
    Energy(Color(0xBBFFEB3B)),
    Weather(Color(0xBB2196F3)),
    Climate(Color(0xBB00BCD4)),
    Media(Color(0xBBE91E63)),
}

// ── Tile data ─────────────────────────────────────────────────────────────────
data class TileData(
    val label: String, val value: String, val unit: String,
    val status: String, val statusColour: Color,
    val isActive: Boolean, val glow: Glow, val icon: String,
)

val TILES = listOf(
    TileData("LIGHTS",   "4",   "/12", "ACTIVE",   Amber,    true,  Glow.Lights,   "💡"),
    TileData("SECURITY", "—",   "",    "DISARMED", Inactive, false, Glow.Security, "🔒"),
    TileData("SOLAR",    "3.2", "kW",  "🔋 84%",   SolarYellow, true, Glow.Energy, "☀️"),
    TileData("WEATHER",  "24",  "°C",  "SUNNY",    WeatherBlue, true, Glow.Weather,"🌤"),
    TileData("BLINDS",   "0",   "/6",  "CLOSED",   Inactive, false, Glow.Blinds,  "🪟"),
    TileData("CLIMATE",  "21",  "°C",  "OFF",      Inactive, false, Glow.Climate, "🌡"),
    TileData("ROOMS",    "8",   "",    "BY ROOM",  Inactive, false, Glow.None,    "🏠"),
    TileData("MEDIA",    "—",   "",    "STOPPED",  Inactive, false, Glow.Media,   "🎵"),
)

// ── Activity ──────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HomeScreen() }
    }
}

// ── Home screen ───────────────────────────────────────────────────────────────
@Composable
fun HomeScreen() {
    val time = remember { SimpleDateFormat("HH:mm", Locale.UK).format(Date()) }
    val date = remember { SimpleDateFormat("EEE d MMM", Locale.UK).format(Date()) }
    var selectedIndex by remember { mutableStateOf(0) }
    val selected = TILES[selectedIndex]
    val listState = rememberLazyListState()

    // Scroll carousel to keep selected centred
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(
            index = maxOf(0, selectedIndex - 2),
            scrollOffset = 0
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColour)
            .drawBehind { drawGrid() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp, bottom = 16.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SMITHMK HOME", style = LabelStyle, color = Gold, letterSpacing = 3.sp)
                    Text(date.uppercase(), style = LabelStyle, color = TextDim)
                }
                Text(time, fontSize = 30.sp, fontWeight = FontWeight.W100, color = TextPrimary)
            }

            Spacer(Modifier.weight(1f))

            // ── Expanded selected tile ─────────────────────────────────────
            val expandedGlowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
                initialValue = 0.5f,
                targetValue = if (selected.isActive) 1f else 0.5f,
                animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "glowPulse"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(220.dp)
            ) {
                Tile(
                    modifier = Modifier.fillMaxSize(),
                    isActive = selected.isActive,
                    glow = selected.glow,
                    glowAlphaOverride = expandedGlowAlpha
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(selected.label, style = LabelStyle, color = TextLabel)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selected.icon, fontSize = 42.sp)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(selected.value, style = ValueStyle, color = TextPrimary)
                                    if (selected.unit.isNotEmpty()) {
                                        Text(
                                            " ${selected.unit}",
                                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W300, color = Color(0x99FFFFFF)),
                                            modifier = Modifier.padding(bottom = 5.dp)
                                        )
                                    }
                                }
                                Text(selected.status, style = StatusStyle, color = selected.statusColour)
                            }
                        }
                        // Active indicator bar
                        if (selected.isActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, selected.glow.colour, Color.Transparent)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Carousel ──────────────────────────────────────────────────
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(TILES) { index, tile ->
                    val isSelected = selectedIndex == index
                    val tileSize by animateDpAsState(
                        targetValue = if (isSelected) 80.dp else 60.dp,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                        label = "size"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.4f,
                        animationSpec = tween(200),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp) // fixed outer box keeps row stable
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Tile(
                            modifier = Modifier
                                .size(tileSize)
                                .graphicsLayer { this.alpha = alpha },
                            isActive = tile.isActive && isSelected,
                            glow = if (isSelected) tile.glow else Glow.None,
                            onClick = { selectedIndex = index }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(tile.icon, fontSize = if (isSelected) 22.sp else 16.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    tile.label,
                                    style = LabelStyle,
                                    color = if (isSelected) Gold else TextLabel,
                                    fontSize = if (isSelected) 8.sp else 7.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Tile component ────────────────────────────────────────────────────────────
@Composable
fun Tile(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    glow: Glow = Glow.None,
    glowAlphaOverride: Float? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "press"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "tile")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isActive && glow != Glow.None) 0.9f else 0.4f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val effectiveGlowAlpha = glowAlphaOverride ?: glowAlpha
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale; scaleY = pressScale
                shadowElevation = if (pressed) 0f else if (isActive) 28f else 8f
                this.shape = shape; clip = true
            }
            .drawBehind {
                if (glow != Glow.None) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            asFrameworkPaint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.TRANSPARENT
                                setShadowLayer(
                                    52f, 0f, 6f,
                                    glow.colour.copy(alpha = effectiveGlowAlpha).toArgb()
                                )
                            }
                        }
                        canvas.drawRoundRect(0f, 0f, size.width, size.height, 14.dp.toPx(), 14.dp.toPx(), paint)
                    }
                }
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(CardTop, CardBottom),
                    Offset.Zero,
                    Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .drawWithContent {
                drawContent()
                // top edge highlight
                drawRect(
                    Brush.verticalGradient(listOf(TopHighlight, Color.Transparent), 0f, size.height * 0.28f)
                )
                // gold shimmer sweep
                val sx = size.width * shimmer
                drawRect(
                    Brush.linearGradient(
                        listOf(Color.Transparent, Gold.copy(alpha = 0.06f), Color.Transparent),
                        Offset(sx - 80f, 0f), Offset(sx + 80f, size.height)
                    )
                )
                // bottom shadow
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                        size.height * 0.7f, size.height
                    )
                )
                // border
                drawRect(TileBorder, style = Stroke(1f))
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick?.invoke() }
                )
            }
            .padding(10.dp),
        content = content
    )
}

// ── Grid background ───────────────────────────────────────────────────────────
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid() {
    val s = 32.dp.toPx()
    val c = Color(0x08FFFFFF)
    var x = 0f; while (x <= size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 0.5f); x += s }
    var y = 0f; while (y <= size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 0.5f); y += s }
}
