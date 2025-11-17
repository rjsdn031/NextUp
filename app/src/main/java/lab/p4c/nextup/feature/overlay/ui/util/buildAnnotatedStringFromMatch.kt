package lab.p4c.nextup.feature.overlay.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

fun buildAnnotatedStringFromMatch(
    target: String,
    matched: BooleanArray,
    matchedStyle: SpanStyle,
    dimmedStyle: SpanStyle
): AnnotatedString {
    if (target.isEmpty() || matched.isEmpty()) return AnnotatedString(target)

    return buildAnnotatedString {
        var runStart = 0
        var runMatched = matched.getOrElse(0) { false }
        for (idx in 1 until target.length) {
            val sameBucket = matched.getOrElse(idx) { false } == runMatched
            if (!sameBucket) {
                pushStyle(if (runMatched) matchedStyle else dimmedStyle)
                append(target.substring(runStart, idx))
                pop()
                runStart = idx
                runMatched = matched.getOrElse(idx) { false }
            }
        }
        pushStyle(if (runMatched) matchedStyle else dimmedStyle)
        append(target.substring(runStart, target.length))
        pop()
    }
}