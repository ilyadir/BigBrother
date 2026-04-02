package com.bigbrother.app.ui.balance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bigbrother.app.data.local.dao.BalanceDao
import com.bigbrother.app.data.local.dao.TrackedAppDao
import com.bigbrother.app.data.local.entity.BalanceEntryEntity
import com.bigbrother.app.data.local.entity.TrackedAppEntity
import com.bigbrother.app.domain.model.EntryType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class BalanceViewModel @Inject constructor(
    balanceDao: BalanceDao,
    trackedAppDao: TrackedAppDao
) : ViewModel() {

    private val startEndOfToday = currentDayMillisRange()

    val uiState: Flow<BalanceUiState> = combine(
        balanceDao.streamBalance(),
        balanceDao.sumEarnForPeriod(startEndOfToday.first, startEndOfToday.second),
        balanceDao.sumSpendForPeriod(startEndOfToday.first, startEndOfToday.second),
        balanceDao.latestEntries(limit = 12),
        trackedAppDao.listBlocked()
    ) { balanceMinutes, earnedTodayMinutes, spentTodayMinutes, latestEntries, blockedApps ->
        BalanceUiState(
            remainingMinutes = balanceMinutes,
            earnedTodayMinutes = earnedTodayMinutes,
            spentTodayMinutes = spentTodayMinutes,
            recentEntries = latestEntries,
            blockedApps = blockedApps
        )
    }
}

data class BalanceUiState(
    val remainingMinutes: Int = 0,
    val earnedTodayMinutes: Int = 0,
    val spentTodayMinutes: Int = 0,
    val recentEntries: List<BalanceEntryEntity> = emptyList(),
    val blockedApps: List<TrackedAppEntity> = emptyList()
)

@Composable
fun BalanceScreen(viewModel: BalanceViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState(initial = BalanceUiState())

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = formatMinutesToClock(state.remainingMinutes),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = "остаток", style = MaterialTheme.typography.bodyMedium)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "заработано сегодня",
                    value = formatMinutesToClock(state.earnedTodayMinutes)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "потрачено сегодня",
                    value = formatMinutesToClock(state.spentTodayMinutes)
                )
            }
        }

        item {
            Text(text = "Последние операции", style = MaterialTheme.typography.titleMedium)
        }

        if (state.recentEntries.isEmpty()) {
            item {
                Text(text = "Пока нет операций", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(items = state.recentEntries, key = { it.id }) { entry ->
                BalanceEntryRow(entry)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Под контролем сейчас", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${state.blockedApps.size} пакетов",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.blockedApps.isEmpty()) {
                        Text(text = "Нет заблокированных приложений")
                    } else {
                        state.blockedApps.forEach { app ->
                            Text(text = "• ${app.appLabel} (${app.packageName})")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BalanceEntryRow(entry: BalanceEntryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (entry.type == EntryType.EARN) "Пополнение" else "Списание",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = entry.note, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(entry.createdAt)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = formatMinutesToClock(entry.minutes),
                style = MaterialTheme.typography.titleMedium,
                color = if (entry.type == EntryType.EARN) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

private fun currentDayMillisRange(): Pair<Long, Long> {
    val now = java.time.LocalDate.now()
    val start = now.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val end = now.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    return start to end
}

private fun formatMinutesToClock(totalMinutes: Int): String {
    val totalSeconds = totalMinutes.coerceAtLeast(0) * 60
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
