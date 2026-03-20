package com.smithmk.smarthomehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// ── Colours ───────────────────────────────────────────────────────────────────

val BgColour       = Color(0xFF0A0E14)
val CardTop        = Color(0xFF142440)
val CardBottom     = Color(0xFF0E1830)
val Gold           = Color(0xFFC4A96B)
val Amber          = Color(0xFFFFB84D)
val LightOn        = Color(0xFFFFC107)
val BlindPurple    = Color(0xFF9C27B0)
val AlarmGreen     = Color(0xFF4CAF50)
val SolarYellow    = Color(0xFFFFEB3B)
val WeatherBlue    = Color(0xFF2196F3)
val TextPrimary    = Color(0xFFFFFFFF)
val TextSecondary  = Color(0xBFFFFFFF)
val TextDim        = Color(0x66FFFFFF)
val TextLabel      = Color(0x99FFFFFF)
val TopHighlight   = Color(0x33FFFFFF)
val TileBorder     = Color(0x1AFFFFFF)

val LabelStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp)

// ── Tile glow colours ─────────────────────────────────────────────────────────

enum class Glow(val colour: Color) {
    None(Color.Transparent),
    Lights(Color(0x99C4A96B)),
    Security(Color(0x994CAF50)),
    Blinds(Color(0x999C27B0)),
    Energy(Color(0x99FFEB3B)),
    Weather(Color(0x992196F3)),
    Climate(Color(0x9900BCD4)),
    Media(Color(0x99E91E63)),
}

// ── MainActivity ──────────────────────────────────────────────────────────────

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColour)
            .drawBehind { drawGrid() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 6.dp)
                .padding(top = 48.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Header()

            // Row 1 — Lights + Security
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Tile(modifier = Modifier.weight(1.4f).height(110.dp), isActive = true, glow = Glow.Lights) {
                    TileContent(label = "LIGHTS", value = "4", unit = "/ 12 ON", status = "ACTIVE", statusColour = Amber)
                }
                Tile(modifier = Modifier.weight(1f).height(110.dp), isActive = false, glow = Glow.Security) {
                    TileContent(label = "SECURITY", value = "—", unit = "", status = "DISARMED", statusColour = TextDim)
                }
            }

            // Row 2 — Solar + Weather
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Tile(modifier = Modifier.weight(1f).height(110.dp), isActive = true, glow = Glow.Energy) {
                    TileContent(label = "SOLAR", value = "3.2", unit = "kW", status = "🔋 84%", statusColour = SolarYellow)
                }
                Tile(modifier = Modifier.weight(1f).height(110.dp), isActive = true, glow = Glow.Weather) {
                    TileContent(label = "WEATHER", value = "24", unit = "°C", status = "SUNNY", statusColour = WeatherBlue)
                }
            }

            // Row 3 — Blinds + Climate
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Tile(modifier = Modifier.weight(1f).height(100.dp), isActive = false, glow = Glow.Blinds) {
                    TileContent(label = "BLINDS", value = "0", unit = "/ 6", status = "CLOSED", statusColour = TextDim)
                }
                Tile(modifier = Modifier.weight(1f).height(100.dp), isActive = false, glow = Glow.Climate) {
                    TileContent(label = "CLIMATE", value = "21", unit = "°C", status = "OFF", statusColour = TextDim)
                }
            }

            // Row 4 — Rooms + Media
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Tile(modifier = Modifier.weight(1f).height(100.dp), isActive = false, glow = Glow.None) {
                    TileContent(label = "ROOMS", value = "🏘", unit = "", status = "BY ROOM", statusColour = TextDim)
                }
                Tile(modifier = Modifier.weight(1f).height(100.dp), isActive = false, glow = Glow.Media) {
                    TileContent(label = "MEDIA", value = "—", unit = "", status = "STOPPED", statusColour = TextDim)
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
fun Header() {
    val time = remember { SimpleDateFormat("HH:mm", Locale.UK).format(Date()) }
    val date = remember { SimpleDateFormat("EEE d MMM", Locale.UK).format(Date()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("SMITHMK HOME", style = LabelStyle, color = Gold, letterSpacing = 3.sp)
            Text(date.uppercase(), style = LabelStyle, color = TextDim)
        }
        Text(time, fontSize = 36.sp, fontWeight = FontWeight.W100, color = TextPrimary)
    }
}

// ── Tile ──────────────────────────────────────────────────────────────────────

@Composable
fun Tile(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    glow: Glow = Glow.None,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "tile")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isActive && glow != Glow.None) 0.85f else 0.4f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = if (pressed) 0f else if (isActive) 24f else 8f; this.shape = shape; clip = true }
            .drawBehind {
                if (isActive && glow != Glow.None) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            asFrameworkPaint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.TRANSPARENT
                                setShadowLayer(48f, 0f, 8f, glow.colour.copy(alpha = glowAlpha).toArgb())
                            }
                        }
                        canvas.drawRoundRect(0f, 0f, size.width, size.height, 12.dp.toPx(), 12.dp.toPx(), paint)
                    }
                }
            }
            .clip(shape)
            .background(Brush.linearGradient(listOf(CardTop, CardBottom), Offset.Zero, Offset(0f, Float.POSITIVE_INFINITY)))
            .drawWithContent {
                drawContent()
                // top highlight
                drawRect(Brush.verticalGradient(listOf(TopHighlight, Color.Transparent), 0f, size.height * 0.3f))
                // shimmer
                val sx = size.width * shimmer
                drawRect(Brush.linearGradient(listOf(Color.Transparent, Gold.copy(alpha = 0.06f), Color.Transparent), Offset(sx - 80f, 0f), Offset(sx + 80f, size.height)))
                // bottom shadow
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), size.height * 0.7f, size.height))
                // border
                drawRect(TileBorder, style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            }
            .padding(12.dp),
        content = content
    )
}

// ── Tile content layout ───────────────────────────────────────────────────────

@Composable
fun TileContent(label: String, value: String, unit: String, status: String, statusColour: Color) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = LabelStyle, color = TextLabel)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 38.sp, fontWeight = FontWeight.W100, color = TextPrimary, maxLines = 1)
            if (unit.isNotEmpty()) {
                Text(" $unit", fontSize = 12.sp, fontWeight = FontWeight.W300, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
        Text(status, style = LabelStyle, color = statusColour, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── Grid background ───────────────────────────────────────────────────────────

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid() {
    val spacing = 32.dp.toPx()
    val c = Color(0x08FFFFFF)
    var x = 0f; while (x <= size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 0.5f); x += spacing }
    var y = 0f; while (y <= size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 0.5f); y += spacing }
}
