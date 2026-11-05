# Permissions Practice - Android Jetpack Compose

This repository contains the implementation of the **Permissions Practice** mini-project built with Kotlin and Jetpack Compose for Android 14+ (API 34+).

---

## 📱 Features

The application consists of a main **Settings Screen** where users can manage permissions for three key device features:

1. **Camera Item (`CAMERA`)**
   - Launches permission request dialog when selected.
   - **If Granted:** Navigates to the **Camera Feature Screen** featuring photo capture preview and controls.
   - **If Denied:** Displays a `Snackbar` indicating permission is denied and showing the remaining `N` tries left (`"Permission denied, you've N amount of tries left."`).
   - **If Tries Exhausted (`N = 0`):** Automatically navigates the user to the system **App Settings** screen (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).

2. **Geolocation Item (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)**
   - Launches location permission request dialog.
   - **If Granted:** Navigates to the **Geolocation Feature Screen** displaying latitude, longitude, accuracy, and map view.
   - **If Denied:** Displays `Snackbar` showing remaining `N` tries left.
   - **If Tries Exhausted:** Navigates user to system App Settings.

3. **Photos / Videos Item (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_VISUAL_USER_SELECTED` for API 34+)**
   - Handles media permission requests tailored for Android 14+ (API 34+).
   - **If Granted:** Navigates to the **Media Feature Screen** containing buttons to select photos/videos and display selected media inside a dedicated display box.
   - **If Denied:** Displays `Snackbar` with remaining tries count.
   - **If Tries Exhausted:** Navigates user to system App Settings.

---

## 🎥 Video Demonstration (Submission requirement)

<details>
<summary>▶ Click to expand 40-second video walkthrough</summary>

*(Replace this placeholder link with your recorded video file / GIF when submitting)*
![Permissions Demo](https://user-images.githubusercontent.com/placeholder/demo.mp4)

</details>

---

## 💻 Code Explanation: How Permissions Are Handled

Below is the code block explaining how permissions, remaining tries count, system dialog launchers, resume lifecycle re-checks, and app settings navigation are implemented in this repository.

```kotlin
// SettingsScreen.kt - Permission Request & Retry Logic
const val MAX_TRIES = 2

@Composable
fun SettingsScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToMedia: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State tracking granted status & remaining tries count
    var cameraGranted by remember {
        mutableStateOf(PermissionUtils.isPermissionGranted(context, Manifest.permission.CAMERA))
    }
    var cameraTries by remember { mutableIntStateOf(MAX_TRIES) }

    // Launcher using Activity Result API
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraGranted = isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Camera permission granted!")
            }
            onNavigateToCamera()
        } else {
            cameraTries = (cameraTries - 1).coerceAtLeast(0)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permission denied, you've $cameraTries amount of tries left.")
            }
        }
    }

    // Re-check permissions on ON_RESUME when user returns from System Settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val updatedCamera = PermissionUtils.isPermissionGranted(context, Manifest.permission.CAMERA)
        if (updatedCamera) {
            cameraGranted = true
            cameraTries = MAX_TRIES
        }
    }

    // Handling item clicks
    PermissionItemCard(
        title = "Camera item",
        isGranted = cameraGranted,
        triesLeft = cameraTries,
        onClick = {
            if (PermissionUtils.isPermissionGranted(context, Manifest.permission.CAMERA)) {
                onNavigateToCamera()
            } else if (cameraTries > 0) {
                cameraLauncher.launch(Manifest.permission.CAMERA)
            } else {
                // Tries exhausted: Navigate user to Settings screen
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Tries exhausted. Navigating to Settings...")
                }
                PermissionUtils.openAppSettings(context)
            }
        }
    )
}
```

### Explanation:
1. **Activity Result API (`rememberLauncherForActivityResult`)**:
   Instead of legacy `onRequestPermissionsResult`, Compose uses `rememberLauncherForActivityResult` with contracts like `RequestPermission` and `RequestMultiplePermissions`.

2. **Retry Counter (`triesLeft`)**:
   When the user denies a permission prompt, the counter decreases. A `Snackbar` is shown immediately telling the user how many attempts remain.

3. **System Settings Intent (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`)**:
   Once remaining attempts reach `0`, subsequent clicks invoke `PermissionUtils.openAppSettings(context)`, launching the system settings page for the app package so the user can grant the permission manually.

4. **Lifecycle Resumption (`LifecycleEventEffect(Lifecycle.Event.ON_RESUME)`)**:
   When the user grants a permission in system settings and returns to the app, `ON_RESUME` re-evaluates the permission state so the UI automatically updates without requiring an app restart.

---

## 🛠 Project Build & Setup

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Min SDK:** 35 (Target SDK 37)
- **IDE:** Android Studio
