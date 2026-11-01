package tv.hackertube

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.edit

/**
 * The videos the viewer liked, disliked, saved for later and opened, oldest first.
 * Like the website, it stays on the device and only travels to the API to shape the Home feed.
 */
class Library(context: Context) {
    private val prefs = context.getSharedPreferences("library", Context.MODE_PRIVATE)

    val liked = load("liked")
    val disliked = load("disliked")
    val later = load("later")
    val seen = load("seen")

    fun like(vid: String) = vote(vid, liked, disliked)

    fun dislike(vid: String) = vote(vid, disliked, liked)

    fun toggleLater(vid: String) {
        later.toggle(vid)
        save()
    }

    fun markSeen(vid: String) {
        seen.remove(vid)
        seen.add(vid)
        if (seen.size > MAX_SEEN) seen.removeRange(0, seen.size - MAX_SEEN)
        save()
    }

    private fun vote(vid: String, list: SnapshotStateList<String>, opposite: SnapshotStateList<String>) {
        opposite.remove(vid)
        list.toggle(vid)
        save()
    }

    private fun load(key: String) = mutableStateListOf<String>().apply {
        addAll(prefs.getString(key, "").orEmpty().split(",").filter { it.isNotEmpty() })
    }

    private fun save() = prefs.edit {
        putString("liked", liked.joinToString(","))
        putString("disliked", disliked.joinToString(","))
        putString("later", later.joinToString(","))
        putString("seen", seen.joinToString(","))
    }

    private fun MutableList<String>.toggle(vid: String) {
        if (!remove(vid)) add(vid)
    }

    private companion object {
        const val MAX_SEEN = 1000
    }
}
