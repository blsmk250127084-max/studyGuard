# Walkthrough - Auto-Preload Jadwal XI-RPL

Jadwal pelajaran kelas **XI - RPL** (Senin s/d Jumat) kini otomatis terisi ke dalam layar *Jadwal Pelajaran* saat aplikasi pertama kali dijalankan, tanpa Anda harus mengetiknya secara manual.

---

## Changes Made

### 1. Auto-Preload di ViewModel (`StudyViewModel.kt`)
- Menambahkan fungsi `checkAndPreloadDefaultSchedule()` yang berjalan saat ViewModel diinisialisasi.
- Jika database lokal belum memiliki catatan jadwal (`empty`), seluruh daftar mata pelajaran XI - RPL (B. Inggris, Penjas, Android, Basis Data, B. Jepang, PBO, MTK, Framework, KIK, PKN, Agama, Sejarah, dll.) langsung dimasukkan secara otomatis.

---

## Verification Results

### Automated Build Verification
- Perintah kompilasi `./gradlew assembleDebug`:
  - Status: **BUILD SUCCESSFUL**.
