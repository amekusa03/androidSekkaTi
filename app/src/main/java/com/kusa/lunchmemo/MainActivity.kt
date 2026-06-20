package com.kusa.sekkati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kusa.sekkati.data.SekkaTiViewModel
import com.kusa.sekkati.ui.theme.SekkaTiTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SekkaTiTheme {
                SekkaTiScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SekkaTiScreen(
    viewModel: SekkaTiViewModel = viewModel()
) {
    val memos by viewModel.memos.collectAsState()
    var showSummary by remember { mutableStateOf(false) }
    var showDeleteMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cleanOldMemos()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            "SekkaTi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showDeleteMenu = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Cleanup")
                }
                ExtendedFloatingActionButton(
                    onClick = { showSummary = true },
                    icon = { Icon(Icons.Default.AutoAwesomeMotion, contentDescription = null) },
                    text = { Text("Summary") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            SekkaTiContent(
                memos = memos,
                onMemoChange = { date, memo -> viewModel.saveMemo(date, memo) }
            )
        }

        if (showSummary) {
            SummaryBottomSheet(
                memos = memos,
                onDismiss = { showSummary = false }
            )
        }

        if (showDeleteMenu) {
            DeleteOptionsBottomSheet(
                onDeleteYesterday = { viewModel.deleteYesterday() },
                onDeleteLastWeek = { viewModel.deleteLastWeek() },
                onDeleteLastMonth = { viewModel.deleteLastMonth() },
                onDismiss = { showDeleteMenu = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(
    memos: Map<LocalDate, String>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Memo Summary",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                val sortedMemos = memos.toList().filter { it.second.isNotEmpty() }.sortedByDescending { it.first }
                items(sortedMemos) { (date, memo) ->
                    ListItem(
                        headlineContent = { Text(date.toString(), fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(memo) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteOptionsBottomSheet(
    onDeleteYesterday: () -> Unit,
    onDeleteLastWeek: () -> Unit,
    onDeleteLastMonth: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Quick Cleanup",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                "Select a range to delete memos from your history.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showDeleteConfirm = "yesterday" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Delete Yesterday's Memo")
                }
                Button(
                    onClick = { showDeleteConfirm = "week" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Delete Last Week's Memos")
                }
                Button(
                    onClick = { showDeleteConfirm = "month" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Delete Last Month's Memos")
                }
            }
        }
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Confirm Deletion") },
            text = { Text("This action cannot be undone. Are you sure you want to delete memos from ${showDeleteConfirm}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (showDeleteConfirm) {
                            "yesterday" -> onDeleteYesterday()
                            "week" -> onDeleteLastWeek()
                            "month" -> onDeleteLastMonth()
                        }
                        showDeleteConfirm = null
                        onDismiss() // Close bottom sheet after action
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SekkaTiContent(
    memos: Map<LocalDate, String>,
    onMemoChange: (LocalDate, String) -> Unit
) {
    val pageCount = 2000
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = initialPage)
    val today = LocalDate.now()

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 44.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) { page ->
        val date = today.plusDays((page - initialPage).toLong())
        val isSelected = pagerState.currentPage == page
        
        val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue

        SekkaTiCard(
            date = date,
            memo = memos[date] ?: "",
            isEditable = isSelected,
            onMemoChange = { onMemoChange(date, it) },
            modifier = Modifier.graphicsLayer {
                val fraction = 1f - pageOffset.coerceIn(0f, 1f)
                alpha = lerp(start = 0.5f, stop = 1f, fraction = fraction)
                scaleX = lerp(start = 0.85f, stop = 1f, fraction = fraction)
                scaleY = lerp(start = 0.85f, stop = 1f, fraction = fraction)
            }
        )
    }
}

@Composable
fun SekkaTiCard(
    date: LocalDate,
    memo: String,
    isEditable: Boolean,
    onMemoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(date) {
        mutableStateOf(TextFieldValue(text = memo, selection = TextRange(memo.length)))
    }

    LaunchedEffect(memo) {
        if (memo != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = memo)
        }
    }

    val isToday = date == LocalDate.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    val dayFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())

    val cardElevation by animateDpAsState(if (isEditable) 12.dp else 2.dp, label = "elevation")
    val cardColor by animateColorAsState(
        if (isEditable) MaterialTheme.colorScheme.surface 
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "color"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ヘッダー部分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = date.format(monthFormatter).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.format(dayFormatter),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = date.format(dayOfWeekFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isToday) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Today", fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // コンテンツ部分
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isEditable) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                        else Color.Transparent
                    )
                    .padding(if (isEditable) 8.dp else 0.dp)
            ) {
                if (isEditable) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            val textChanged = newValue.text != textFieldValue.text
                            textFieldValue = newValue
                            if (textChanged) {
                                onMemoChange(newValue.text)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        placeholder = { 
                            Text(
                                "Quick memo...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Start
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = memo.ifEmpty { "No memo" },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = if (memo.isEmpty()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                color = if (memo.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                                        else MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            if (isEditable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SekkaTiScreenPreview() {
    SekkaTiTheme {
        SekkaTiContent(
            memos = mapOf(LocalDate.now() to "A quick memo"),
            onMemoChange = { _, _ -> }
        )
    }
}
