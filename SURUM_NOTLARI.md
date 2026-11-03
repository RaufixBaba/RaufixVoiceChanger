# 🚀 Raufix Voice Changer — `V1.0 BETA` Sürüm Notları

**Yayın Tarihi:** Kasım 2026  
**Geliştirici:** `RaufixBaba`  
**Hedef Sürüm:** Minecraft `1.21.11` (Fabric Loader `0.18.0+` • Simple Voice Chat `2.6.x` • Java `21`)

---

## 🌟 V1.0 BETA ile Neler Geldi?

### 1. Doğal Vokal DSP Motoru v2.0 (`VocalPitchShifter` + `VocalRealismStage`)
- **Doğal Kız ve Erkek Seslerinde Devrim:** Yukarı perde kaydırmada oluşan "sincap / helyum / metalik çınlama" sorununu çözen kesirsel spektral faz vokoder (`FFT=1024, HOP=128`) eklendi.
- **Akıllı Konuşmacı Perde Takibi (`SpeakerPitchTracker`):** Mikrofondan gelen kendi sesinizin temel frekansını (`F0`) gerçek zamanlı ölçerek hedef kadın (`~195–210 Hz`) veya erkek (`~100–115 Hz`) aralığına orantılı ve doğal biçimde oturtur.
- **Türkçe Ünsüz ve Sibilant Koruması:** `3.6 kHz` üzerindeki ötümsüz `s`, `ş`, `ç`, `t`, `f` sesleri perde kaydırma bulanıklığından korunur; entegre dinamik S-Yumuşatıcı (De-Esser) ile tiz patlamalar dengelenir.
- **4 Yeni Stüdyo Parametresi (Toplam 30 Parametre):**
  - `Berraklık (Clarity)` — Boğukluğu temizleyen `185 Hz` kesim + `2.75 kHz` netlik rafı.
  - `Nefeslilik (Breathiness)` — Konuşma zarfıyla tetiklenen doğal vokal hava katmanı (`2.5–6.8 kHz`).
  - `Tüp Sıcaklığı (Warmth)` — Yumuşak optik kompresyon ve sıcak çift harmonik doygunluk.
  - `Stüdyo Koro (Chorus)` — Çift LFO'lu stereo/mono uyumlu mikro vokal genişletme.

### 2. 15 Yepyeni Ses Profili (Toplam 22 Seçilebilir Ses!)
- **Yeni Doğal Sesler:** `Yumuşak Kız Sesi (Soft)`, `Olgun Kadın Sesi`, `Derin Radyo Spikeri`, `Genç Erkek`, `Yaşlı Bilge Adam`
- **Yeni Karakter Sesleri:** `Anime Kızı (Kawaii)`, `Telefon Katili (Ghostface)`, `Karanlık Lord (Kasklı)`, `Kadim Büyücü`
- **Yeni Fantastik & Korku Sesleri:** `Gölge Azrail`, `Hortlak / Zombi`, `Semavi Melek`
- **Yeni Bilim Kurgu & Müzik Sesleri:** `Kozmik Uzaylı`, `Askeri Telsiz`, `Trap Rapçi Autotune`
- Mevcut `Doğal Kız Sesi`, `Doğal Erkek Sesi`, `Çocuk Sesi`, `Karanlık Şövalye`, `Devasa Titan`, `Cehennem İblisi` ve `Taktiksel Sayborg` profilleri yeni vokal gerçekçilik motoruna göre baştan kalibre edildi.

### 3. Yeni Nesil RGB Neon Stüdyo Arayüzü
- Akıcı animasyonlu RGB çerçeveler, cam efektli paneller, özel RGB kaydırıcılar (`RgbSliderPainter`) ve neon butonlar (`NeonButton`, `VoiceCardButton`).
- **4 RGB Tema Paleti:** `Kroma RGB`, `Siber Neon`, `Kızıl Alev`, `Aurora Matris`.
- **Canlı 8-Bant Spektrum Analizörü & Giriş/Çıkış VU Metre:** Konuşma anında gerçek zamanlı görsel geri bildirim.
- **Yenilenen Hızlı Ses Seçici (`J`):** Canlı arama çubuğu, kategori sekmeleri, `★` favori yıldızlama ve `[YENİ]` rozetleri.

### 4. Tam Türkçe (Varsayılan) + İngilizce Dil Sistemi
- Varsayılan dil **Türkçe (`tr_tr`)** olarak ayarlandı; üst bardan tek tıkla `Türkçe` / `English` / `Otomatik` arasında anında geçiş yapılabilir.

### 5. Kısayollar ve Sunucu Komutları
- Kısayollar: **`J`** (Hızlı Ses Seçici), **`U`** (Tam Ses Stüdyosu), **`O`** (Aç/Kapat), **`I`** (Kendini Dinle).
- Sunucu komutları: `/raufixvc` ve kısa `/rvc` (`status`, `allow`, `block`, `restrict`, `player`, `reload`).
