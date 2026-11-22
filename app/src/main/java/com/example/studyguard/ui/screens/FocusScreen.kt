package com.example.studyguard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyguard.ui.theme.*
import com.example.studyguard.viewmodel.FocusSoundscape
import com.example.studyguard.viewmodel.StudySessionState
import com.example.studyguard.viewmodel.StudyViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(viewModel: StudyViewModel) {
    val subject by viewModel.subject.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val selectedDuration by viewModel.selectedDurationMinutes.collectAsState()
    val selectedSoundscape by viewModel.selectedSoundscape.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val timeLeft by viewModel.timeLeftSeconds.collectAsState()
    val prepTime by viewModel.prepTimeLeft.collectAsState()
    val graceTime by viewModel.graceTimeLeft.collectAsState()
    val violationReason by viewModel.violationReason.collectAsState()
    val isProximityClose by viewModel.isProximityClose.collectAsState()
    val isMotionDetected by viewModel.isMotionDetected.collectAsState()
    val isPositionViolated by viewModel.isPositionViolated.collectAsState()

    val totalDurationSeconds = selectedDuration * 60
    val progressRatio = if (totalDurationSeconds > 0) {
        (timeLeft.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 500),
        label = "TimerProgress"
    )

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (sessionState == StudySessionState.VIOLATION) {
                    Brush.verticalGradient(
                        colors = listOf(ViolationBgDark, Color(0xFF140305))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(BgMain, BgSecondary)
                    )
                }
            )
    ) {
        if (sessionState == StudySessionState.VIOLATION) {
            // Screen 2: Peringatan (Violation Screen)
            ViolationScreenContent(
                graceTime = graceTime,
                violationReason = violationReason,
                isMotionDetected = isMotionDetected,
                isProximityClose = isProximityClose,
                isPositionViolated = isPositionViolated,
                onReturnToFocus = { viewModel.resumeFocusFromViolation() }
            )
        } else {
            // Screen 1: Focus Screen (Fixed Bottom Action Bar + Scrollable Content)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Scrollable Upper Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Bar (Shield Icon + StudyGuard + Streak Badge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield Logo",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "StudyGuard",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Streak Pill Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Streak: $currentStreak hari",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Field Input: Mata Kuliah / Topik
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Mata Kuliah / Topik:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { if (sessionState == StudySessionState.IDLE) viewModel.setSubject(it) },
                            placeholder = { Text("Ketik Mata Kuliah atau Topik", color = TextMuted) },
                            enabled = sessionState == StudySessionState.IDLE,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = CardBg,
                                unfocusedContainerColor = CardBg,
                                disabledContainerColor = CardBg.copy(alpha = 0.6f),
                                disabledBorderColor = CardBorder,
                                disabledTextColor = TextPrimary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Duration Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            15 to "15 Menit",
                            25 to "25 Menit",
                            45 to "45 Menit",
                            1 to "Demo 1m"
                        ).forEach { (min, label) ->
                            val isSelected = selectedDuration == min
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) PrimaryBlue else CardBg
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GlowCyan else CardBorder,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable(enabled = sessionState == StudySessionState.IDLE) {
                                        viewModel.setDuration(min)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BgMain else TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Ambient Soundscape Selector
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Audio",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Latar Belakang Audio (Focus Soundscape):",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FocusSoundscape.entries.forEach { sound ->
                                val isSelected = selectedSoundscape == sound
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setSoundscape(sound) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else CardBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AccentCyan else CardBorder
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sound.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) AccentCyan else TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Circular Pomodoro Timer Ring
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(CircleShape)
                            .clickable(enabled = sessionState == StudySessionState.IDLE || sessionState == StudySessionState.COMPLETED || sessionState == StudySessionState.FAILED) {
                                if (sessionState == StudySessionState.COMPLETED || sessionState == StudySessionState.FAILED) {
                                    viewModel.resetToIdle()
                                }
                                viewModel.startFocusSession()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2

                            // Track Circle
                            drawCircle(
                                color = CardBg,
                                radius = radius,
                                style = Stroke(width = strokeWidth)
                            )

                            // Outer Glow Track Border
                            drawCircle(
                                color = CardBorder,
                                radius = radius + (strokeWidth / 2),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Progress Arc (Cyan Glow)
                            val sweepAngle = 360f * animatedProgress
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(AccentCyan, GlowCyan, PrimaryBlue, AccentCyan)
                                ),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (sessionState == StudySessionState.PREPARING) "00:03" else timeFormatted,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (sessionState == StudySessionState.IDLE) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = when (sessionState) {
                                        StudySessionState.FOCUSING -> "FOKUS BERJALAN"
                                        StudySessionState.PREPARING -> "PERSIAPAN ($prepTime S)"
                                        StudySessionState.COMPLETED -> "SESI SELESAI"
                                        StudySessionState.FAILED -> "SESI GAGAL"
                                        else -> "MULAI FOKUS"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }

                    // Optional Notes Field (Target Spesifik)
                    if (sessionState == StudySessionState.IDLE) {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { viewModel.setNotes(it) },
                            placeholder = { Text("Catatan / Target Spesifik (Opsional)", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = CardBg,
                                unfocusedContainerColor = CardBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Sensor Status Indicators (3 Sensor Chips)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sensor 1: Kedekatan (Clickable for testing)
                            SensorStatusChip(
                                modifier = Modifier.weight(1f),
                                title = "Sensor Kedekatan:",
                                statusText = if (isProximityClose) "TERPICU (Dekat)" else "AMAN (Jauh)",
                                isSafe = !isProximityClose,
                                onClick = { viewModel.simulateProximityTrigger(!isProximityClose) }
                            )

                            // Sensor 2: Gerak
                            SensorStatusChip(
                                modifier = Modifier.weight(1f),
                                title = "Sensor Gerak:",
                                statusText = if (isMotionDetected) "TERPICU (Gerak)" else "DIAM (Stabil)",
                                isSafe = !isMotionDetected
                            )
                        }

                        // Sensor 3: Posisi (Kemiringan)
                        SensorStatusChip(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Sensor Posisi (Orientasi):",
                            statusText = if (isPositionViolated) "TERPICU (Miring / Diangkat)" else "AMAN (Datar di Meja)",
                            isSafe = !isPositionViolated
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Fixed Always-Visible Bottom Action Button Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (sessionState == StudySessionState.FOCUSING || sessionState == StudySessionState.PREPARING) {
                        // Cancel Session Button
                        OutlinedButton(
                            onClick = { viewModel.cancelSessionManually() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CardBg,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "Batalkan Sesi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    } else {
                        // Prominent Always-Visible Start Session Button
                        Button(
                            onClick = {
                                if (sessionState == StudySessionState.COMPLETED || sessionState == StudySessionState.FAILED) {
                                    viewModel.resetToIdle()
                                }
                                viewModel.startFocusSession()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start",
                                    tint = BgMain,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (sessionState == StudySessionState.COMPLETED || sessionState == StudySessionState.FAILED) "Mulai Sesi Baru" else "Mulai Sesi Fokus",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BgMain
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorStatusChip(
    modifier: Modifier = Modifier,
    title: String,
    statusText: String,
    isSafe: Boolean,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSafe) SuccessGreen else ViolationRed)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSafe) SuccessGreen else ViolationRed
                )
            }
        }
    }
}

@Composable
fun ViolationScreenContent(
    graceTime: Int,
    violationReason: String,
    isMotionDetected: Boolean,
    isProximityClose: Boolean,
    isPositionViolated: Boolean,
    onReturnToFocus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Center Warning Icon inside double glowing concentric circles
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(ViolationRed.copy(alpha = 0.15f))
                    .border(2.dp, ViolationRed.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(ViolationRed.copy(alpha = 0.3f))
                        .border(2.dp, ViolationRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = ViolationRed,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Text(
                text = "PERINGATAN!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = ViolationTextBright,
                letterSpacing = 1.sp
            )

            Text(
                text = "JANGAN SENTUH ATAU ANGKAT HP!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ViolationTextBright
            )

            Text(
                text = "Jauhkan wajah/tangan dari layar\n& letakkan HP di meja",
                fontSize = 14.sp,
                color = TextPrimary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // 2 Cards: Masa Tenggang & Sensor Terpicu
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Masa Tenggang
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ViolationBgCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, ViolationBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = ViolationTextBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Masa Tenggang:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%02d DETIK", graceTime),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ViolationTextBright
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibrating Phone",
                        tint = ViolationTextBright,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Card 2: Sensor Terpicu
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ViolationBgCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, ViolationBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ViolationRed)
                    )
                    Column {
                        Text(
                            text = "Sensor Terpicu:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (violationReason.isNotBlank()) {
                                violationReason
                            } else if (isPositionViolated) {
                                "Posisi HP Miring / Diangkat!"
                            } else if (isMotionDetected && isProximityClose) {
                                "HP Bergerak & Ada Objek Dekat!"
                            } else if (isMotionDetected) {
                                "HP Bergerak / Diangkat!"
                            } else {
                                "Ada Objek Dekat Layar!"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ViolationTextBright
                        )
                    }
                }
            }
        }

        // Bottom Action Button: Kembali Fokus (Letakkan HP)
        Button(
            onClick = onReturnToFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ViolationRed)
        ) {
            Text(
                text = "Kembali Fokus (Letakkan HP)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
