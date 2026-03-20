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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

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

val LabelStyle  = TextStyle(fontSize = 9.sp,  fontWeight = FontWeight.Medium, letterSpacing = 1.8.sp)
val ValueStyle  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.W100)
val UnitStyle   = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.W300, color = Color(0x99FFFFFF))
val StatusStyle = TextStyle(fontSize = 9.sp,  fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp)

data class TileData(
    val label: String,
    val value: String,
    val unit: String,
    val status: String,
    val isActive: Boolean,
    val glowColour: Color,
    val statusColour: Color,
    val icon: String
)

val tiles = listOf(
    TileData("LIGHTS",   "4",    "/12",  "ACTIVE",   true,  Color(0xAAC4A96B), Amber,       "💡"),
    TileData("SECURITY", "—",    "",     "DISARMED", false, Color(0xAA4CAF50), TextDim,     "🔒"),
    TileData("SOLAR",    "3.2",  "kW",   "🔋 84%",   true,  Color(0xAAFFEB3B), SolarYellow, "☀️"),
    TileData("WEATHER",  "24",   "°C",   "SUNNY",    true,  Color(0xAA2196F3), WeatherBlue, "🌤"),
    TileData("BLINDS",   "0",    "/6",   "CLOSED",   false, Color(0xAA9C27B0), TextDim,     "🪟"),
    TileData("CLIMATE",  "21",   "°C",   "OFF",      false, Color(0xAA00BCD4), TextDim,     "🌡"),
    TileData("MEDIA",    "—",    "",     "STOPPED",  false, Color(0xAAE91E63), TextDim,     "🎵"),
    TileData("ENERGY",   "1.2",  "kW",   "IMPORTING",false, Color(0xAAFFEB3B), SolarYellow, "⚡"),
    TileData("ROOMS",    "12",   "",     "ONLINE",   true,  Color(0xAAC4A96B), Amber,       "🏠"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HomeScreen() }
    }
}

@Composable
fun HomeScreen() {
    val time = remember { SimpleDateFormat("HH:mm", Locale.UK).format(Date()) }
    val date = remember { SimpleDateFormat("EEE d MMM", Locale.UK).format(Date()) }
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableStateOf(0) }

    // Derive selected index from scroll position
    LaunchedEffect(listState.firstVisibleItemIndex) {
        selectedIndex = listState.firstVisibleItemIndex
    }

    val selectedTile = tiles[selectedIndex.coerceIn(0, tiles.lastIndex)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColour)
            .drawBehind { drawGrid() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 8.dp),
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

            // ── Big centre display for selected tile ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(selectedTile.icon, fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(selectedTile.label, style = LabelStyle, color = Gold, letterSpacing = 3.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(selectedTile.value, style = ValueStyle, color = TextPrimary)
                        if (selectedTile.unit.isNotEmpty()) {
                            Text(" ${selectedTile.unit}", style = UnitStyle, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        selectedTile.status,
                        style = StatusStyle,
                        color = selectedTile.statusColour,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Dot indicators ────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                tiles.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == selectedIndex) 6.dp else 3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == selectedIndex) Gold else TextDim)
                    )
                }
            }

            // ── Carousel strip ────────────────────────────────────────────
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp)
            ) {
                itemsIndexed(tiles) { index, tile ->
                    val isSelected = index == selectedIndex
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 0.85f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        label = "scale"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.45f,
                        animationSpec = tween(200),
                        label = "alpha"
                    )

                    CarouselTile(
                        tile = tile,
                        isSelected = isSelected,
                        scale = scale,
                        alpha = alpha,
                        onClick = {
                            selectedIndex = index
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CarouselTile(
    tile: TileData,
    isSelected: Boolean,
    scale: Float,
    alpha: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isSelected && tile.isActive) 1f else 0.5f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                shadowElevation = if (isSelected) 32f else 4f
                this.shape = shape
                clip = true
            }
            .drawBehind {
                if (isSelected && tile.isActive) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            asFrameworkPaint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.TRANSPARENT
                                setShadowLayer(40f, 0f, 6f, tile.glowColour.copy(alpha = glowAlpha).toArgb())
                            }
                        }
                        canvas.drawRoundRect(0f, 0f, size.width, size.height, 16.dp.toPx(), 16.dp.toPx(), paint)
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
                drawRect(Brush.verticalGradient(listOf(TopHighlight, Color.Transparent), 0f, size.height * 0.35f))
                val sx = size.width * shimmer
                if (isSelected) {
                    drawRect(
                        Brush.linearGradient(
                            listOf(Color.Transparent, Gold.copy(alpha = 0.08f), Color.Transparent),
                            Offset(sx - 60f, 0f), Offset(sx + 60f, size.height)
                        )
                    )
                }
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), size.height * 0.65f, size.height))
                drawRect(TileBorder, style = Stroke(1f))
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(tile.icon, fontSize = 28.sp)
            Text(
                tile.label,
                style = LabelStyle,
                color = if (isSelected) Gold else TextLabel,
                maxLines = 1
            )
            Text(
                "${tile.value}${tile.unit}",
                fontSize = 13.sp,
                fontWeight = FontWeight.W200,
                color = if (isSelected) TextPrimary else TextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid() {
    val spacing = 32.dp.toPx()
    val c = Color(0x08FFFFFF)
    var x = 0f; while (x <= size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 0.5f); x += spacing }
    var y = 0f; while (y <= size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 0.5f); y += spacing }
}
