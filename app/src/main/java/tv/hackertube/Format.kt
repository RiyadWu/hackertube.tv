package tv.hackertube

import java.time.Duration
import java.time.Instant
import java.util.Locale

// The wording of hackertube.org, in Chinese.

/** "4:48" or "1:02:03". */
fun duration(seconds: Int?): String {
    if (seconds == null || seconds == 0) return ""
    val h = seconds / 3600
    val m = seconds % 3600 / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/** "6 小时 32 分钟" or "45 分钟". */
fun hours(minutes: Int): String = if (minutes >= 60) "${minutes / 60} 小时 ${minutes % 60} 分钟" else "$minutes 分钟"

/** "4390万次观看 • 13 年前". */
fun Video.stats(separator: String = " • "): String =
    listOfNotNull(views?.let(::viewCount), published?.let(::ago)).joinToString(separator)

private fun viewCount(n: Long): String = when {
    n >= 100_000_000 -> "${oneDecimal(n / 1e8)}亿次观看"
    n >= 10_000 -> "${oneDecimal(n / 1e4)}万次观看"
    else -> "${n}次观看"
}

private fun oneDecimal(x: Double) = "%.1f".format(Locale.US, x).removeSuffix(".0")

private fun ago(iso: String): String {
    val days = Duration.between(Instant.parse(iso), Instant.now()).toDays()
    val (n, unit) = listOf(365L to "年", 30L to "个月", 7L to "周", 1L to "天")
        .map { (size, unit) -> days / size to unit }
        .firstOrNull { it.first >= 1 } ?: return "今天"
    return "$n ${unit}前"
}

/** hackertube.org's content_type and shelf_life values, as the details page shows them. */
fun typeLabel(type: String): String = when (type) {
    "talk" -> "演讲"
    "lecture" -> "讲座"
    "course" -> "课程"
    "documentary" -> "纪录片"
    "explainer" -> "讲解"
    "interview" -> "访谈"
    "podcast" -> "播客"
    "tutorial" -> "教程"
    "demo" -> "演示"
    "news" -> "新闻"
    "commentary" -> "评论"
    "comedy" -> "喜剧"
    "music" -> "音乐"
    "other" -> "其他"
    "evergreen" -> "常看常新"
    "dated" -> "有时效"
    else -> type
}
