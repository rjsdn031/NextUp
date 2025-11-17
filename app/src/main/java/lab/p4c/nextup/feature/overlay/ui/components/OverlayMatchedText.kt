package lab.p4c.nextup.feature.overlay.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import lab.p4c.nextup.feature.overlay.ui.util.buildAnnotatedStringFromMatch
import lab.p4c.nextup.feature.overlay.ui.util.getSimilarity

@Composable
fun OverlayMatchedText(
    target: String,
    partial: String,
    textColor: Color,
    matchedColor: Color
) {
    val annotated = remember(target, partial) {

        if (partial.isBlank()) {
            return@remember buildAnnotatedString {
                pushStyle(
                    SpanStyle(
                        color = matchedColor,
                        fontWeight = FontWeight.Normal
                    )
                )
                append(target)
                pop()
            }
        }

        val (res, _) = getSimilarity(target, partial)
        buildAnnotatedStringFromMatch(
            target = target,
            matched = res.matchedInTarget,
            matchedStyle = SpanStyle(
                color = matchedColor,
                fontWeight = FontWeight.SemiBold
            ),
            dimmedStyle = SpanStyle(color = textColor)
        )
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        lineHeight = 28.sp
    )
}