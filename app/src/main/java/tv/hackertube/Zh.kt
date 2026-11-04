package tv.hackertube

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Simplified Chinese for what hackertube.org serves in English: video titles, summaries and
 * "why HN recommends it", topic areas, categories, playlist titles, intros and notes. The
 * translations ship in the app as a Java resource (hackertube/zh.json, built by
 * scripts/build-zh.py from translations/zh/).
 * Anything without a translation, such as a video added to the site later, stays in English.
 */
object Zh {
    @Serializable
    private class Data(
        val titles: Map<String, String> = emptyMap(),
        val summaries: Map<String, String> = emptyMap(),
        val whys: Map<String, String> = emptyMap(),
        val areas: Map<String, String> = emptyMap(),
        val categories: Map<String, String> = emptyMap(),
        val playlists: Map<String, String> = emptyMap(),
        /** "intro:<slug>" and "note:<slug>/<vid>" -> Chinese. */
        val playlistText: Map<String, String> = emptyMap(),
    )

    private val data: Data by lazy {
        try {
            val text = Zh::class.java.getResourceAsStream("/hackertube/zh.json")!!.use { it.readBytes().decodeToString() }
            Json { ignoreUnknownKeys = true }.decodeFromString<Data>(text)
        } catch (_: Exception) {
            Data() // missing or broken: everything stays in English
        }
    }

    fun title(vid: String, english: String) = data.titles[vid] ?: english
    fun summary(vid: String, english: String) = if (english.isEmpty()) english else data.summaries[vid] ?: english
    fun why(vid: String, english: String) = if (english.isEmpty()) english else data.whys[vid] ?: english
    fun area(id: String, english: String) = data.areas[id] ?: english
    fun category(id: String, english: String) = data.categories[id] ?: english
    fun playlist(slug: String, english: String) = data.playlists[slug] ?: english
    fun intro(slug: String, english: String) = if (english.isEmpty()) english else data.playlistText["intro:$slug"] ?: english
    fun note(slug: String, vid: String, english: String) =
        if (english.isEmpty()) english else data.playlistText["note:$slug/$vid"] ?: english

    /** True if [query] has Chinese in it, which hackertube.org's search (English only) can't match. */
    fun isChinese(query: String) = query.any { it in '\u4e00'..'\u9fff' }

    /**
     * Video ids whose Chinese title, or failing that summary, has every word of [query].
     * Title matches come first.
     */
    fun search(query: String, limit: Int = 60): List<String> {
        val words = query.lowercase().split(' ', '\u3000').filter { it.isNotEmpty() }
        if (words.isEmpty()) return emptyList()
        fun matches(text: String?) = text != null && text.lowercase().let { t -> words.all { it in t } }
        val inTitle = data.titles.filter { matches(it.value) }.keys
        val inSummary = data.summaries.filter { it.key !in inTitle && matches(it.value) }.keys
        return (inTitle + inSummary).take(limit)
    }
}

// Applied to every API response, so the screens show Chinese without knowing about it.

fun Video.zh() = copy(
    title = Zh.title(vid, title),
    titleEn = title,
    summary = Zh.summary(vid, summary),
    whyHn = Zh.why(vid, whyHn),
    categories = categories.map { it.zh() },
)

fun List<Video>.zh() = map { it.zh() }

fun CategoryRef.zh() = copy(name = Zh.category(id, name), areaName = Zh.area(area, areaName))

fun Area.zh() = copy(name = Zh.area(id, name), categories = categories.map { it.copy(name = Zh.category(it.id, it.name)) })

fun PlaylistCard.zh() = copy(title = Zh.playlist(slug, title))

fun Playlist.zh() = copy(
    title = Zh.playlist(slug, title),
    intro = Zh.intro(slug, intro),
    videos = videos.map { it.zh().copy(note = Zh.note(slug, it.vid, it.note)) },
)

fun VideoPage.zh() = copy(video = video.zh())
