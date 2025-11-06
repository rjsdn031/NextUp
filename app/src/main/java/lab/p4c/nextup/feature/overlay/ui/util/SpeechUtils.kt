package lab.p4c.nextup.feature.overlay.ui.util

data class LcsResult(
    val matchedInTarget: BooleanArray, // target 각 인덱스가 LCS에 포함됐는지
    val lcsLen: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LcsResult

        if (lcsLen != other.lcsLen) return false
        if (!matchedInTarget.contentEquals(other.matchedInTarget)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lcsLen
        result = 31 * result + matchedInTarget.contentHashCode()
        return result
    }
}

private fun lcsOnChars(target: String, hyp: String): LcsResult {
    val n = target.length
    val m = hyp.length
    val dp = Array(n + 1) { IntArray(m + 1) }

    for (i in 1..n) {
        val tc = target[i - 1]
        for (j in 1..m) {
            dp[i][j] = if (tc == hyp[j - 1]) dp[i - 1][j - 1] + 1
            else maxOf(dp[i - 1][j], dp[i][j - 1])
        }
    }

    val matched = BooleanArray(n)
    var i = n; var j = m
    while (i > 0 && j > 0) {
        when {
            target[i - 1] == hyp[j - 1] -> { matched[i - 1] = true; i--; j-- }
            dp[i - 1][j] >= dp[i][j - 1] -> i--
            else -> j--
        }
    }
    return LcsResult(matchedInTarget = matched, lcsLen = dp[n][m])
}

private fun normalizeForSpeech(s: String) = s
    .lowercase()
    .replace(Regex("\\p{Punct}"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

// LCS 기반 유사도: LCS 길이 / target 길이
fun getSimilarity(rawTarget: String, rawHyp: String): Pair<LcsResult, Float> {
    val t = normalizeForSpeech(rawTarget)
    val h = normalizeForSpeech(rawHyp)
    if (t.isEmpty() || h.isEmpty()) return LcsResult(BooleanArray(0), 0) to 0f
    val res = lcsOnChars(t, h)
    val sim = res.lcsLen.toFloat() / t.length.toFloat()
    return res to sim.coerceIn(0f, 1f)
}
