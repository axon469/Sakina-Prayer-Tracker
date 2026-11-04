 <div align="center">

<img src="assets/logo.svg" alt="Sakina logo" width="120" height="120">

# Sakina

**A calm, private daily prayer tracker.**<br>
Mark your five daily prayers, build your streak, and see your progress over time.

![Version](https://img.shields.io/badge/version-1.1.0-cf9f86?style=for-the-badge)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![No dependencies](https://img.shields.io/badge/dependencies-none-1b1b1d?style=for-the-badge)
![Data stays on device](https://img.shields.io/badge/data-stays%20on%20device-1b1b1d?style=for-the-badge)

[About](#about) · [Screenshots](#screenshots) · [Features](#features) · [How it works](#how-it-works) · [Backup and import](#backup-and-import) · [This repository](#this-repository)

</div>

---

## About

Sakina (Arabic for *calm* and *tranquility*) is a simple, mobile-first prayer tracker for keeping a personal record of the five daily prayers:

**Fajr, Dhuhr, Asr, Maghrib and Isha.**

Sakina is designed to be calm, lightweight and easy to use. There are no accounts to create and no backend service required for tracking your prayers.

The project currently has two ways to use it:

- **Website:** Open Sakina in a modern web browser.
- **Android app:** Install the Android application from the GitHub Releases page. The current Android version displays the website through a WebView and requires an internet connection to load it.

A fully local Android version, with the website bundled inside the APK, is planned for a future update. This will allow the app interface to work without relying on the hosted website.

Sakina does not currently calculate prayer times or automatically detect your location. You manually mark your prayers as completed.

## Screenshots

<div align="center">
<table>
  <tr>
    <td align="center"><img src="screenshots/home.png" width="210" alt="Home"><br><sub><b>Home</b></sub></td>
    <td align="center"><img src="screenshots/statistics.png" width="210" alt="Statistics"><br><sub><b>Statistics</b></sub></td>
    <td align="center"><img src="screenshots/history.png" width="210" alt="History"><br><sub><b>History</b></sub></td>
    <td align="center"><img src="screenshots/settings.png" width="210" alt="Settings"><br><sub><b>Settings</b></sub></td>
  </tr>
</table>
<sub>Screenshots use sample data.</sub>
</div>

## Features

### Home

- Today's date and prayer progress ring.
- Current streak counter.
- Daily focus card with a short Quran or hadith reminder and its source.
- One-tap controls to mark each prayer as completed.
- Weekly activity strip. Select a day to view or correct its records.
- Weekly summary showing completed prayers, missed prayers, completion rate and full days.
- Quick prayer logging button available from the main pages.

### Statistics

- Overall prayer completion rate.
- Current and longest streaks.
- Perfect-day count.
- Totals for completed prayers, missed prayers and tracked days.
- Full-year activity map.
- Year selector to review previous years.
- Activity colors representing zero to five completed prayers per day.

### History

- Individual summaries for all five prayers.
- Time-range filtering.
- Daily cards showing the status of all five prayers.
- Ability to correct prayer records from previous days.

### Settings

- Personal name setting.
- Week-start preference: Monday or Sunday.
- Export and import of backup files.
- Option to reset stored data.

## How it works

| Term | Meaning |
| --- | --- |
| Completed | A prayer you manually marked as completed. |
| Pending | A prayer for today that has not been marked yet. |
| Missed | A prayer that remains incomplete after its day has ended. |
| Completion rate | Completed prayers divided by completed plus missed prayers. Pending prayers are excluded. |
| Streak | Consecutive days on which all five prayers were completed. An unfinished current day does not break the streak. |
| Start date | Tracking begins on the first day you use Sakina, or the earliest date included in an imported backup. |

You cannot log future days.

Sakina is a personal tracking tool. It does not verify whether a prayer was performed and does not replace prayer-time guidance.

## Backup and import

The website stores prayer records in browser `localStorage` using the key:

` sakina:v1 `

Your records remain in that browser when you refresh or close the page. Clearing browser data can remove them, so export a backup periodically.

The Settings page provides backup export and import.

Importing a backup adds its prayer records to your history. Years present in the imported records can also appear in the Statistics year selector.

### Backup file format

Backups use JSON format:

```json
{
  "app": "sakina",
  "start": "2021-03-01",
  "days": {
    "2021-03-01": ["fajr", "dhuhr", "asr", "maghrib", "isha"],
    "2021-03-02": ["fajr", "isha"]
  }
}
```

Prayer IDs:

- `fajr`
- `dhuhr`
- `asr`
- `maghrib`
- `isha`

Dates use the `YYYY-MM-DD` format. Future dates are ignored during import.

**Important:** Browser storage and Android app storage are not automatically the same. Keep an exported backup if you need to move your records between browsers or devices.

## Android app

The Android application is built around the Sakina website.

Current Android version: **1.1.0**

- Application name: Sakina Prayer Tracker
- Package ID: `org.axon.sakina`
- Update checking: Checks GitHub for available app updates when the app opens and an internet connection is available.
- Current website loading: Uses the hosted Sakina website.

### Planned improvements

The following improvements are planned for future Android updates:

- Bundle the complete website inside the APK for offline access.
- Add location-based features and time-zone support.
- Improve native Android data storage.
- Add an Android-focused backup and restore experience.

These are planned features and are not part of the current 1.1.0 release.

## Installation

### Website

1. Visit the [Sakina website](https://axon469.github.io/Sakina-Prayer-Tracker/).
2. Open it in a modern browser.
3. Start tracking your prayers.

### Android

1. Open the [GitHub Releases page](https://github.com/axon469/Sakina-Prayer-Tracker/releases).
2. Open the latest release.
3. Download the APK file.
4. Install it on your Android device.

Android may ask you to allow installation from your browser or file manager. Only install APKs from a source you trust.

## This repository

The repository contains the website source, Android application project, build workflow and supporting images.

| Path | Purpose |
| --- | --- |
| `index.html` | Website source: HTML, CSS and JavaScript. |
| `app/` | Native Android application project. |
| `.github/workflows/` | GitHub Actions build workflow. |
| `update.json` | Information used by the Android update checker. |
| `assets/` | Website logo and other assets. |
| `screenshots/` | Images used in this README. |
| `README.md` | Project documentation. |
| `LICENSE` | Project license. |

### Run the website locally

1. Download or clone this repository.
2. Open `index.html` in a modern web browser.
3. Start using Sakina.

No web server or package installation is required for the basic website.

## Privacy

Sakina is designed to keep prayer tracking simple and personal.

- No account or registration is required.
- The website stores prayer records in browser storage.
- The prayer-tracking feature does not require a backend database.
- The current Android app needs internet access to load the hosted website and check for updates.
- The fully offline Android experience is planned for a future release.

Remember that browser data can be lost if you clear the browser's stored data. Export a backup periodically.

## License

Sakina is open-source software licensed under the **MIT License**.

You are free to use, copy, modify, merge, publish, distribute, sublicense and sell copies of the software, subject to the terms of the license.

See the [LICENSE](LICENSE) file for the complete license text.

---

<div align="center">
  <sub>Made with care. May Sakina bring a little more calm and consistency to your daily routine.</sub>
</div>
