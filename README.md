# HackerTube for TV

An Android TV app for [HackerTube](https://hackertube.org), the videos that Hacker News recommends. When you select a video, the YouTube app on your TV plays it.

![The Home screen on a TV](docs/home.png)

## 中文版（this fork）

This fork, **HackerTube 中文** (`tv.hackertube.zh`), installs next to the English app and shows HackerTube in Simplified Chinese:

- Video titles, summaries and "why HN recommends it", topic areas, categories, playlists with their intros and notes, and every screen of the app. The details page also shows the original English title.
- The sidebar lists the 16 topic areas, as on [hackertube-zh](https://fjwangyantao.github.io/hackertube-zh/). An area opens with chips to filter by type (演讲, 讲座, 课程…) and sort, and its finer topics. Home has the type chips too.
- Search in Chinese looks in the Chinese titles and summaries; search in English goes to hackertube.org as before.
- Videos open in SmartTube when it is installed (for its auto-translated subtitles), else in YouTube.

The text ships inside the app: `translations/zh/` holds the sources (see `translations/zh/NOTICE.md`), and `python3 -I scripts/build-zh.py` builds `app/src/main/resources/hackertube/zh.json`. Anything without a translation stays in English.

CI (`.github/workflows/build.yml`) builds the APK and walks the app on an Android TV emulator, keeping a screenshot of each screen. For a release build signed with your own key, set `HT_KEYSTORE`, `HT_KEYSTORE_PASSWORD`, `HT_KEY_ALIAS` and `HT_KEY_PASSWORD`.

## Features

The app has the same pages and the same look as hackertube.org:

- Home, with a chip for each topic area
- Search, by words or by meaning
- Longs, Liked, History and Watch later
- Categories and Playlists

To play a video in YouTube, push OK on the video. When the video stops, or when you push Back, the TV shows HackerTube again.

To open the page of a video, hold OK on the video. The page shows the summary, the reason that HN recommends the video, the HN comments, and the next videos. On this page, you can like the video, mark it "Not for me", or save it to Watch later.

![The page of a video](docs/details.png)

![The sidebar](docs/sidebar.png)

## Install on a TV

You must have an Android TV or Google TV with Android 8 or later, and the YouTube app.

1. On the TV, activate the developer options and network debugging.
2. Download `hackertube-tv.apk` from the [latest release](https://github.com/ephraimduncan/hackertube.tv/releases/latest).
3. Connect to the TV: `adb connect <tv-ip>:5555`.
4. On the TV, accept the debugging prompt.
5. Install the app: `adb install hackertube-tv.apk`.

HackerTube then shows in the row of apps on the TV home screen.

## Build

You must have JDK 21 and the Android SDK (platform 37).

```sh
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

The release build uses the debug key. Thus, you can install it on your TV without more setup.

## Website

The `site` folder holds the landing page at [hackertube.tv](https://hackertube.tv). The page is HTML and CSS in `site/public`. The page has no build step.

A Cloudflare Worker (`site/worker.js`) sends the files in `site/public`. The Worker also redirects `www.hackertube.tv` and `http://` addresses to `https://hackertube.tv`.

To deploy the site, use this command in the `site` folder:

```sh
bunx wrangler@latest deploy
```

When you release a new version of the app, replace `site/public/hackertube-tv.apk` with the new APK. Then deploy the site again.

## How it works

The app reads the JSON API of hackertube.org. The website uses the same API: `/api/feed`, `/api/list`, `/api/search`, `/api/video`, `/api/related`, `/api/browse`, `/api/playlists` and `/api/playlist`.

The website keeps your likes, history and Watch later list in the browser. The app keeps them on the TV. As on the website, the Home feed sends them to the API, and the API uses them to select your videos.

To play a video, the app sends a YouTube link to the YouTube app for TV. We wrote the app in Kotlin with [Compose for TV](https://developer.android.com/training/tv/playback/compose).

## Credits

- The videos, summaries and curation come from [hackertube.org](https://hackertube.org). This project is not part of HackerTube.
- The icons are [Material Symbols](https://github.com/google/material-design-icons) by Google, under the Apache License 2.0.

## License

[MIT](LICENSE)
