package tv.hackertube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/** The JSON API behind hackertube.org. */
object Api {
    private val base = "https://hackertube.org/api/".toHttpUrl()
    private val http = OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build()
    private val json = Json { ignoreUnknownKeys = true }

    /** The Home feed: videos like the liked ones, without the disliked and the already seen. */
    suspend fun feed(library: Library, seed: Int, offset: Int, n: Int): List<Video> = get<List<Video>>(
        "feed",
        "like" to library.liked.joinToString(","),
        "dislike" to library.disliked.joinToString(","),
        // keep the URL short; the newest entries matter most
        "seen" to library.seen.takeLast(200).joinToString(","),
        "seed" to "$seed",
        "offset" to "$offset",
        "n" to "$n",
    ).zh()

    /** Every video that matches [filters] (such as `cat`, `area`, `dur`, `sort`), best first. */
    suspend fun list(filters: Map<String, String>, offset: Int, n: Int): List<Video> =
        get<List<Video>>("list", *filters.toList().toTypedArray(), "offset" to "$offset", "n" to "$n").zh()

    suspend fun search(query: String): List<Video> =
        if (Zh.isChinese(query)) docs(Zh.search(query)) else get<SearchResults>("search", "q" to query).results.zh()

    suspend fun video(vid: String): VideoPage = get<VideoPage>("video", "vid" to vid).zh()

    suspend fun related(vid: String): List<Video> = get<List<Video>>("related", "vid" to vid, "n" to "20").zh()

    /** The videos for [vids], in the same order. */
    suspend fun docs(vids: List<String>): List<Video> =
        if (vids.isEmpty()) emptyList() else get<List<Video>>("docs", "vids" to vids.joinToString(",")).zh()

    suspend fun areas(): List<Area> = get<List<Area>>("browse").map { it.zh() }

    suspend fun playlists(): List<PlaylistCard> = get<List<PlaylistCard>>("playlists").map { it.zh() }

    suspend fun playlist(slug: String): Playlist = get<Playlist>("playlist", "slug" to slug).zh()

    private suspend inline fun <reified T> get(path: String, vararg params: Pair<String, String>): T {
        val url = base.newBuilder().addPathSegment(path).apply {
            params.filter { it.second.isNotEmpty() }.forEach { addQueryParameter(it.first, it.second) }
        }.build()
        return json.decodeFromString(fetch(url))
    }

    private suspend fun fetch(url: HttpUrl): String = withContext(Dispatchers.IO) {
        http.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw HttpError(response.code)
            response.body.string()
        }
    }
}

class HttpError(val code: Int) : IOException("HTTP $code")

@Serializable
data class Video(
    val vid: String,
    val title: String,
    val channel: String = "",
    val published: String? = null,
    @SerialName("duration_s") val durationSeconds: Int? = null,
    val views: Long? = null,
    val summary: String = "",
    @SerialName("why_hn") val whyHn: String = "",
    @SerialName("content_type") val type: String = "",
    @SerialName("shelf_life") val shelfLife: String = "",
    val categories: List<CategoryRef> = emptyList(),
    val hn: HnStats? = null,
    /** The topic a search result matched on, when it matched on meaning rather than words. */
    val via: String? = null,
    /** A playlist's note on this video. */
    val note: String = "",
    /** The original English title when [title] holds a translation (set by [zh], not the API). */
    @Transient val titleEn: String = "",
)

@Serializable
data class CategoryRef(
    val id: String,
    val name: String,
    val area: String = "",
    @SerialName("area_name") val areaName: String = "",
)

@Serializable
data class HnStats(
    val mentions: Int = 0,
    val recommenders: Int = 0,
    @SerialName("rec_months") val recMonths: Int = 0,
    val first: String = "",
    val last: String = "",
)

@Serializable
data class VideoPage(val video: Video, val comments: List<Comment> = emptyList())

@Serializable
data class Comment(
    val id: Long,
    val author: String,
    val ym: String = "",
    val story: String = "",
    val text: String = "",
)

@Serializable
private data class SearchResults(val results: List<Video>)

@Serializable
data class Area(val id: String, val name: String, val count: Int, val categories: List<Category>)

@Serializable
data class Category(val id: String, val name: String, val count: Int)

@Serializable
data class PlaylistCard(
    val slug: String,
    val title: String,
    val tier: String? = null,
    val minutes: Int = 0,
    val count: Int = 0,
    val cover: List<String> = emptyList(),
)

@Serializable
data class Playlist(
    val slug: String,
    val title: String,
    val intro: String = "",
    val minutes: Int = 0,
    val videos: List<Video> = emptyList(),
)
