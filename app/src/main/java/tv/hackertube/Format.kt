package tv.hackertube

import java.time.Duration
import java.time.Instant
import java.util.Locale

// The same wording as hackertube.org.

/** "4:48" or "1:02:03". */
fun duration(seconds: Int?): String {
    if (seconds == null || seconds == 0) return ""
    val h = seconds / 3600
    val m = seconds % 3600 / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/** "6 hr 32 min" or "45 min". */
fun hours(minutes: Int): String = if (minutes >= 60) "${minutes / 60} hr ${minutes % 60} min" else "$minutes min"

/** "43.9M views • 13 years ago". */
fun Video.stats(separator: String = " • "): String =
    listOfNotNull(views?.let(::viewCount), published?.let(::ago)).joinToString(separator)

private fun viewCount(n: Long): String = when {
    n >= 1_000_000_000 -> "${oneDecimal(n / 1e9)}B views"
    n >= 1_000_000 -> "${oneDecimal(n / 1e6)}M views"
    n >= 1_000 -> "${Math.round(n / 1e3)}K views"
    else -> "$n views"
}

private fun oneDecimal(x: Double) = "%.1f".format(Locale.US, x).removeSuffix(".0")

private fun ago(iso: String): String {
    val days = Duration.between(Instant.parse(iso), Instant.now()).toDays()
    val (n, unit) = listOf(365L to "year", 30L to "month", 7L to "week", 1L to "day")
        .map { (size, unit) -> days / size to unit }
        .firstOrNull { it.first >= 1 } ?: return "today"
    return "$n $unit${if (n > 1) "s" else ""} ago"
}
