/**
 * SuryaNusa – penerima pengajuan survei dari aplikasi Android.
 * Tempel kode ini di Google Sheet: Ekstensi → Apps Script, lalu Terapkan sebagai Aplikasi web.
 */
const SHEET_NAME = 'Pengajuan Survei';
const HEADERS = [
  'Waktu masuk', 'Nomor pengajuan', 'Nama', 'WhatsApp', 'Alamat', 'Tanggal survei',
  'Jenis', 'Daya PLN (VA)', 'Tagihan/bulan (Rp)', 'Rekomendasi (kWp)', 'Jumlah panel',
  'Hemat/bulan (Rp)', 'Skema', 'Cicilan/bulan (Rp)', 'Total bayar (Rp)', 'Status'
];

function doPost(e) {
  const lock = LockService.getScriptLock();
  lock.waitLock(15000);
  try {
    const d = JSON.parse(e.postData.contents);
    const sh = getSheet_();

    // Abaikan kiriman ganda (aplikasi mengirim ulang otomatis saat sinyal putus)
    if (d.id && sh.getLastRow() > 1) {
      const ids = sh.getRange(2, 2, sh.getLastRow() - 1, 1).getValues().flat();
      if (ids.indexOf(d.id) !== -1) return json_({ ok: true, duplicate: true });
    }

    sh.appendRow([
      new Date(), clean_(d.id), clean_(d.nama), "'" + clean_(d.hp), clean_(d.alamat), clean_(d.tanggal),
      clean_(d.jenis), num_(d.daya), num_(d.tagihan), num_(d.kwp), num_(d.panel),
      num_(d.hemat), clean_(d.skema), num_(d.cicilan), num_(d.total), 'Baru'
    ]);
    return json_({ ok: true, id: d.id });
  } catch (err) {
    return json_({ ok: false, error: String(err) });
  } finally {
    lock.releaseLock();
  }
}

function doGet() {
  return json_({ ok: true, service: 'SuryaNusa pengajuan survei' });
}

function getSheet_() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  let sh = ss.getSheetByName(SHEET_NAME);
  if (!sh) sh = ss.insertSheet(SHEET_NAME);
  if (sh.getLastRow() === 0) {
    sh.appendRow(HEADERS);
    sh.setFrozenRows(1);
    sh.getRange(1, 1, 1, HEADERS.length).setFontWeight('bold').setBackground('#0b4f6c').setFontColor('#ffffff');
    sh.getRange('A:A').setNumberFormat('dd/mm/yyyy hh:mm');
    sh.getRange('I:I').setNumberFormat('#,##0');
    sh.getRange('L:L').setNumberFormat('#,##0');
    sh.getRange('N:O').setNumberFormat('#,##0');
  }
  return sh;
}

// Cegah teks yang diawali = + - @ dibaca sebagai rumus
function clean_(v) {
  const s = v == null ? '' : String(v).slice(0, 500);
  return /^[=+\-@]/.test(s) ? "'" + s : s;
}
function num_(v) {
  const n = Number(v);
  return v === '' || v == null || isNaN(n) ? '' : n;
}
function json_(o) {
  return ContentService.createTextOutput(JSON.stringify(o)).setMimeType(ContentService.MimeType.JSON);
}
