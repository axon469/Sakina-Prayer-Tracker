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

Sakina (Arabic for *calm* and *tranquility*) is a mobile-first website for tracking the five daily prayers: **Fajr, Dhuhr, Asr, Maghrib and Isha**.

It is one HTML file. There is no backend, no account, no framework and no library. Your records are saved in your own browser and never leave your device.

Sakina does not show prayer times and does not detect prayers on its own. You tell it what you prayed. That is all.

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

**Home**
- Today's date, prayer progress ring and streak counter
- Daily focus card with a short reminder from the Quran or hadith, with its source
- One tap to mark each prayer as completed
- Weekly activity strip. Tap a day to view or fix it
- This week: completed, missed, completion rate and full days
- Large center button for quick logging from any page

**Statistics**
- Overall completion ring, current streak, longest streak and perfect days
- Totals for done, missed, days tracked and perfect days
- Full-year activity map with a year dropdown. Colors show 0 to 5 prayers per day, and missed days have their own color

**History**
- Prayer summary for each of the five prayers, with a time range filter
- A detailed card for every day with all five prayers
- Tap any prayer on any past day to correct it

**Settings**
- Name, week start (Monday or Sunday)
- Export and import a backup file
- Reset all data

## How it works

| Rule | Meaning |
| --- | --- |
| Completed | You tapped the prayer. This is the only thing you mark. |
| Missed | A prayer not completed once the day has ended. This is automatic. |
| Completion rate | Completed ÷ (completed + missed)
| Streak | Days in a row with all five prayers completed. An unfinished today does not break it. |
| Start date | Counting begins on the day you first use Sakina, or on the earliest day in an imported backup. |

You cannot log future days.


```
.
├── index.html          The whole website (HTML, CSS and JavaScript)
├── README.md           This file
├── assets/
│   ├── logo.svg        Logo (vector)
│   └── logo.png        Logo (1024 × 1024, transparent)
└── screenshots/        Images used in this README
```
