package com.example.coursework.presentation.files

import android.app.*
import android.content.*
import android.net.*
import android.os.*
import android.webkit.*
import android.widget.*
import androidx.activity.compose.*
import androidx.activity.result.contract.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.*
import com.example.coursework.data.remote.*
import com.example.coursework.data.remote.model.*
import com.example.coursework.data.storage.*
import com.google.accompanist.swiperefresh.*
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {

    val ctx = LocalContext.current
    val vm: FilesViewModel =
        viewModel(factory = FilesViewModelFactory(ctx.applicationContext as Application))

    val files by vm.files.collectAsState()
    val path by vm.currentPath.collectAsState()
    val busy by vm.loading.collectAsState()
    val sortMode by vm.sortMode.collectAsState()

    var sortMenu by remember { mutableStateOf(false) }

    var dlgAccounts by remember { mutableStateOf(false) }
    var dlgCreateMenu by remember { mutableStateOf(false) }
    var dlgNewFolder by remember { mutableStateOf(false) }
    var dlgRename by remember { mutableStateOf(false) }
    var dlgMove by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFileName by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<DiskFile?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { vm.uploadLocal(it, ctx.contentResolver) }
    }

    val tokenStore = remember { TokenDataStore(ctx) }
    val allTokens by tokenStore.allTokens.collectAsState(initial = emptySet())
    val selectedToken by tokenStore.selectedToken.collectAsState(initial = null)

    var askFileName by remember { mutableStateOf(false) }
    var pendingAsset by remember { mutableStateOf("") }
    var pendingExt by remember { mutableStateOf("") }
    var fileBaseName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Файлы Яндекс.Диска") },
                navigationIcon = {
                    if (path != "disk:/") {
                        IconButton(onClick = {
                            vm.load(path.removeSuffix("/").substringBeforeLast("/") + "/")
                            selectedFile = null
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenu,
                            onDismissRequest = { sortMenu = false }
                        ) {
                            SortMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label) },
                                    onClick = {
                                        vm.setSort(mode)
                                        sortMenu = false
                                    },
                                    trailingIcon = {
                                        if (mode == sortMode) Icon(Icons.Default.Check, null)
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { dlgAccounts = true }) {
                        Icon(Icons.Default.Person, null)
                    }
                    IconButton(onClick = onToggleTheme) {
                        Text(if (isDarkTheme) "☀️" else "🌙")
                    }
                }
            )
        },

        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (selectedFile == null) {
                    FloatingActionButton(
                        onClick = { dlgCreateMenu = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.Default.Add, null) }
                }

                selectedFile?.let { f ->

                    FloatingActionButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val href = ApiClient.create(ctx)
                                        .getDownloadLink("${path.removeSuffix("/")}/${f.name}")
                                        .href
                                    downloadFile(ctx, href, f.name)
                                } catch (_: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(ctx, "Ошибка скачивания", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.Rounded.Download, null) }

                    FloatingActionButton(
                        onClick = {
                            newFileName = f.name
                            dlgRename = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.Default.Edit, null) }

                    FloatingActionButton(
                        onClick = { dlgMove = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) }

                    FloatingActionButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch { vm.delete(f.name) }
                            selectedFile = null
                        },
                        containerColor = Color(0xFF9E9E9E),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.Default.Delete, null) }
                }
            }
        }
    ) { pad ->

        SwipeRefresh(
            state = rememberSwipeRefreshState(busy),
            onRefresh = { vm.load() },
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .pointerInput(selectedFile) {
                    detectTapGestures { selectedFile = null }
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { f ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    selectedFile = null
                                    if (f.type == "dir")
                                        vm.load("${path.removeSuffix("/")}/${f.name}")
                                    else
                                        openFileSmart(ctx, f, path)
                                },
                                onLongClick = { selectedFile = f }
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                "${getFileIcon(f)}  ${f.name}",
                                fontWeight = FontWeight.SemiBold
                            )
                            if (f.type != "dir") {
                                Text(
                                    "${formatSize(f.size)} • ${f.type}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (dlgCreateMenu) {
        AlertDialog(
            onDismissRequest = { dlgCreateMenu = false },
            title = {
                Text(
                    "Создать",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    val btnColors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    val btnShape = RoundedCornerShape(20.dp)
                    val btnMod = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)

                    ElevatedButton(
                        onClick = { dlgCreateMenu = false; dlgNewFolder = true },
                        colors = btnColors, shape = btnShape, modifier = btnMod
                    ) { Text("Папку") }

                    listOf(
                        "Текстовый (.txt)" to ("empty.txt" to ".txt"),
                        "Документ (.docx)" to ("empty.docx" to ".docx"),
                        "Таблицу  (.xlsx)" to ("empty.xlsx" to ".xlsx"),
                        "Презентацию (.pptx)" to ("empty.pptx" to ".pptx")
                    ).forEach { (title, pair) ->
                        ElevatedButton(
                            onClick = {
                                dlgCreateMenu = false
                                pendingAsset = pair.first
                                pendingExt = pair.second
                                fileBaseName = ""
                                askFileName = true
                            },
                            colors = btnColors, shape = btnShape, modifier = btnMod
                        ) { Text(title) }
                    }

                    ElevatedButton(
                        onClick = { dlgCreateMenu = false; pickLauncher.launch(arrayOf("*/*")) },
                        colors = btnColors, shape = btnShape, modifier = btnMod
                    ) { Text("Загрузить с устройства") }
                }
            },
            confirmButton = {}
        )
    }

    if (dlgNewFolder) {
        AlertDialog(
            onDismissRequest = { dlgNewFolder = false },
            title = {
                Text(
                    "Новая папка",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    singleLine = true, label = { Text("Имя") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderName.isNotBlank())
                        vm.createFolder(newFolderName.trim())
                    newFolderName = ""; dlgNewFolder = false
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { dlgNewFolder = false }) { Text("Отмена") } }
        )
    }

    if (askFileName) {
        AlertDialog(
            onDismissRequest = { askFileName = false },
            title = {
                Text(
                    "Введите имя",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                OutlinedTextField(
                    value = fileBaseName,
                    onValueChange = { fileBaseName = it },
                    singleLine = true, label = { Text("Без расширения") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (fileBaseName.isNotBlank())
                        vm.createFromTemplate(fileBaseName.trim(), pendingExt, pendingAsset)
                    askFileName = false
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { askFileName = false }) { Text("Отмена") } }
        )
    }

    if (dlgMove) {
        val dirs = files.filter { it.type == "dir" }
        val notRoot = path != "disk:/"

        AlertDialog(
            onDismissRequest = { dlgMove = false },
            title = {
                Text(
                    "Переместить в…",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {

                    if (notRoot) {
                        ElevatedButton(
                            onClick = {
                                selectedFile?.let { vm.move(it.name, "..") }
                                dlgMove = false; selectedFile = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text("..") }
                    }

                    if (dirs.isEmpty()) Text("В этом каталоге нет папок")
                    dirs.forEach { dir ->
                        ElevatedButton(
                            onClick = {
                                selectedFile?.let { vm.move(it.name, dir.name) }
                                dlgMove = false; selectedFile = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text(dir.name) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dlgMove = false }) { Text("Отмена") } }
        )
    }

    if (dlgRename) {
        AlertDialog(
            onDismissRequest = { dlgRename = false },
            title = {
                Text(
                    "Переименовать",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    singleLine = true, label = { Text("Новое имя") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val old = selectedFile?.name ?: return@TextButton
                    if (newFileName.isNotBlank() && newFileName != old)
                        vm.rename(old, newFileName.trim())
                    dlgRename = false; selectedFile = null
                }) { Text("ОК") }
            },
            dismissButton = { TextButton(onClick = { dlgRename = false }) { Text("Отмена") } }
        )
    }

    if (dlgAccounts) {
        AlertDialog(
            onDismissRequest = { dlgAccounts = false },
            title = {
                Text(
                    "Аккаунты",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    if (allTokens.isEmpty()) Text("Нет сохранённых токенов")
                    allTokens.forEach { token ->
                        val name by tokenStore.getTokenName(token).collectAsState(initial = null)
                        ElevatedButton(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    tokenStore.selectToken(token)
                                    withContext(Dispatchers.Main) {
                                        dlgAccounts = false; vm.load("disk:/")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                if (token == selectedToken) "✅   ${name ?: token}" else name
                                    ?: token,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dlgAccounts = false }) { Text("Ок") } }
        )
    }

    LaunchedEffect(Unit) { vm.load() }
}

fun getFileIcon(f: DiskFile): String = when (f.type) {
    "dir" -> "📁"
    else -> when {
        f.name.endsWith(".jpg", true) || f.name.endsWith(".png", true) -> "🖼️"
        f.name.endsWith(".zip", true) || f.name.endsWith(".rar", true) -> "📦"
        f.name.endsWith(".pdf", true) -> "📕"
        f.name.endsWith(".txt", true) -> "📄"
        f.name.endsWith(".docx", true) || f.name.endsWith(".doc", true) -> "📝"
        f.name.endsWith(".pptx", true) || f.name.endsWith(".ppt", true) -> "📊"
        f.name.endsWith(".xlsx", true) || f.name.endsWith(".xls", true) -> "📈"
        f.name.endsWith(".mp4", true) -> "🎞️"
        else -> "📄"
    }
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 Б"
    val units = arrayOf("Б", "КБ", "MБ", "ГБ", "ТБ")
    var i = 0
    var size = bytes.toDouble()
    while (size >= 1024 && i < units.lastIndex) {
        size /= 1024; i++
    }
    return "%.1f %s".format(size, units[i])
}

fun openFileSmart(ctx: Context, f: DiskFile, cwd: String) {
    val ext = f.name.substringAfterLast('.', "").lowercase()
    val inline = listOf("jpg", "jpeg", "png", "pdf")
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val link = ApiClient.create(ctx)
                .getDownloadLink("${cwd.removeSuffix("/")}/${f.name}").href
            val uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (ext in inline) setDataAndType(uri, getMimeType(f.name)) else data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(ctx, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun getMimeType(name: String): String =
    MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(name.substringAfterLast('.', "")) ?: "*/*"

fun downloadFile(ctx: Context, url: String, name: String) {
    val req = DownloadManager.Request(Uri.parse(url))
        .setTitle(name)
        .setDescription("Загрузка файла")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
        .setAllowedOverMetered(true)
    (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
}