package tv.hackertube

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(page: DetailsPage) {
    val nav = LocalNavigator.current
    val library = LocalLibrary.current
    LaunchedEffect(page) { page.details.load() }
    // the card already has the basics, so draw those while the rest loads
    val video = page.details.value?.video ?: page.video
    val comments = page.details.value?.comments.orEmpty()

    LazyColumn(
        state = page.list,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "#head") {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Thumbnail(video, Modifier.padding(top = 4.dp).width(400.dp).clip(RoundedCornerShape(12.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        video.title,
                        style = Type.pageTitle.copy(fontSize = 22.sp, lineHeight = 28.sp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(video.channel, style = Type.body.copy(fontWeight = FontWeight.Bold))
                        video.hn?.let { Text(recommendations(it), style = Type.sub.copy(fontSize = 13.sp)) }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 6.dp),
                    ) {
                        Pill(
                            "Play on YouTube",
                            icon = R.drawable.ic_play_arrow_filled,
                            onClick = { nav.play(video) },
                            modifier = Modifier.restoreFocus(page, "play", first = true),
                            selected = true,
                        )
                        val liked = video.vid in library.liked
                        Pill(
                            "Like",
                            onClick = { library.like(video.vid) },
                            modifier = Modifier.restoreFocus(page, "like"),
                            icon = if (liked) R.drawable.ic_thumb_up_filled else R.drawable.ic_thumb_up,
                            selected = liked,
                        )
                        val disliked = video.vid in library.disliked
                        Pill(
                            "Not for me",
                            onClick = { library.dislike(video.vid) },
                            modifier = Modifier.restoreFocus(page, "dislike"),
                            icon = if (disliked) R.drawable.ic_thumb_down_filled else R.drawable.ic_thumb_down,
                            selected = disliked,
                        )
                        val saved = video.vid in library.later
                        Pill(
                            if (saved) "Saved" else "Watch later",
                            icon = if (saved) R.drawable.ic_check else R.drawable.ic_schedule,
                            onClick = { library.toggleLater(video.vid) },
                            modifier = Modifier.restoreFocus(page, "later"),
                            selected = saved,
                        )
                    }
                    Text(
                        listOf(video.stats(" · "), video.type, video.shelfLife).filter { it.isNotEmpty() }.joinToString(" · "),
                        style = Type.sub.copy(fontSize = 13.sp),
                    )
                }
            }
        }

        item(key = "#about") {
            Column(Modifier.widthIn(max = 1000.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (video.summary.isNotEmpty()) Text(video.summary, style = Type.body.copy(fontSize = 15.sp, lineHeight = 22.sp))
                if (video.whyHn.isNotEmpty()) Text(video.whyHn, style = Type.sub.copy(fontSize = 14.sp, lineHeight = 21.sp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                    for (category in video.categories) {
                        Chip(
                            category.name,
                            onClick = {
                                nav.open(GridPage.filtered(category.name, mapOf("cat" to category.id), crumb = category.areaName))
                            },
                            modifier = Modifier.restoreFocus(page, "cat:${category.id}"),
                        )
                    }
                }
            }
        }

        item(key = "#next") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Up next", style = Type.section)
                Loaded(page.related) { related ->
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                    ) {
                        items(related, key = { it.vid }) { next ->
                            Box(Modifier.width(220.dp)) {
                                VideoCard(next, Modifier.restoreFocus(page, "next:${next.vid}"))
                            }
                        }
                    }
                }
            }
        }

        if (comments.isNotEmpty()) {
            item(key = "#comments") {
                Text(
                    "${comments.size} comment${if (comments.size == 1) "" else "s"} from Hacker News",
                    style = Type.section,
                )
            }
            items(comments, key = { it.id }) { comment ->
                CommentCard(comment, Modifier.widthIn(max = 1000.dp).restoreFocus(page, "comment:${comment.id}"))
            }
        }
    }
}

/** A comment can only be read on a TV if it can take focus, so the list scrolls to it. */
@Composable
private fun CommentCard(comment: Comment, modifier: Modifier) {
    Surface(
        onClick = {},
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Colors.bg2),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(1.dp, Colors.bg3))),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("@${comment.author}", style = Type.body.copy(fontWeight = FontWeight.Bold))
                Text(
                    listOf(comment.ym.take(4), "in “${comment.story}”").joinToString(" · "),
                    style = Type.sub.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                // the API marks where a comment linked this video, or something else
                comment.text.replace("[VIDEO]", "▶ this video").replace("[LINK]", "link"),
                style = Type.body.copy(fontSize = 14.sp, lineHeight = 21.sp),
            )
        }
    }
}

private fun recommendations(hn: HnStats): String {
    fun plural(n: Int, word: String) = "$n $word${if (n == 1) "" else "s"}"
    val years = "${hn.first}–${hn.last}"
    return if (hn.recommenders > 0) {
        "${plural(hn.recommenders, "HN recommender")} · recommended in ${plural(hn.recMonths, "month")}, $years"
    } else {
        "Cited ${hn.mentions}× on HN, $years"
    }
}
