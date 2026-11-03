<div align="center">

# 🎙️ Raufix Voice Changer — `V1.0 BETA`

**Minecraft `1.21.11` (Fabric) + Simple Voice Chat `2.6.x` için Yeni Nesil RGB Stüdyo & Doğal Vokal DSP Ses Değiştirici**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-00E5FF?style=for-the-badge)](https://minecraft.net)
[![Mod Loader](https://img.shields.io/badge/Loader-Fabric%200.18+-9D4EDD?style=for-the-badge)](https://fabricmc.net)
[![Simple Voice Chat](https://img.shields.io/badge/Simple%20Voice%20Chat-2.6.x-FF2A85?style=for-the-badge)](https://modrinth.com/plugin/simple-voice-chat)
[![Dil](https://img.shields.io/badge/Dil-Türkçe%20%2B%20English-38F29A?style=for-the-badge)](#)
[![Geliştirici](https://img.shields.io/badge/Geliştirici-RaufixBaba-FFB800?style=for-the-badge)](#)

</div>

---

## ✨ Neden Raufix Voice Changer V1.0 BETA?

**Raufix Voice Changer**, Minecraft **Simple Voice Chat** üzerinde çalışan gerçek zamanlı (`48 kHz`, ultra düşük gecikmeli) bir stüdyo ses değiştiricidir. Sıradan perde kaydırıcıların oluşturduğu "helyum / sincap / metalik robot" yapaylığını ortadan kaldırmak için sıfırdan tasarlanmış **Formant Korumalı Spektral Faz Vokoder (`VocalPitchShifter`)** ve **Doğal Vokal Gerçekçilik Katmanı (`VocalRealismStage`)** ile güçlendirilmiştir.

### 🔥 V1.0 BETA Öne Çıkan Özellikler

1. **🎧 Stüdyo Kalitesinde Doğal Kız & Erkek Sesi (DSP v2.0)**
   - **Kesirsel Spektral Faz Vokoder (`1024-nokta FFT`, `%87.5` Hann örtüşme):** Yukarı perde kaydırmada harmonikler arasında tarak filtresi boşluğu bırakmaz; ses pürüzsüz, dolgun ve doğal çıkar.
   - **Yumuşatılmış Kepstral Formant Zarfı & F0 Takibi:** Kendi sesinizin temel frekansını (`70–420 Hz`) gerçek zamanlı ölçer; vokal yolu rezonansını ayrı tutarak kulağa gerçek insan gibi gelen kadın, erkek ve çocuk sesleri üretir.
   - **Türkçe Ünsüz & Sibilant Koruması (`s`, `ş`, `ç`, `t`, `k`):** `3.6 kHz` üzeri ötümsüz ünsüzleri perde kaydırma bozulmasından korur ve dinamik **S-Yumuşatıcı (De-Esser)** ile kulak tırmalamasını önler.
   - **4 Yeni Vokal Gerçekçilik Parametresi (Toplam 30 Parametre):**
     - `Berraklık (Clarity)`: `185 Hz` kutu/boğukluk kesimi + `2.75 kHz` stüdyo vokal netliği.
     - `Nefeslilik (Breathiness)`: Zarfla kapılanmış (`2.5–6.8 kHz`) doğal glottal hava dokusu (konuşmadığınızda %100 sessiz kalır).
     - `Tüp Sıcaklığı (Warmth)`: Yumuşak optik seviyeleme + çift harmonik tüp doygunluğu.
     - `Stüdyo Koro (Chorus)`: Çift LFO mikro-detune vokal genişletici.

2. **🌈 Premium RGB Neon Stüdyo Arayüzü**
   - **4 Canlı RGB Tema Paleti:** `Kroma RGB`, `Siber Neon`, `Kızıl Alev`, `Aurora Matris` — tek tıkla anında geçiş.
   - **Canlı 8-Bant RGB Spektrum Analizörü & VU Metre:** Konuşurken sesinizin frekanslarını gerçek zamanlı izleyin.
   - **Hızlı Ses Seçici Kart Izgarası:** Kategori filtreleri (`Tümü`, `Doğal`, `Karakter`, `Fantastik`, `Bilim Kurgu / Efekt`, `Kayıtlı`, `★ Favori`), canlı arama kutusu, favori yıldızlama ve `[YENİ]` V1 rozetleri.

3. **🔊 %100 OpenAL Canlı Dinleme & Mobil (MojoLauncher / PojavLauncher) + PC Uyumluluğu**
   - `javax.sound.sampled` çakışmaları tamamen kaldırılarak Simple Voice Chat'in kendi **OpenAL `SoundManager` + `SpeakerManager` + `MicThread`** altyapısına entegre edildi.
   - Hem stüdyo menüsünde hem de oyun içinde **`[I]`** tuşuyla kendi değiştirilmiş sesinizi oyun sesini kesmeden, sıfır mikrofon kopmasıyla anlık dinleyebilirsiniz!
   - Stüdyo içinden tek tıkla **Bas-Konuş (`PTT`) ↔ Otomatik Ses Algılama (`VAD`)**, **Mikrofon Aç/Kapat**, **OpenAL Ses Yenile (`↻`)** ve **SVC Ayarları (`⚙`)** kontrolü!

4. **🎭 26 Kalibreli Hazır Stüdyo Sesi (+ Tek Tık Normal Ses Kartı)**
   - **Doğal İnsan (9):** `Doğal Kız Sesi`, `Yumuşak Kız Sesi (Soft)`, `Olgun Kadın Sesi`, `Doğal Erkek Sesi`, `GigaChad Bass` *(Yeni)*, `Derin Radyo Spikeri`, `Genç Erkek`, `Çocuk Sesi`, `Yaşlı Bilge Adam`
   - **Sinematik Karakter (6):** `Anime Kızı (Kawaii)`, `Karanlık Şövalye`, `Telefon Katili (Ghostface)`, `Karanlık Lord (Kasklı)`, `Kadim Büyücü`, `Telsiz Komutanı` *(Yeni)*
   - **Fantastik & Korku (6):** `Devasa Titan`, `Cehennem İblisi`, `Mağara Trolü` *(Yeni)*, `Gölge Azrail`, `Hortlak / Zombi`, `Semavi Melek`
   - **Bilim Kurgu, Eğlence & Müzik (5):** `Taktiksel Sayborg`, `Kozmik Uzaylı`, `Askeri Telsiz`, `Sincap (Helyum)` *(Yeni)*, `Trap Rapçi Autotune`

5. **🇹🇷 Tam Türkçe (Varsayılan) + 🇬🇧 İngilizce Dil Desteği**
   - Mod ilk açılışta **otomatik olarak Türkçe** başlar (Minecraft'ınız İngilizce olsa bile!).
   - Üst bardaki **`Dil: Türkçe / EN / Oto`** butonuyla oyunu yeniden başlatmadan anında dil değiştirebilirsiniz.

5. **🤖 İsteğe Bağlı Yapay Zekâ (AI / RVC) Ses Desteği + Sunucu Yönetimi**
   - Yerel RVC / w-okada motorları veya ElevenLabs bulut dönüşümü için hazır AI ekranı.
   - Sunucu sahipleri için `/raufixvc` (ve kısa `/rvc`) yönetim komutları ve paylaşılan sunucu ses kütüphanesi.

---

## 📦 Hızlı Kurulum (Hazır `.jar` — Derleme Gerekmez!)

Hazır derlenmiş mod dosyası doğrudan `releases/` klasöründe yer alır:

👉 **[`releases/raufix-voice-changer-1.0.0-BETA+1.21.11.jar`](releases/raufix-voice-changer-1.0.0-BETA+1.21.11.jar)**

1. **Minecraft `1.21.11`** için **Fabric Loader (`0.18.0+`)** kurun.
2. `mods/` klasörünüze şu 3 dosyayı atın:
   - **Fabric API** (`1.21.11`)
   - **Simple Voice Chat** (`fabric-1.21.11-2.6.x`)
   - **`raufix-voice-changer-1.0.0-BETA+1.21.11.jar`**
3. Oyuna girin ve kontrollerin keyfini çıkarın!

---

## ⌨️ Oyun İçi Kısayollar & Erişim

| İşlem | Varsayılan Tuş / Yöntem |
| :--- | :--- |
| **Hızlı Ses Seçiciyi Aç** | **`J`** tuşu *(Ayarlar > Kontroller altından değiştirilebilir)* |
| **Tam RGB Ses Stüdyosunu Aç** | **`U`** tuşu veya Simple Voice Chat (`V`) menüsündeki **RGB Mikrofon** ikonu |
| **Ses Değiştiriciyi Aç / Kapat** | **`O`** tuşu |
| **Kendini Dinle (Mikrofon Testi)** | **`I`** tuşu veya Stüdyo alt barındaki **Kendini Dinle** butonu |
| **Sunucu Komutları (OP)** | `/raufixvc status`, `/raufixvc allow`, `/raufixvc block`, `/rvc reload` |

> 💡 **İpucu:** Simple Voice Chat menüsündeki (`V`) mikrofon butonuna normal tıkladığınızda **Hızlı Ses Seçici**, **`Shift` + Sol Tık** yaptığınızda doğrudan **Tam Ses Stüdyosu** açılır!

---

## 🛠️ Kaynak Koddan Derleme (Geliştiriciler İçin)

Temiz kaynak kod arşivi ayrıca [`releases/RaufixVoiceChanger-1.0.0-BETA-src.zip`](releases/RaufixVoiceChanger-1.0.0-BETA-src.zip) içinde paketlenmiştir.

```bash
# Java 21 gereklidir
./gradlew build
# Çıktı: build/libs/raufix-voice-changer-1.0.0-BETA+1.21.11.jar
```

---

## 📚 Ek Dokümantasyon

- 📄 **[Sürüm Notları (`SURUM_NOTLARI.md`)](SURUM_NOTLARI.md)** — V1.0 BETA yenilikleri ve değişiklik özeti
- 🎭 **[Ses Kataloğu (`docs/SES_KATALOGU.md`)](docs/SES_KATALOGU.md)** — 22 ses profilinin akustik karakterleri ve kullanım önerileri
- 🎛️ **[DSP Ses Motoru Mimarisi (`docs/SES_MOTORU_DSP.md`)](docs/SES_MOTORU_DSP.md)** — Spektral Faz Vokoder ve Vokal Gerçekçilik sinyal zinciri

---

## 🇬🇧 English Quick Summary

**Raufix Voice Changer V1.0 BETA** (created by **RaufixBaba**) is a next-generation real-time voice studio addon for **Simple Voice Chat (`2.6.x`)** on **Minecraft `1.21.11` (Fabric)**:
- **Natural Vocal DSP Engine v2.0:** Fractional upward spectral phase vocoder (`1024-pt FFT`, `87.5%` Hann overlap), smoothed cepstral formant preservation, unvoiced consonant/sibilant protection (`>3.6 kHz`), adaptive speaker F0 tracking, and 4 studio vocal realism stages (`Clarity`, `Breathiness`, `Tube Warmth`, `Dual-LFO Chorus`).
- **22 Built-in Voices** across Natural Human, Character, Fantasy/Horror, and Sci-Fi/Music categories (15 brand-new V1 Beta voices!).
- **Animated RGB Neon UI:** 4 live RGB palettes, 8-band real-time spectrum visualizer, search & favorites filter, and 1-click **Turkish / English / Auto** language switching in the top header.
- **Ready-to-Play JAR:** Grab `releases/raufix-voice-changer-1.0.0-BETA+1.21.11.jar` and drop it straight into `.minecraft/mods/` alongside Fabric API and Simple Voice Chat!

---

<div align="center">
<b>Geliştirici / Author:</b> <code>RaufixBaba</code> • <b>Sürüm:</b> <code>1.0.0-BETA+1.21.11</code> • <b>Lisans:</b> MIT
</div>
