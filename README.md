# PersistentNotificationsApp

An Android app that turns short notes into persistent notifications. Each note stays in the notification shade until it is deleted. The user-facing app name is **QuickNote**.

## Features

- **Create and edit notes** in a single activity. The editor has a text field with **Save** and **Cancel** buttons.
    - A new note can be saved once it contains non-blank text.
    - An existing note can be saved only after its text has been edited.
- **One notification per note.** Tapping a notification opens the editor with the note's text filled in.
- **Delete from the notification.** An expanded notification shows a text-only **Delete** action that removes the note without opening the app. It also works from the lock screen.
- **Persistence.**
    - Notifications are ongoing and not auto-cancelled.
    - On Android 14 and later, ongoing notifications can still be swiped away, so a swiped note is re-posted automatically while it exists.
    - All saved notes are re-posted after a reboot or an app update.
- **Lock screen.** Notes use public visibility, so their full text is shown on the lock screen.
- **Quick Settings tile.** A "New note" tile opens a blank editor. Large tiles show "QuickNote" and "New note"; small tiles show only the icon. The tile is added manually through the Quick Settings edit menu.
- **Modern styling.** Jetpack Compose with Material 3, dynamic color (Material You), and edge-to-edge layout.

## Requirements

| Item | Version |
| --- | --- |
| Minimum Android | 13 (API 33) |
| Compile / target SDK | 36 |
| Gradle | 9.6.0 |
| Android Gradle Plugin | 9.5.1 |
| Kotlin | 2.2.20 (AGP built-in Kotlin) |
| Compose BOM | 2025.09.01 |
| JDK | 17 or newer |

Versions are pinned in `gradle/libs.versions.toml` and `gradle/wrapper/gradle-wrapper.properties`.

## Build and run

1. Open the project folder in Android Studio and let Gradle sync finish.
2. Select the `app` run configuration (module `PersistentNotificationsApp.app`).
3. Choose a device or emulator running Android 13 or newer and run.

From a terminal, `./gradlew assembleDebug` builds a debug APK.

On first launch the app requests the notification permission. If notifications are disabled when a note is saved, the note is still stored and a toast explains why nothing appears.

## Project structure

```
persistent-notifications-app/
├── .gitignore
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       └── gradle-wrapper.properties
└── app/
    ├── .gitignore
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/github/csande/persistentnotificationsapp/
        │   ├── MainActivity.kt
        │   ├── EditorViewModel.kt
        │   ├── data/
        │   │   └── NoteRepository.kt
        │   ├── notification/
        │   │   ├── BootReceiver.kt
        │   │   ├── DeleteNoteReceiver.kt
        │   │   ├── NotificationDismissedReceiver.kt
        │   │   └── NotificationHelper.kt
        │   ├── tile/
        │   │   └── NewNoteTileService.kt
        │   └── ui/
        │       ├── EditorScreen.kt
        │       └── theme/
        │           └── Theme.kt
        └── res/
            ├── drawable/
            │   ├── ic_launcher_foreground.xml
            │   ├── ic_notification.xml
            │   └── ic_tile.xml
            ├── mipmap-anydpi/
            │   ├── ic_launcher.xml
            │   └── ic_launcher_round.xml
            ├── values/
            │   ├── colors.xml
            │   ├── strings.xml
            │   └── themes.xml
            └── values-night/
                └── themes.xml
```

## How it works

| Component | Role |
| --- | --- |
| `MainActivity` | The only activity. Opened from the launcher or the tile, it creates a note. Opened from a notification, it edits that note (the note ID is passed as an intent extra). |
| `EditorViewModel` | Holds the editor state, decides whether Save is enabled, and persists state through `SavedStateHandle` across rotation and process death. |
| `NoteRepository` | Stores notes as a JSON array in SharedPreferences. A note's ID doubles as its notification ID. |
| `NotificationHelper` | Creates the notification channel and posts, updates, or cancels note notifications. |
| `BootReceiver` | Re-posts all notes after a reboot or app update. |
| `NotificationDismissedReceiver` | Re-posts a note if the system lets the user swipe its notification away. |
| `DeleteNoteReceiver` | Handles the notification's Delete action: removes the note from storage, then cancels its notification. |
| `NewNoteTileService` | The Quick Settings tile. |

Notification settings, in `NotificationHelper`:

- Low-importance channel, so there is no sound, vibration, or heads-up pop-up.
- Ongoing, not auto-cancelled, and alerts only once.
- Public lock-screen visibility.
- A `BigTextStyle` so the full text shows when expanded.
- A delete intent for re-posting after a swipe.
- A text-only Delete action that does not require authentication.

There is no foreground service, `WorkManager`, alarm, or wake lock. Code runs only on boot or update, on a swipe, on a Delete tap, and while the editor is open. The tile service is bound by the system only while the Quick Settings panel is visible.

## Customization

- **App name:** change `app_name` in `app/src/main/res/values/strings.xml`. It is also used for the tile's first line.
- **Package and application ID:** `com.github.csande.persistentnotificationsapp`, set in `app/build.gradle.kts` (`namespace` and `applicationId`) and in the Kotlin package directories.
- **Tile subtitle:** `tile_subtitle` in `strings.xml`.

## Known behavior and limitations

- **Note length.** No limit is enforced by the app. Android truncates notification text, and recent versions are believed to cut at around 1,024 characters (5,120 in older versions; this value is an internal platform constant and was not verified on a device). The full text remains in storage and in the editor.
- **Expanded or collapsed state.** The system and the user control it. There is no API to force a notification to stay collapsed.
- **Lock screen.** The full note text is visible, and Delete works without unlocking. Anyone holding the locked phone can read and delete notes.
- **Shade after Delete.** The Delete action is a plain broadcast, so the shade is expected to stay open. This depends on the Android version and device.
- **Delete action color.** The action text uses the system default. A red color span was tried and ignored by the system on the tested emulator and phone.
- **Swiped notifications.** On Android 14 and later, a swiped note reappears after a brief moment.
- **Force stop.** Force-stopping the app removes its notifications until the next reboot or app update.
- **Tile behavior.** Tapping the tile restarts the editor, so unsaved text in an open editor is discarded.
- **Cancel.** Cancel closes the editor and discards unsaved changes without confirmation.

## Development notes

- **AGP and Gradle.** AGP 8.13 fails to sync on Gradle 9.6.0 because it uses a Gradle internal API that was removed in that release. AGP 9.5.x requires Gradle 9.6.0 as a minimum, which is why this project uses 9.5.1.
- **Built-in Kotlin.** AGP 9 compiles Kotlin itself, so the `org.jetbrains.kotlin.android` plugin must not be applied. Only the Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) remains, and its version determines the Kotlin version.
- **Window theme.** `android:Theme.Material.DayNight.NoActionBar` does not exist in the platform. The app uses `android:Theme.Material.Light.NoActionBar` in `values/` and `android:Theme.Material.NoActionBar` in `values-night/`.
- **Property named `field`.** `EditorViewModel` exposes a property named `field`. Inside a property getter, a bare `field` refers to the accessor's backing field, so `canSave` uses `this.field`.

## Possible future work

These items are not implemented.

- **Undo after delete.** Replace the note's notification in place with a short-lived "Note deleted" notification that has an Undo action. The action would carry the note's ID and text so a receiver can restore the note, and the system would remove the notification after a timeout set with `setTimeoutAfter`. This needs a `NoteRepository.restore(note)` method that preserves the original ID.
- **Maximum note length** matching the notification truncation limit.
- **Optional lock-screen privacy**, such as a redacted public version of the notification or a setting that toggles visibility.
