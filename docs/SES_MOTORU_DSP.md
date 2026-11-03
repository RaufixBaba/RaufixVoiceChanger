# 🎛️ Raufix Voice Changer V1.0 BETA — DSP Ses Motoru Mimarisi

Raufix Voice Changer, gerçek zamanlı ses iletişiminde (`48.000 Hz`, `16-bit PCM`, `20 ms / 960 örnek` çerçeve boyutu) minimum gecikme ile maksimum vokal doğallığı sağlamak üzere tasarlanmış çok aşamalı bir dijital sinyal işleme (`VoiceChannelChain`) mimarisi kullanır.

---

## 🔗 Sinyal İşleme Zinciri (`VoiceChannelChain`)

```text
Mikrofon Girişi (48 kHz PCM)
   │
   ▼
1. Gürültü Kapısı (NoiseGate) + 75 Hz Rumble Yüksek Geçiren Filtre (HPF)
   │
   ▼
2. Konuşmacı F0 Takipçisi (SpeakerPitchTracker) + Müzikal Autotune (AutotuneTracker)
   │
   ▼
3. Formant Korumalı Spektral Faz Vokoder (VocalPitchShifter — 1024 FFT / 128 Hop)
   │   ├── Düzleştirilmiş Kepstral Formant Zarfı Ayırma
   │   ├── Geçici Darbe (Transient) Faz Kilitleme (Plosive netliği)
   │   ├── >3.6 kHz Ötümsüz Ünsüz / Sibilant Koruması (Türkçe s, ş, ç, t, k)
   │   ├── Kesirsel Yukarı Örnekleme (Tarak filtresi deliklerini önler)
   │   └── Canlı 8-Bant Görsel Spektrum Çıkarımı
   │
   ▼
4. Dinamik S-Yumuşatıcı (DeEsser — 4.5–9 kHz sibilant kontrolü)
   │
   ▼
5. Doğal Vokal Gerçekçilik Katmanı (VocalRealismStage)
   │   ├── Berraklık (Clarity): 185 Hz çamur kesimi + 2.75 kHz vokal varlık rafı
   │   ├── Nefeslilik (Breathiness): Zarf-kapılı 2.5–6.8 kHz glottal hava sentezi
   │   ├── Tüp Sıcaklığı (Warmth): Optik seviyeleyici + asimetrik çift harmonik tüp doygunluğu
   │   └── Stüdyo Koro (Chorus): Çift LFO'lu (0.73 Hz / 1.19 Hz) mikro-gecikme genişletici
   │
   ▼
6. 3-Bant Ton Yığını (ToneStack: Bas / Orta / Tiz)
   │
   ▼
7. Karakter & Efekt Katmanı (CharacterStage: Alt Harmonik, Halka Mod., Bit Ezici, Fısıltı, Telsiz)
   │
   ▼
8. Akustik Mekân Yankısı (RoomReverb) + Yumuşak Tavan Sınırlayıcı (SoftLimiter)
   │
   ▼
Simple Voice Chat Opus Kodlayıcı / Kendini Dinle Monitörü
```

---

## 🧪 Neden Kız ve Erkek Sesleri Çok Daha Doğal Çıkıyor?

Klasik gerçek zamanlı perde kaydırıcılarda erkek sesini (`~120 Hz`) kadın sesine (`~195 Hz`, yaklaşık `1.62x`) taşırken iki büyük akustik bozulma yaşanır:

1. **Formant Kayması ("Sincap / Helyum" Etkisi):** Ham perde kaydırma, ses tellerinin frekansıyla birlikte boğaz/ağız boşluğu rezonanslarını (`F1, F2, F3`) da `1.62x` yukarı iter. Raufix DSP motoru, spektral zarfı kepstral yumuşatma ile ayırır; temel perde `1.62x` yükselirken formantlar yalnızca gerçek insan kadın vokal yolu oranında (`~+3.0` yarım ton ≈ `1.19x`) kaydırılır.
2. **Spektral Boşluklar & Sibilant Bozulması:** Basit bin kopyalama yöntemleri yukarı kaydırmada komşu frekans kutularını boş bırakarak metalik çınlama yaratır ve `s / ş / ç` seslerini ıslığa çevirir. `VocalPitchShifter`, ters yönde doğrusal büyüklük/faz enterpolasyonu kullanır ve ötümsüz ünsüz anlarında yüksek frekans bandını doğrudan korur.
