package com.example.studyguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyguard.data.StudySessionEntity
import com.example.studyguard.ui.theme.*
import com.example.studyguard.viewmodel.AchievementBadge
import com.example.studyguard.viewmodel.DailyFocusStat
import com.example.studyguard.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: StudyViewModel) {
    val sessions by viewModel.allSessions.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val badges by viewModel.achievementBadges.collectAsState()

    var selectedSessionForDetail by remember { mutableStateOf<StudySessionEntity?>(null) }

    // Calculate real stats from DB
    val totalSessions = sessions.size
    val completedSessions = sessions.count { it.status == "SUCCESS" }
    val successRate = if (totalSessions > 0) (completedSessions * 100) / totalSessions else 0
    val totalFocusMinutes = sessions.filter { it.status == "SUCCESS" }.sumOf { it.achievedDurationSeconds } / 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Riwayat & Progres",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (sessions.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Riwayat",
                        tint = ViolationRed
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Streak Banner Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Streak Konsistensi: $currentStreak hari",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (currentStreak > 0) "Pertahankan ritme fokusmu setiap hari." else "Selesaikan 1 sesi fokus hari ini untuk memulai streak.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Section 2: Top 3 Stats Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HistoryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        value = "$totalFocusMinutes Min",
                        label = "Total Fokus"
                    )

                    HistoryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        value = "$completedSessions Sesi",
                        label = "Selesai"
                    )

                    HistoryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Percent,
                        value = "$successRate%",
                        label = "Rasio Sukses"
                    )
                }
            }

            // Section 3: Weekly 7-Day Bar Chart
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Aktivitas Fokus 7 Hari Terakhir",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        WeeklyBarChart(stats = weeklyStats)
                    }
                }
            }

            // Section 4: Achievement Badges Carousel
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Pencapaian",
                                tint = WarningYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Lencana Pencapaian",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(badges) { badge ->
                            BadgeCard(badge = badge)
                        }
                    }
                }
            }

            // Section 5: Log Sesi Belajar Header
            item {
                Text(
                    text = "Log Sesi Belajar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Section 6: Session Items
            if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Belum Ada Catatan Belajar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Selesaikan sesi fokus pertamamu untuk mencatat aktivitas di sini.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sessions) { session ->
                    HistorySessionItem(
                        session = session,
                        onClick = { selectedSessionForDetail = session }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet Detail Sesi
    selectedSessionForDetail?.let { session ->
        ModalBottomSheet(
            onDismissRequest = { selectedSessionForDetail = null },
            containerColor = CardBg,
            contentColor = TextPrimary
        ) {
            SessionDetailBottomSheetContent(
                session = session,
                onDelete = {
                    viewModel.deleteSession(session)
                    selectedSessionForDetail = null
                },
                onClose = { selectedSessionForDetail = null }
            )
        }
    }
}

@Composable
fun HistoryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeklyBarChart(stats: List<DailyFocusStat>) {
    val maxMinutes = stats.maxOfOrNull { it.minutes }?.coerceAtLeast(60) ?: 60

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        stats.forEach { stat ->
            val barHeightRatio = if (maxMinutes > 0) (stat.minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f) else 0.08f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                if (stat.minutes > 0) {
                    Text(
                        text = "${stat.minutes}m",
                        fontSize = 9.sp,
                        color = if (stat.isToday) PrimaryBlue else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(barHeightRatio * 0.75f)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (stat.isToday) PrimaryBlue else if (stat.minutes > 0) AccentCyan.copy(alpha = 0.6f) else CardBorder
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stat.dayLabel,
                    fontSize = 10.sp,
                    fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (stat.isToday) PrimaryBlue else TextSecondary
                )
            }
        }
    }
}

@Composable
fun BadgeCard(badge: AchievementBadge) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badge.isUnlocked) PrimaryBlue.copy(alpha = 0.4f) else CardBorder
        ),
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) PrimaryBlue.copy(alpha = 0.15f) else BgSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badge.isUnlocked) "OK" else "-",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) PrimaryBlue else TextMuted
                )
            }

            Text(
                text = badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) TextPrimary else TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = badge.progressText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (badge.isUnlocked) SuccessGreen else TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistorySessionItem(
    session: StudySessionEntity,
    onClick: () -> Unit
) {
    val isSuccess = session.status == "SUCCESS"
    val formattedDate = formatSessionTimestamp(session.timestamp)
    val achievedMinutes = session.achievedDurationSeconds / 60
    val durationText = if (isSuccess) {
        "${session.targetDurationMinutes} Menit"
    } else {
        "$achievedMinutes/${session.targetDurationMinutes} Menit"
    }

    val badgeText = if (isSuccess) {
        "SELESAI"
    } else {
        val reason = session.failureReason ?: "HP Diangkat"
        if (reason.contains("HP", ignoreCase = true) || reason.contains("diangkat", ignoreCase = true) || reason.contains("bergerak", ignoreCase = true)) {
            "GAGAL: HP Diangkat"
        } else if (reason.contains("Objek", ignoreCase = true) || reason.contains("tangan", ignoreCase = true) || reason.contains("wajah", ignoreCase = true)) {
            "GAGAL: Objek Dekat"
        } else {
            "GAGAL"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.subject.ifBlank { "Pemrograman Mobile" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = durationText,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSuccess) SuccessGreen.copy(alpha = 0.15f) else ViolationRed.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSuccess) SuccessGreen else ViolationRed,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SessionDetailBottomSheetContent(
    session: StudySessionEntity,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val isSuccess = session.status == "SUCCESS"
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("id", "ID"))
    val fullDateString = dateFormat.format(Date(session.timestamp))
    val achievedMin = session.achievedDurationSeconds / 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detail Sesi Belajar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSuccess) SuccessGreen.copy(alpha = 0.15f) else ViolationRed.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isSuccess) "SELESAI ✅" else "GAGAL ❌",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSuccess) SuccessGreen else ViolationRed,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        HorizontalDivider(color = CardBorder)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow(label = "Mata Kuliah / Topik", value = session.subject)
            DetailRow(label = "Waktu Pelaksanaan", value = fullDateString)
            DetailRow(label = "Target Durasi", value = "${session.targetDurationMinutes} Menit")
            DetailRow(label = "Durasi Tercapai", value = "$achievedMin Menit")
            DetailRow(label = "Jumlah Peringatan Alarm", value = "${session.alarmCount} kali")

            if (!isSuccess && !session.failureReason.isNullOrBlank()) {
                DetailRow(label = "Alasan Kegagalan", value = session.failureReason, valueColor = ViolationRed)
            }

            if (!session.notes.isNullOrBlank()) {
                DetailRow(label = "Catatan Target", value = session.notes)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ViolationRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, ViolationRed.copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hapus Sesi")
            }

            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Tutup", color = BgMain, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

private fun formatSessionTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }

    val isToday = now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    return when {
        isToday -> "Hari ini, ${timeFormat.format(time.time)}"
        isYesterday -> "Kemarin, ${timeFormat.format(time.time)}"
        else -> dateFormat.format(time.time)
    }
}
