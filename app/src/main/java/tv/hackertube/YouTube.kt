package tv.hackertube

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

private val youtubeApps = listOf(
    "com.google.android.youtube.tv", // Android TV and Google TV
    "com.amazon.firetv.youtube", // Fire TV
    null, // anything else that opens YouTube links
)

/** Plays [vid] in the YouTube app on the TV. */
fun Context.playOnYouTube(vid: String) {
    val link = "https://www.youtube.com/watch?v=$vid".toUri()
    for (app in youtubeApps) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, link).setPackage(app)
                    // YouTube for TV then opens its bare player, and Back comes straight back here
                    // instead of to YouTube's home screen.
                    .putExtra("force_fullscreen", true)
                    .putExtra("finish_on_ended", true),
            )
            return
        } catch (_: ActivityNotFoundException) {
        }
    }
    Toast.makeText(this, "Install YouTube to play videos", Toast.LENGTH_LONG).show()
}
