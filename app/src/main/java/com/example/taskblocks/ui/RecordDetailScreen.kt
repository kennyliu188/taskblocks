package com.example.taskblocks.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskblocks.R
import androidx.core.content.FileProvider
import com.example.taskblocks.data.Record
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap

private val recordTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
fun RecordDetailScreen(
    recordId: Long,
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var record by remember(recordId) { mutableStateOf<Record?>(null) }
    LaunchedEffect(recordId) {
        record = viewModel.getRecordById(recordId)
    }
    if (record == null) {
        Text(stringResource(R.string.loading), modifier = Modifier.padding(16.dp))
        return
    }
    val r = record!!
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.record_detail), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(recordTimeFormat.format(Date(r.createdAt)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = r.content.ifEmpty { stringResource(R.string.no_text_content) },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        val attachments = r.parseAttachments()
        if (attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.attachments), style = MaterialTheme.typography.titleSmall)
            attachments.forEach { a ->
                val file = File(a.path)
                if (a.type == "image") {
                    var thumb by remember(a.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                    LaunchedEffect(a.path) {
                        val bmp = BitmapFactory.decodeFile(a.path)
                        thumb = bmp?.asImageBitmap()
                    }
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { openFile(context, file) },
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (thumb != null) {
                            Image(
                                bitmap = thumb!!,
                                contentDescription = stringResource(R.string.image),
                                modifier = Modifier.size(80.dp)
                            )
                        } else {
                            Text(stringResource(R.string.image), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(file.name, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { openFile(context, file) },
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.document), style = MaterialTheme.typography.bodyMedium)
                        Text(file.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun openFile(context: android.content.Context, file: File) {
    if (!file.exists()) return
    val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val ext = file.extension.lowercase()
    val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, type)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        // 没有可处理的应用时静默失败
    }
}
