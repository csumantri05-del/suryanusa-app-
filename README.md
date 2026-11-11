# SuryaNusa – Aplikasi Android Kalkulator PLTS Atap

Aplikasi Android (APK) untuk **Kalkulator SuryaNusa**: simulasi kapasitas dan pembiayaan PLTS atap untuk rumah dan UMKM, sesuai dengan prototipe di pitch deck (slide "Solusi Kami" & "Prototipe").

Aplikasinya bisa dipakai tanpa internet. Internet hanya dipakai untuk membuka WhatsApp dan memuat font.

## Fitur
- Input rata-rata tagihan listrik bulanan, jenis pelanggan (Rumah/UMKM), dan daya PLN
- Rekomendasi sistem (kWp, jumlah panel, luas atap, harga paket Rp15 jt/kWp)
- Dasbor: produksi energi harian, penghematan per bulan, tagihan baru, balik modal, CO₂
- Grafik profil produksi per jam dan akumulasi penghematan 25 tahun
- Skema pembiayaan: cicilan 12/24/36 bulan (DP 20%) atau tunai diskon 10%
- Rincian biaya perawatan & garansi, serta alur survei atap (masukan dari validasi prototipe)
- Formulir "Ajukan Jadwal Survei Teknis" yang mengirim ringkasan lewat WhatsApp
- Semua asumsi (tarif, harga, jam matahari, DP, margin, diskon) bisa diubah di dalam aplikasi

## Cara mendapatkan file APK

### Cara A — Otomatis lewat GitHub (tanpa install apa pun)
1. Buat repositori baru di GitHub, lalu upload seluruh isi folder ini (termasuk folder `.github`).
2. Buka tab **Actions** → workflow **Build APK** akan berjalan otomatis (± 3–5 menit).
3. Setelah selesai (centang hijau), buka run tersebut → bagian **Artifacts** → unduh **SuryaNusa-apk**.
4. Ekstrak zip-nya, kirim file `app-release.apk` ke HP, lalu pasang (izinkan "Instal dari sumber tidak dikenal").

### Cara B — Android Studio
1. Buka Android Studio → **Open** → pilih folder ini.
2. Tunggu proses Gradle sync selesai.
3. Menu **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
4. File APK ada di `app/build/outputs/apk/`.

## Pengaturan yang perlu diisi
Buka `app/src/main/assets/index.html`, cari `var CONFIG` di bagian script:
- `waNumber`: isi nomor WhatsApp resmi SuryaNusa (format `628xxxxxxxxxx`) agar pengajuan survei langsung terkirim ke nomor itu.
- `inverterPerKwp`, `maintPctYear`: perkiraan biaya penggantian inverter dan perawatan tahunan.

Tarif PLN default memakai tarif Oktober–Desember 2026 (R-1 1.300–2.200 VA Rp1.444,70/kWh; R-2/R-3 Rp1.699,53/kWh; B-2 Rp1.444,70/kWh).

## Catatan untuk rilis Play Store
Build saat ini ditandatangani dengan kunci debug, cukup untuk dipasang langsung dan untuk demo. Untuk upload ke Google Play, buat keystore sendiri dan atur `signingConfig` di `app/build.gradle`.
