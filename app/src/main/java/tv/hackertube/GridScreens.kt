package tv.hackertube

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The type chips of hackertube-zh's feed. */
private val videoTypes = listOf(
    "talk" to "演讲",
    "lecture" to "讲座",
    "course" to "课程",
    "explainer" to "讲解",
    "documentary" to "纪录片",
    "interview" to "访谈",
    "demo" to "演示",
)

/** hackertube-zh's sort menu, as chips. */
private val sorts = listOf(
    null to "综合排序",
    "hn" to "推荐最多",
    "recent" to "最近提及",
)

@Composable
fun HomeScreen(page: HomePage) {
    HoldFocusUntilLoaded(page, page.feed)
    VideoGrid(page, page.feed, page.grid, empty = "没有符合条件的视频。") {
        fullWidth("wordmark") { Wordmark(Modifier.padding(start = 4.dp)) }
        fullWidth("types") {
            // the topic areas are in the sidebar; the chip bar filters by type, as on hackertube-zh
            ChipRow {
                item {
                    Chip(
                        "全部",
                        { page.show(null) },
                        Modifier.restoreFocus(page, ALL, first = page.feed.videos.isEmpty()),
                        selected = page.type == null,
                    )
                }
                items(videoTypes, key = { it.first }) { (type, label) ->
                    Chip(label, { page.show(type) }, Modifier.restoreFocus(page, "type:$type"), selected = page.type == type)
                }
            }
        }
    }
}

@Composable
fun AreaScreen(page: AreaPage) {
    val nav = LocalNavigator.current
    HoldFocusUntilLoaded(page, page.feed)
    VideoGrid(page, page.feed, page.grid, empty = "没有符合条件的视频。") {
        fullWidth("head") { PageHead(page.area.name, subtitle = "${page.area.count} 个视频") }
        fullWidth("types") {
            ChipRow {
                item {
                    Chip(
                        "全部类型",
                        { page.show(type = null) },
                        Modifier.restoreFocus(page, ALL, first = page.feed.videos.isEmpty()),
                        selected = page.type == null,
                    )
                }
                items(videoTypes, key = { it.first }) { (type, label) ->
                    Chip(label, { page.show(type = type) }, Modifier.restoreFocus(page, "type:$type"), selected = page.type == type)
                }
            }
        }
        fullWidth("sorts") {
            ChipRow {
                items(sorts, key = { it.second }) { (sort, label) ->
                    Chip(label, { page.show(sort = sort) }, Modifier.restoreFocus(page, "sort:$sort"), selected = page.sort == sort)
                }
            }
        }
        fullWidth("categories") {
            // the area's finer topics, as on the Categories page
            ChipRow {
                items(page.area.categories, key = { it.id }) { category ->
                    Chip(
                        category.name,
                        { nav.open(GridPage.filtered(category.name, mapOf("cat" to category.id), crumb = page.area.name)) },
                        Modifier.restoreFocus(page, "cat:${category.id}"),
                        trailing = "${category.count}",
                    )
                }
            }
        }
    }
}

/**
 * Until [feed] arrives, the first chip holds focus, or the sidebar takes it and stays shut.
 * Then focus moves on to the first video, unless the viewer has moved it.
 */
@Composable
private fun HoldFocusUntilLoaded(page: Page, feed: Feed) {
    LaunchedEffect(feed) {
        snapshotFlow { feed.videos.isNotEmpty() }.first { it }
        if (page.focus == ALL) {
            page.focus = null
            page.needsFocus = true
        }
    }
}

@Composable
private fun ChipRow(content: LazyListScope.() -> Unit) = LazyRow(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp),
    content = content,
)

private const val ALL = "chip:all"

@Composable
fun GridScreen(page: GridPage) {
    VideoGrid(page, page.feed, page.grid, page.empty) {
        fullWidth("head") { PageHead(page.title, page.crumb, page.subtitle) }
    }
}

/** Four columns of video cards that load more as focus nears the end. */
@Composable
private fun VideoGrid(
    page: Page,
    feed: Feed,
    state: LazyGridState,
    empty: String,
    header: LazyGridScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(feed) {
        snapshotFlow { state.layoutInfo.run { (visibleItemsInfo.lastOrNull()?.index ?: 0) to totalItemsCount } }
            .collect { (last, total) -> if (last >= total - 12) feed.more() }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = state,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        header()
        itemsIndexed(feed.videos, key = { _, video -> video.vid }) { i, video ->
            VideoCard(video, Modifier.restoreFocus(page, video.vid, first = i == 0))
        }
        fullWidth("status") {
            val error = feed.error
            when {
                error != null -> Failed(error, retry = { scope.launch { feed.more() } })
                feed.loading -> Status("加载中…")
                feed.done && feed.videos.isEmpty() -> Status(empty)
            }
        }
    }
}

private fun LazyGridScope.fullWidth(key: String, content: @Composable () -> Unit) =
    item(key = "#$key", span = { GridItemSpan(maxLineSpan) }) { content() }
