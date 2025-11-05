# Dokumentasi Pengguna Aplikasi Minibank

## Panduan Yang Tersedia

1. **[Panduan Approval Workflow](panduan-approval-workflow.md)** ⭐ NEW
   - Target: Customer Service (CS) dan Branch Manager
   - Proses: Membuat nasabah baru dengan approval workflow
   - Format: Panduan lengkap dengan screenshot dan video
   - Versi: 2.0 (Updated with Approval Workflow)

## Cara Menggunakan Panduan

1. Buka file panduan yang sesuai dengan kebutuhan Anda
2. Ikuti langkah-langkah secara berurutan
3. Lihat screenshot untuk referensi visual
4. Tonton video tutorial jika tersedia
5. Gunakan bagian troubleshooting jika mengalami masalah

## Perubahan dari Versi Sebelumnya

### Versi 2.0 (05 November 2025)
- ✨ **NEW:** Implementasi Approval Workflow
- ✨ Customer baru memerlukan approval dari Branch Manager
- ✨ Dual control untuk operasi penting
- ✨ Audit trail lengkap untuk setiap approval/rejection
- ⚠️ **BREAKING CHANGE:** Customer tidak langsung aktif setelah dibuat

### Versi 1.0 (Obsolete)
- Customer langsung aktif setelah dibuat (tanpa approval)
- ❌ **DEPRECATED:** Panduan pembukaan rekening versi lama tidak berlaku lagi

## Pembaruan Panduan

Panduan ini dibuat secara otomatis dari test dokumentasi. Untuk memperbarui:

```bash
# 1. Jalankan test dokumentasi (slow mode dengan recording)
mvn test -Dtest=ApprovalWorkflowTutorialTest \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=2000 \
  -Dplaywright.record=true

# 2. Generate ulang panduan
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.ApprovalWorkflowDocGenerator"
```

---

*Dibuat pada: 05 November 2025*
