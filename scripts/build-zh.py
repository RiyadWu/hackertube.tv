#!/usr/bin/env python3
"""Builds the app's Chinese resource from translations/zh/ (sources in translations/zh/NOTICE.md).

titles.jsonl         {"vid", "en", "zh"} per video: edit "zh" to fix a title
summaries.jsonl      {"vid", "summary", "why"} per video
areas.json           area id -> name
categories.json      category id -> name
playlists.json       playlist slug -> title
playlist_text.json   "intro:<slug>" and "note:<slug>/<vid>" -> text

Writes app/src/main/resources/hackertube/zh.json, read at runtime by Zh.kt.
Usage: python3 -I scripts/build-zh.py
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "translations", "zh")
OUT = os.path.join(ROOT, "app", "src", "main", "resources", "hackertube", "zh.json")


def lines(name):
    with open(os.path.join(SRC, name), encoding="utf-8") as f:
        for n, line in enumerate(f, 1):
            if line.strip():
                yield n, json.loads(line)


def load(name):
    with open(os.path.join(SRC, name), encoding="utf-8") as f:
        return json.load(f)


def main():
    titles = {}
    for n, row in lines("titles.jsonl"):
        zh = (row.get("zh") or "").strip()
        if not row.get("vid") or not zh:
            raise SystemExit(f"titles.jsonl:{n}: missing vid or zh")
        titles[row["vid"]] = zh
    summaries, whys = {}, {}
    for _, row in lines("summaries.jsonl"):
        if row.get("summary"):
            summaries[row["vid"]] = row["summary"].strip()
        if row.get("why"):
            whys[row["vid"]] = row["why"].strip()

    data = {
        "titles": titles,
        "summaries": summaries,
        "whys": whys,
        "areas": load("areas.json"),
        "categories": load("categories.json"),
        "playlists": load("playlists.json"),
        "playlistText": load("playlist_text.json"),
    }
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"), sort_keys=True)
        f.write("\n")
    print(OUT + ": " + ", ".join(f"{len(v)} {k}" for k, v in data.items()))


if __name__ == "__main__":
    main()
