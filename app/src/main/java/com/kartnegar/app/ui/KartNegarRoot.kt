package com.kartnegar.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartnegar.app.ui.screens.ScanScreen
import com.kartnegar.app.ui.screens.ToolsPrivacyScreen
import com.kartnegar.app.ui.screens.VaultScreen
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KartNegarRoot(
    viewModel: MainViewModel,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val savedItems by viewModel.savedItems.collectAsStateWithLifecycle()
    val manualInput by viewModel.manualInput.collectAsStateWithLifecycle()
    val manualResult by viewModel.manualResult.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedTab = 0
            viewModel.scanImageUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            selectedTab = 0
            viewModel.scanImageUri(uri)
        }
    }

    LaunchedEffect(scanState.statusBanner) {
        val msg = scanState.statusBanner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeStatusBanner()
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("KartNegar", text))
        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }

    fun launchCameraCapture() {
        val capturesDir = File(context.cacheDir, "captures").apply { mkdirs() }
        val photoFile = File(capturesDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val savedNormalizedSet = remember(savedItems) {
        savedItems.map { it.normalizedValue }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "KartNegar • کارت‌نگار"
                            1 -> "Local Vault • ذخیره‌شده"
                            else -> "Tools & Privacy"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.DocumentScanner, contentDescription = "Scan") },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.Bookmark, contentDescription = "Vault") },
                    label = { Text("Vault (${savedItems.size})") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.VerifiedUser, contentDescription = "Tools") },
                    label = { Text("Tools") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ScanScreen(
                    scanState = scanState,
                    savedNormalizedValues = savedNormalizedSet,
                    onPickImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onTakePhoto = { launchCameraCapture() },
                    onPasteText = { text -> viewModel.scanRawText(text) },
                    onClear = { viewModel.clearScanResult() },
                    onCopyValue = { copyToClipboard(it) },
                    onShareValue = { shareText(it) },
                    onSaveItem = { item, label -> viewModel.saveItem(item, label) }
                )

                1 -> VaultScreen(
                    items = savedItems,
                    onCopyValue = { copyToClipboard(it) },
                    onShareValue = { shareText(it) },
                    onUpdateLabel = { id, label -> viewModel.updateSavedLabel(id, label) },
                    onDeleteItem = { item -> viewModel.deleteSavedItem(item) }
                )

                else -> ToolsPrivacyScreen(
                    manualInput = manualInput,
                    manualResult = manualResult,
                    savedCount = savedItems.size,
                    onManualInputChange = { viewModel.updateManualInput(it) },
                    onCopyValue = { copyToClipboard(it) },
                    onClearAllVault = {
                        viewModel.clearAllSavedItems()
                        scope.launch { snackbarHostState.showSnackbar("Local vault cleared") }
                    }
                )
            }
        }
    }
}
