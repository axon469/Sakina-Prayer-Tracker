# Sakina — Prayer Tracker

A simple, private daily prayer tracker. Mark each of the five prayers as **Completed** or **Missed**, and watch your streak grow.

Built with plain HTML, CSS and JavaScript. One file. No backend, no accounts, no frameworks, no libraries.

> **Phase 1** — foundation, navigation and the daily tracking dashboard.

## Features

- Home dashboard with today's date and daily progress
- Daily focus card with a short Islamic reminder (Quran or hadith, source shown)
- Five prayers: Fajr, Dhuhr, Asr, Maghrib, Isha
- Mark each prayer as Completed or Missed (tap again to clear)
- Weekly activity strip. Tap a past day to log or fix it
- Daily progress ring
- This week: completed, missed, completion rate, full days
- Prayer streak counter
- Statistics page: all-time totals, current and best streak, per-prayer bars, 12-week activity map
- Profile page: name, week start (Mon or Sun), reset data
- Bottom floating navigation with a large central **Log prayers** button
- Mobile-first, dark UI, works offline

## What it does not do

- It does **not** show prayer times.
- It does **not** detect prayers automatically.
- It does **not** send your data anywhere.

You tell the app what you prayed. That is all.

## Your data

Everything is saved in your browser with `localStorage` (key: `sakina:v1`).

- Data stays after you refresh or close the page.
- Data is per browser and per device.
- Clearing browser data will delete it.

## How numbers are counted

- **Rate** = completed ÷ (completed + missed). Only prayers you marked are counted. Unmarked prayers are not counted as missed.
- **Streak** = days in a row with all five prayers completed. If today is not finished yet, your streak is not broken.
- You cannot log future days.

## Run it

Open `index.html` in any modern browser. That is all.

On Android with KSWEB: copy the files to your `htdocs` folder and open `http://localhost:8080/` (or your KSWEB port).
In an AIDE WebView, make sure DOM storage is enabled, or data will not be saved.


## Project structure

```
index.html   The whole app (HTML + CSS + JS)
README.md    This file
```

Inside `index.html` the JavaScript is split into clear parts: storage, tracking logic, small components (`PrayerRow`, `WeekStrip`, `Ring`, `Nav`, sheets), views (`HomeView`, `StatsView`, `ProfileView`), and event handling.

## Notes

- Reminder texts are short English renderings. The source is shown under each one. Please check them against a trusted translation if you plan to publish.
- Not affiliated with any organization.
