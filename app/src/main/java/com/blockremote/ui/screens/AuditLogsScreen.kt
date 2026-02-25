package com.blockremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.blockremote.viewmodel.AuditLogEntry
import com.blockremote.viewmodel.BlockRemoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogsScreen(viewModel: BlockRemoteViewModel) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(state.auditLogs.size) {
        if (state.auditLogs.isNotEmpty()) {
            listState.animateScrollToItem(state.auditLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "AUDIT LOGS",
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.primary
        )
        Text(
            text = "System event terminal",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.background)
                .padding(1.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Text(
                text = "root@blockremote:~# tail -f /var/log/sentinel.log",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(state.auditLogs) { entry ->
                    LogEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: AuditLogEntry) {
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    val timeStr = dateFormat.format(Date(entry.timestamp))
    val colorScheme = MaterialTheme.colorScheme

    val levelColor = when (entry.level) {
        "CRIT" -> colorScheme.error
        "WARN" -> colorScheme.secondary
        "INFO" -> colorScheme.primary
        else -> colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = colorScheme.onSurface.copy(alpha = 0.4f))) {
            append("[$timeStr] ")
        }
        withStyle(SpanStyle(color = levelColor)) {
            append("${entry.level.padEnd(4)} ")
        }
        withStyle(SpanStyle(color = colorScheme.secondary.copy(alpha = 0.7f))) {
            append("[${entry.tag}] ")
        }
        withStyle(SpanStyle(color = colorScheme.onSurface.copy(alpha = 0.8f))) {
            append(entry.message)
        }
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
    )
}
