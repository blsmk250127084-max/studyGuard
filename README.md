1. Nama dan Deskripsi Aplikasi
Nama Aplikasi: studyGuard
Deskripsi: Aplikasi produktivitas berbasis Android yang dirancang khusus untuk pelajar dan mahasiswa agar dapat belajar dengan fokus tinggi menggunakan metode
Pomodoro. Aplikasi ini dilengkapi dengan sistem keamanan pintar berbasis sensor perangkat (anti-distraksi/anti-curang) yang mendeteksi jika pengguna mengangkat
atau memainkan HP saat sesi belajar berlangsung. Selain itu, aplikasi ini juga menyediakan fitur manajemen jadwal kuliah, rekam jejak streak harian, dan audio focus soundscape.


3. Fitur-Fitur Aplikasi
Sesi Fokus (Pomodoro Timer & Soundscape):
Pilihan durasi fleksibel (15 menit, 25 menit, 45 menit, atau mode demo 1 menit).
Dilengkapi latar belakang audio penunjang konsentrasi (Focus Soundscape).
Proteksi Sensor Anti-Distraksi (Focus Guard):
Memantau gerakan dan posisi perangkat secara real-time. Jika HP diangkat, dimiringkan, atau didekatkan ke objek saat sesi fokus aktif, aplikasi akan memicu layar peringatan (Violation Screen) dengan masa tenggang (grace period).
Manajemen Jadwal Kuliah & Pelajaran:
Pengaturan jadwal harian (Senin s.d. Minggu) lengkap dengan jam, mata kuliah, ruangan, nama dosen, serta indikator waktu pelajaran yang sedang berlangsung secara otomatis.
Dashboard & Statistik Harian (Home Screen):
Menampilkan target belajar harian, persentase pencapaian waktu, jumlah sesi sukses, indikator streak hari aktif, dan daftar aktivitas terbaru.
Riwayat Sesi Belajar (History & Tracking):
Menyimpan rekam jejak seluruh sesi belajar (status sukses atau gagal) ke dalam database lokal.


4. Teknologi dan Arsitektur yang Digunakan
Bahasa Pemrograman: Kotlin
UI Framework: Jetpack Compose (Material 3) dengan desain modern bernuansa gelap dan aksen neon (dark theme).
Arsitektur: MVVM (Model-View-ViewModel) yang mengintegrasikan StateFlow dan Coroutines untuk pengelolaan reaktivitas data secara asinkron.
Database Lokal: Room Database (androidx.room) untuk penyimpanan data relasional riwayat sesi belajar dan jadwal kuliah.
Navigasi & Komponen Lain: Jetpack Navigation Compose, Material Icons Extended, Lifecycle KTX, dan KSP (Kotlin Symbol Processing).
Minimum SDK: Android 8.0 (API Level 26) / Target SDK 34.


5. Sensor yang Digunakan Beserta Fungsinya
Aplikasi ini memanfaatkan tiga sensor hardware utama pada perangkat Android untuk mendeteksi pelanggaran konsentrasi saat mode fokus aktif:
1. Sensor Proximity (Sensor.TYPE_PROXIMITY):
Fungsi: Mendeteksi jarak objek di dekat layar (misalnya saat tangan atau benda menutupi bagian atas layar HP).
2. Sensor Linear Acceleration (Sensor.TYPE_LINEAR_ACCELERATION):
Fungsi: Mendeteksi perubahan percepatan atau pergerakan tiba-tiba ketika perangkat digerakkan atau diangkat dari permukaan meja.
3. Sensor Accelerometer (Sensor.TYPE_ACCELEROMETER):
Fungsi: Mendeteksi orientasi posisi perangkat (memeriksa apakah HP diletakkan datar di atas meja atau sedang dipegang dan dimiringkan oleh pengguna).


5. Cara Menjalankan Aplikasi
Buka projectnya langsung dihp jika sudah terhubung ke hp
