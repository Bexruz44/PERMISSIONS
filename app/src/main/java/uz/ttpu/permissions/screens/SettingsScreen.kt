package uz.ttpu.permissions.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import uz.ttpu.permissions.utils.PermissionUtils

const val MAX_TRIES = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToMedia: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Camera state & launcher
    var cameraGranted by remember {
        mutableStateOf(PermissionUtils.isPermissionGranted(context, Manifest.permission.CAMERA))
    }
    var cameraTries by remember { mutableIntStateOf(MAX_TRIES) }

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

    // 2. Geolocation state & launcher
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var locationGranted by remember {
        mutableStateOf(PermissionUtils.areAllPermissionsGranted(context, locationPermissions))
    }
    var locationTries by remember { mutableIntStateOf(MAX_TRIES) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val isGranted = result.values.any { it }
        locationGranted = isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Geolocation permission granted!")
            }
            onNavigateToLocation()
        } else {
            locationTries = (locationTries - 1).coerceAtLeast(0)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permission is denied, you've $locationTries amount of tries left.")
            }
        }
    }

    // 3. Media state & launcher (API 34+)
    val mediaPermissions = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )

    var mediaGranted by remember {
        mutableStateOf(PermissionUtils.areAllPermissionsGranted(context, mediaPermissions))
    }
    var mediaTries by remember { mutableIntStateOf(MAX_TRIES) }

    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val isGranted = result.values.any { it }
        mediaGranted = isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Photos/Videos permission granted!")
            }
            onNavigateToMedia()
        } else {
            mediaTries = (mediaTries - 1).coerceAtLeast(0)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permission is denied, you've $mediaTries amount of tries left.")
            }
        }
    }

    // Re-check permissions when returning to app (e.g., from Settings)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val updatedCamera = PermissionUtils.isPermissionGranted(context, Manifest.permission.CAMERA)
        val updatedLocation = PermissionUtils.areAllPermissionsGranted(context, locationPermissions)
        val updatedMedia = PermissionUtils.areAllPermissionsGranted(context, mediaPermissions)

        if (updatedCamera) {
            cameraGranted = true
            cameraTries = MAX_TRIES
        }
        if (updatedLocation) {
            locationGranted = true
            locationTries = MAX_TRIES
        }
        if (updatedMedia) {
            mediaGranted = true
            mediaTries = MAX_TRIES
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions Manager") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Permissions Settings Screen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any item below to manage permissions or launch the feature.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 1. Camera Item
            PermissionItemCard(
                title = "Camera item",
                subtitle = "Camera permission",
                isGranted = cameraGranted,
                triesLeft = cameraTries,
                icon = Icons.Default.CameraAlt,
                onClick = {
                    val isNowGranted = PermissionUtils.isPermissionGranted(context, Manifest.permission.CAMERA)
                    if (isNowGranted) {
                        onNavigateToCamera()
                    } else if (cameraTries > 0) {
                        cameraLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Tries exhausted. Navigating to Settings...")
                        }
                        PermissionUtils.openAppSettings(context)
                    }
                }
            )

            // 2. Geolocation Item
            PermissionItemCard(
                title = "Geolocation item",
                subtitle = "Location | Fine & Coarse Location",
                isGranted = locationGranted,
                triesLeft = locationTries,
                icon = Icons.Default.LocationOn,
                onClick = {
                    val isNowGranted = PermissionUtils.areAllPermissionsGranted(context, locationPermissions)
                    if (isNowGranted) {
                        onNavigateToLocation()
                    } else if (locationTries > 0) {
                        locationLauncher.launch(locationPermissions)
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Tries exhausted. Navigating to Settings...")
                        }
                        PermissionUtils.openAppSettings(context)
                    }
                }
            )

            // 3. Photos/Videos Item (Media API 34+)
            PermissionItemCard(
                title = "Photos/Videos item",
                subtitle = "Media permission (API 34+)",
                isGranted = mediaGranted,
                triesLeft = mediaTries,
                icon = Icons.Default.PhotoLibrary,
                onClick = {
                    val isNowGranted = PermissionUtils.areAllPermissionsGranted(context, mediaPermissions)
                    if (isNowGranted) {
                        onNavigateToMedia()
                    } else if (mediaTries > 0) {
                        mediaLauncher.launch(mediaPermissions)
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Tries exhausted. Navigating to Settings...")
                        }
                        PermissionUtils.openAppSettings(context)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Open System Settings Direct Action Card
            OutlinedCard(
                onClick = { PermissionUtils.openAppSettings(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Open System App Settings",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    triesLeft: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isGranted) {
                    Text(
                        text = "Status: Granted (Tap to open)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = if (triesLeft > 0) "Status: Denied ($triesLeft tries left)" else "Status: Tries exhausted (Tap for Settings)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Icon(
                imageVector = if (isGranted) Icons.Default.ChevronRight else Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
