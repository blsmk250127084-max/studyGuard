package com.example.studyguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyguard.data.ScheduleEntity
import com.example.studyguard.ui.theme.*
import com.example.studyguard.viewmodel.StudyViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: StudyViewModel) {
    val allSchedules by viewModel.allSchedules.collectAsState()

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    var selectedDay by remember {
        mutableStateOf(
            when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Senin"
                Calendar.TUESDAY -> "Selasa"
                Calendar.WEDNESDAY -> "Rabu"
                Calendar.THURSDAY -> "Kamis"
                Calendar.FRIDAY -> "Jumat"
                Calendar.SATURDAY -> "Sabtu"
                else -> "Minggu"
            }
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    val filteredSchedules = allSchedules.filter { it.day.equals(selectedDay, ignoreCase = true) }

    Scaffold(
        containerColor = BgMain,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = BgMain,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Jadwal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal Pelajaran",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Day Selector Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(days) { day ->
                    val isSelected = selectedDay == day
                    Surface(
                        modifier = Modifier
                            .clickable { selectedDay = day },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) PrimaryBlue else CardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GlowCyan else CardBorder
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BgMain else TextSecondary
                            )
                        }
                    }
                }
            }

            // Schedule List
            if (filteredSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Belum Ada Jadwal untuk Hari $selectedDay",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Ketuk tombol (+) di bawah untuk menambahkan mata kuliah.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSchedules) { schedule ->
                        ScheduleItemCard(
                            schedule = schedule,
                            onDelete = { viewModel.deleteSchedule(schedule) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            defaultDay = selectedDay,
            onDismiss = { showAddDialog = false },
            onSave = { newSchedule ->
                viewModel.insertSchedule(newSchedule)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    schedule: ScheduleEntity,
    onDelete: () -> Unit
) {
    val isOngoing = isScheduleOngoing(schedule.day, schedule.time)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isOngoing) PrimaryBlue.copy(alpha = 0.12f) else CardBg,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isOngoing) 1.5.dp else 1.dp,
            color = if (isOngoing) PrimaryBlue else CardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Waktu",
                        tint = if (isOngoing) GlowCyan else PrimaryBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = schedule.time,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOngoing) GlowCyan else PrimaryBlue
                    )
                }

                Text(
                    text = schedule.subject,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ruangan",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = schedule.room,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    if (!schedule.lecturer.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Dosen",
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = schedule.lecturer,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = ViolationRed
                )
            }
        }
    }
}

private fun isScheduleOngoing(scheduleDay: String, timeRangeStr: String): Boolean {
    val currentCalendar = Calendar.getInstance()
    val currentDayOfWeek = when (currentCalendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Senin"
        Calendar.TUESDAY -> "Selasa"
        Calendar.WEDNESDAY -> "Rabu"
        Calendar.THURSDAY -> "Kamis"
        Calendar.FRIDAY -> "Jumat"
        Calendar.SATURDAY -> "Sabtu"
        else -> "Minggu"
    }

    if (!scheduleDay.equals(currentDayOfWeek, ignoreCase = true)) return false

    val parts = timeRangeStr.split("-").map { it.trim() }
    if (parts.size != 2) return false

    val startParts = parts[0].split(":")
    val endParts = parts[1].split(":")
    if (startParts.size != 2 || endParts.size != 2) return false

    val startMinutes = (startParts[0].toIntOrNull() ?: 0) * 60 + (startParts[1].toIntOrNull() ?: 0)
    val endMinutes = (endParts[0].toIntOrNull() ?: 0) * 60 + (endParts[1].toIntOrNull() ?: 0)

    val currentMinutes = currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(Calendar.MINUTE)

    return currentMinutes in startMinutes..endMinutes
}

@Composable
fun AddScheduleDialog(
    defaultDay: String,
    onDismiss: () -> Unit,
    onSave: (ScheduleEntity) -> Unit
) {
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    var selectedDay by remember { mutableStateOf(defaultDay) }
    var time by remember { mutableStateOf("08:00 - 09:40") }
    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("Tambah Jadwal Pelajaran", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hari:", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(days) { day ->
                        val isSel = selectedDay == day
                        Surface(
                            modifier = Modifier.clickable { selectedDay = day },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) PrimaryBlue else BgSecondary
                        ) {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) BgMain else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Jam (Contoh: 08:00 - 09:40)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Mata Kuliah / Topik") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Ruangan / Lokasi") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = lecturer,
                    onValueChange = { lecturer = it },
                    label = { Text("Nama Dosen (Opsional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && room.isNotBlank()) {
                        onSave(
                            ScheduleEntity(
                                day = selectedDay,
                                time = time.ifBlank { "08:00 - 09:40" },
                                subject = subject,
                                room = room,
                                lecturer = lecturer.ifBlank { null }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Simpan", color = BgMain, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}
