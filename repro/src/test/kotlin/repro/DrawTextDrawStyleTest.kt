package repro

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Draws "Hello" in black in two canvases, each with its own TextMeasurer so they don't share a
 * cached TextLayoutResult: one passes drawStyle = Fill, the other passes no drawStyle. Both should
 * paint the same pixels.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DrawTextDrawStyleTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun noDrawStyleFillsTheText() = assertSamePixels("plain") {}

    @Test
    fun noDrawStyleFillsTheTextAfterAStrokedDrawOfTheSameLayout() =
        assertSamePixels("after-stroke") { layout ->
            // A halo: the same layout drawn first as a white outline.
            drawText(layout, Color.White, drawStyle = Stroke(width = 4f))
        }

    private fun assertSamePixels(
        name: String,
        before: DrawScope.(TextLayoutResult) -> Unit,
    ) {
        val style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Bold)
        compose.setContent {
            Column {
                val explicit = rememberTextMeasurer()
                Canvas(Modifier.size(240.dp, 100.dp).testTag("fill")) {
                    val layout = explicit.measure("Hello", style)
                    before(layout)
                    drawText(layout, Color.Black, drawStyle = Fill)
                }
                val unspecified = rememberTextMeasurer()
                Canvas(Modifier.size(240.dp, 100.dp).testTag("none")) {
                    val layout = unspecified.measure("Hello", style)
                    before(layout)
                    drawText(layout, Color.Black)
                }
            }
        }

        val filled = capture("fill", "$name-fill")
        val unspecified = capture("none", "$name-no-drawstyle")
        val differing = (0 until filled.width).sumOf { x -> (0 until filled.height).count { y -> filled[x, y] != unspecified[x, y] } }
        println("$name: $differing of ${filled.width * filled.height} pixels differ between drawStyle = Fill and no drawStyle")

        assertEquals("drawText without a drawStyle should paint what drawStyle = Fill paints", 0, differing)
    }

    private fun capture(
        tag: String,
        file: String,
    ): PixelMap {
        val image = compose.onNodeWithTag(tag).captureToImage()
        File("build/outputs").mkdirs()
        File("build/outputs/$file.png").outputStream().use { image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        return image.toPixelMap()
    }
}
