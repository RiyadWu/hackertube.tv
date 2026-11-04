package tv.hackertube

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(page: CategoriesPage) {
    val nav = LocalNavigator.current
    Loaded(page.areas) { areas ->
        PageColumn(page.list) {
            item(key = "#head") {
                Column {
                    PageHead("分类")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        quickLinks.forEachIndexed { i, (title, filters) ->
                            Pill(
                                title,
                                onClick = { nav.open(GridPage.filtered(title, filters)) },
                                modifier = Modifier.restoreFocus(page, "quick:$title", first = i == 0),
                            )
                        }
                    }
                }
            }
            items(areas, key = { it.id }) { area ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(area.name, style = Type.section)
                        Text(count(area.count), style = Type.sub)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (category in area.categories) {
                            Chip(
                                category.name,
                                onClick = {
                                    nav.open(GridPage.filtered(category.name, mapOf("cat" to category.id), crumb = area.name))
                                },
                                modifier = Modifier.restoreFocus(page, "cat:${category.id}"),
                                trailing = count(category.count),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The shortcuts at the top of the site's Categories page. */
private val quickLinks = listOf(
    "最近推荐" to mapOf("sort" to "recent"),
    "长视频" to mapOf("dur" to "xlong"),
    "冷门佳作" to mapOf("gems" to "1"),
    "全部视频" to emptyMap(),
)

@Composable
fun PlaylistsScreen(page: PlaylistsPage) {
    Loaded(page.playlists) { playlists ->
        PageColumn(page.list) {
            item(key = "#head") {
                PageHead(
                    "片单",
                    subtitle = "由 Claude 从 Hacker News 推荐的视频中挑选，每个片单都按特意安排的顺序排列。",
                )
            }
            val first = playlists.firstOrNull { (it.tier ?: "deep") == tiers.first().tier }
            for (tier in tiers) {
                val shelf = playlists.filter { (it.tier ?: "deep") == tier.tier }
                if (shelf.isEmpty()) continue
                item(key = "#${tier.tier}") {
                    Column {
                        Text(tier.title, style = Type.section)
                        Text(tier.blurb, style = Type.sub)
                    }
                }
                items(shelf.chunked(4), key = { row -> row.first().slug }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (playlist in row) {
                            PlaylistTile(
                                playlist,
                                Modifier.weight(1f),
                                Modifier.restoreFocus(page, playlist.slug, first = playlist == first),
                            )
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

private class Tier(val tier: String, val title: String, val blurb: String)

private val tiers = listOf(
    Tier("start", "从这里开始", "覆盖面广的必看片单，适合第一次来。"),
    Tier("deep", "深入专题", "每个片单只钻一个主题，适合想一头扎进去的时候。"),
)

@Composable
private fun PlaylistTile(playlist: PlaylistCard, modifier: Modifier, focusModifier: Modifier) {
    val nav = LocalNavigator.current
    Column(modifier) {
        Surface(
            onClick = { nav.open(PlaylistPage(playlist.slug)) },
            modifier = focusModifier,
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
            colors = ClickableSurfaceDefaults.colors(containerColor = Colors.bg2, focusedContainerColor = Colors.bg2),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(3.dp, Colors.text))),
        ) {
            Box {
                Mosaic(playlist.cover)
                Text(
                    "${playlist.count} 个视频",
                    style = Type.badge,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .background(Color.Black.copy(alpha = .8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Text(
            playlist.title,
            style = Type.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
        )
        Text(hours(playlist.minutes), style = Type.sub)
    }
}

@Composable
fun PlaylistScreen(page: PlaylistPage) {
    Loaded(page.playlist) { playlist ->
        PageColumn(page.list) {
            item(key = "#head") {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    Mosaic(playlist.videos.map { it.vid }, Modifier.width(320.dp))
                    Column(Modifier.weight(1f)) {
                        Text("片单", style = Type.sub)
                        Text(playlist.title, style = Type.pageTitle)
                        Text(
                            "Claude 挑选 · ${playlist.videos.size} 个视频 · ${hours(playlist.minutes)}",
                            style = Type.sub,
                        )
                        Text(playlist.intro, style = Type.body, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            itemsIndexed(playlist.videos, key = { _, video -> video.vid }) { i, video ->
                VideoRow(
                    video,
                    Modifier.widthIn(max = 1000.dp).restoreFocus(page, video.vid, first = i == 0),
                    number = i + 1,
                )
            }
        }
    }
}

@Composable
private fun PageColumn(state: LazyListState, content: LazyListScope.() -> Unit) = LazyColumn(
    state = state,
    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
    modifier = Modifier.fillMaxSize(),
    content = content,
)

private fun count(n: Int) = "%,d".format(Locale.US, n)
