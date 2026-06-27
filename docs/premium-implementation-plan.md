# Premium Gelistirme Implementasyon Plani

Bu dokuman, LyraApp premium gelistirmesi icin uygulanacak kapsam, API sozlesmeleri,
dosya dokumu ve batch planini tanimlar. Implementasyona baslamadan once kullanici
onayi alinacaktir.

## 1. Dogrulanan API Kaynagi

- Kaynak: `https://streaming-api.halitkalayci.com/docs/#/`
- OpenAPI: `https://streaming-api.halitkalayci.com/docs/openapi.json`
- Base URL: `https://streaming-api.halitkalayci.com/`

Bu dokuman disindaki `tickets-api` servisi LyraApp icin kullanilmayacaktir.

## 2. Dogrulanan Endpointler

### 2.1. Membership

- `GET /api/v1/memberships/plans`
  - Premium plan listesini dondurur.
  - Plan tipleri: `one-time`, `recurring`.
  - Alanlar: `id`, `type`, `name`, `description`, `priceKurus`, `price`, `currency`,
    `durationDays`, `autoRenew`.

- `POST /api/v1/memberships/checkout`
  - Mock kart odemesi ile premium uyelik satin alir.
  - Bearer token gerektirir.
  - Request:
    - `plan`: `one-time` veya `recurring`
    - `card.number`
    - `card.expMonth`
    - `card.expYear`
    - `card.cvc`
    - `card.holderName`
  - Test kartlari:
    - `4242 4242 4242 4242`: basarili
    - `4000 0000 0000 0002`: reddedilir

### 2.2. Profil

- `GET /api/v1/me`
  - Kullanici profilini dondurur.
  - `membership` alani free kullanicida `null`, premium kullanicida aktif uyelik
    bilgisi olarak gelir.
  - Membership alanlari: `planId`, `type`, `status`, `autoRenew`, `startedAt`, `expiresAt`.

### 2.3. Playback

- `POST /api/v1/me/playback/next`
  - Calinacak siradaki ogeleri backend belirler.
  - Premium kullanici icin `type: "song"` doner.
  - Free kullanici icin her 3 sarkida bir `type: "ad"` doner.
  - Ad response icinde reklam, reklam stream linki, `impressionId`, asil sarki ve sarki
    stream linki birlikte gelir.
  - Bu endpoint play kaydini da tuttugu icin ayrica `POST /api/v1/me/plays`
    cagrilmamalidir.

- `POST /api/v1/me/playback/ad-complete`
  - Reklam tamamlaninca `impressionId` ile bildirim yapilir.

- `GET /api/v1/songs/{id}/stream-url`
  - Premium only olarak kullanilmalidir.
  - Free kullanici direkt bu endpointi kullanmamalidir; `playback/next` akisi
    kullanilmalidir.

## 3. Tasarim Kapsami

Gonderilen gorseller referans alinacaktir:

- Profil yeni ekran:
  - Profil ust bolumu.
  - `Premium - 3 gun kaldi` membership karti.
  - Gorunum secimi.
  - Ayarlar listesi.

- Premium satin alma ekranlari:
  - `LyraApp Premium` basligi.
  - Fayda listesi.
  - Aylik abonelik ve tek seferlik plan secimi.
  - `Devam et` aksiyonu.

- Odeme ekrani:
  - Kart preview alani.
  - Kart numarasi, isim, son kullanma, CVC alanlari.
  - Secili plan ozeti.
  - Odeme butonu.

- Tek seferlik bitis popup:
  - Premium bitisine kalan sure.
  - Aylik abonelige gecme aksiyonu.
  - 30 gun yenileme aksiyonu.
  - Daha sonra aksiyonu.

- Odeme basarili ekrani:
  - Premium aktivasyon onayi.
  - Secilen planin sure/uyelik bilgisi.
  - `Dinlemeye basla` aksiyonu.

## 4. Spotify Benzeri Akis Kurallari

### 4.1. Premium Giris Noktalari

Premium akisi tek bir route uzerinden acilacak, farkli giris noktalarinda yalnizca
secili plan veya niyet degisecektir.

- Profil membership karti:
  - Kullanici free ise kart `Premium'a gec` aksiyonu gibi davranir.
  - Kullanici one-time premium ise kart uyelik bitisine kalan sureyi gosterir.
  - Kullanici recurring premium ise kart aktif abonelik durumunu gosterir.
  - Karta tiklaninca premium plan secimi ekranina gidilir.

- Yenileme hatirlaticisi popup:
  - Yalnizca `membership.type = one-time`, `autoRenew = false`,
    `status = active` ve `expiresAt` tarihine 3 gun veya daha az kaldiysa
    gosterilir.
  - `Aylik abonelige gec` aksiyonu, `recurring` plan secili olacak sekilde odeme
    akisina gider.
  - `30 gun yenile` aksiyonu, `one-time` plan secili olacak sekilde odeme akisina
    gider.
  - `Daha sonra` aksiyonu sadece popup'i kapatir.

- Free kullanici premium-only beklentiye girdiginde:
  - UI premium durumunu tahmin ederek playback'i engellemez.
  - Playback karari `POST /api/v1/me/playback/next` tarafindan verilir.
  - Premium avantajlari profil karti ve premium ekraninda sunulur.

### 4.2. Plan Secimi

- Planlar her zaman `GET /api/v1/memberships/plans` uzerinden alinacaktir.
- Fiyatlar, sureler ve auto-renew bilgisi hardcode edilmeyecektir.
- Varsayilan secim Spotify benzeri olarak recurring plan olacaktir.
- Kullanici plan degistirirse odeme ekranina secili plan ile gidilecektir.

### 4.3. Odeme ve Basari Akisi

- Odeme formu tamamlanmadan odeme butonu aktif olmayacaktir.
- Checkout basarili olursa odeme basarili ekranina gidilecektir.
- Basari ekranindan `Dinlemeye basla` ile Home ekranina donulecektir.
- Basarili checkout sonrasi profil verisi tekrar okunarak membership bilgisi
  guncellenecektir.

### 4.4. Popup Gosterim Kurali

- Popup state icinde kalici bir navigasyon flag'i tutulmayacaktir.
- Popup kapatma kullanici niyeti olarak ele alinacaktir.
- Konfigurasyon degisiminde ayni oturumda surekli tekrar acilmasini engellemek icin
  ViewModel state'i icinde `hasDismissedRenewalReminder` benzeri gecici state
  tutulacaktir.
- Uygulama tekrar acildiginda API membership bilgisine gore popup yeniden
  degerlendirilebilir.

## 5. Bagimlilik Matrisi

Yeni bagimlilik eklenmeyecektir.

| Kutuphane | Mevcut Kullanim | Degisiklik | Gerekce |
| --- | --- | --- | --- |
| Retrofit | Var | Yok | API cagri katmani icin yeterli. |
| kotlinx.serialization | Var | Yok | DTO modelleme icin yeterli. |
| Hilt | Var | Yok | Repository ve API injection icin yeterli. |
| Jetpack Compose Material3 | Var | Yok | Premium, odeme ve popup UI icin yeterli. |
| Media3/ExoPlayer | Var | Yok | Playback/advertising queue akisi icin yeterli. |

## 6. Dosya Dokumu ve Batch Plani

Proje kurali geregi tek seferde en fazla 5 alakali dosya ile calisilacaktir.

### Batch 1: API ve Model Temeli

- `docs/decisions.md`
  - Premium, membership ve server-authoritative playback kararinin kaydi.
- `app/src/main/java/com/turkcell/lyraapp/data/auth/AuthApi.kt`
  - `UserDto` icine `membership` modeli eklenecek.
- `app/src/main/java/com/turkcell/lyraapp/data/me/MeApi.kt`
  - Playback ve ad-complete endpointleri eklenecek.
- `app/src/main/java/com/turkcell/lyraapp/data/profile/UserProfile.kt`
  - Membership bilgisi domain/UI modeline eklenecek.
- `app/src/main/java/com/turkcell/lyraapp/data/profile/DefaultProfileRepository.kt`
  - Mock `status = "Premium"` kaldirilip API membership verisi kullanilacak.

### Batch 2: Premium Veri Katmani

- `app/src/main/java/com/turkcell/lyraapp/data/premium/PremiumApi.kt`
  - `memberships/plans` ve `memberships/checkout` Retrofit endpointleri.
- `app/src/main/java/com/turkcell/lyraapp/data/premium/PremiumModels.kt`
  - Plan, checkout request, payment ve membership DTO modelleri.
- `app/src/main/java/com/turkcell/lyraapp/data/premium/PremiumRepository.kt`
  - Premium veri sozlesmesi.
- `app/src/main/java/com/turkcell/lyraapp/data/premium/DefaultPremiumRepository.kt`
  - Gercek API implementasyonu.
- `app/src/main/java/com/turkcell/lyraapp/di/PremiumModule.kt`
  - Repository binding.

### Batch 3: Premium Ekrani ve Navigasyon

- `app/src/main/java/com/turkcell/lyraapp/ui/premium/PremiumContract.kt`
  - Premium ekran State, Intent, Effect sozlesmesi.
- `app/src/main/java/com/turkcell/lyraapp/ui/premium/PremiumViewModel.kt`
  - Plan listeleme ve plan secme mantigi.
- `app/src/main/java/com/turkcell/lyraapp/ui/premium/PremiumScreen.kt`
  - Referans gorsellere uygun premium ekran UI.
- `app/src/main/java/com/turkcell/lyraapp/ui/payment/PaymentSuccessScreen.kt`
  - Odeme basarili ekrani.
- `app/src/main/java/com/turkcell/lyraapp/ui/navigation/LyraDestination.kt`
  - Premium ve odeme rotalari.

### Batch 4: Odeme Ekrani

- `app/src/main/java/com/turkcell/lyraapp/ui/payment/PaymentContract.kt`
  - Odeme State, Intent, Effect sozlesmesi.
- `app/src/main/java/com/turkcell/lyraapp/ui/payment/PaymentViewModel.kt`
  - Kart formu, validasyon ve checkout aksiyonu.
- `app/src/main/java/com/turkcell/lyraapp/ui/payment/PaymentScreen.kt`
  - Referans odeme ekranina uygun UI.
- `app/src/main/java/com/turkcell/lyraapp/ui/navigation/LyraNavHost.kt`
  - Premium, odeme ve odeme basarili ekranlarinin navigasyon entegrasyonu.
- `app/src/main/java/com/turkcell/lyraapp/ui/profile/ProfileContract.kt`
  - Premium kart ve popup niyetleri icin state/intent genisletmesi.
- `app/src/main/java/com/turkcell/lyraapp/ui/profile/ProfileViewModel.kt`
  - Membership state ve premium yonlendirme etkileri.

### Batch 5: Profil UI ve Premium Popup

- `app/src/main/java/com/turkcell/lyraapp/ui/profile/ProfileScreen.kt`
  - Yeni profil tasarimi, membership karti ve bitis popup entegrasyonu.
- `app/src/main/java/com/turkcell/lyraapp/ui/icons/LyraIcons.kt`
  - Gerekli eksik ikonlar varsa eklenecek.
- `app/src/main/java/com/turkcell/lyraapp/ui/theme/Color.kt`
  - Gerekirse mevcut temaya uygun renk sabitleri eklenecek.

Not: Bu batch 3 dosya ile sinirlidir. Ek dosya ihtiyaci cikarsa ayri onay istenecektir.

### Batch 6: Playback ve Reklam Akisi

- `app/src/main/java/com/turkcell/lyraapp/data/player/PlayerRepository.kt`
  - `resolveNextPlayback` ve `completeAd` sozlesmeleri.
- `app/src/main/java/com/turkcell/lyraapp/data/player/DefaultPlayerRepository.kt`
  - `playback/next` ve `playback/ad-complete` implementasyonu.
- `app/src/main/java/com/turkcell/lyraapp/data/player/GlobalPlayerManager.kt`
  - Free kullanici icin ad + song queue; premium icin direkt song akisi.
- `app/src/main/java/com/turkcell/lyraapp/ui/player/PlayerContract.kt`
  - Reklam durumu, next akisi ve hata state alanlari.
- `app/src/main/java/com/turkcell/lyraapp/ui/player/PlayerViewModel.kt`
  - Player intentlerinin yeni repository akisina baglanmasi.

## 7. Happy-Path Test Plani

Implementasyon sonrasi mumkunse asagidaki testler calistirilacaktir:

1. Uygulama build:
   - `./gradlew.bat :app:assembleDebug`

2. Premium plan akisi:
   - Premium ekran acilir.
   - `GET /api/v1/memberships/plans` ile planlar listelenir.
   - Aylik plan secilir.
   - Odeme ekranina gidilir.
   - `4242 4242 4242 4242` karti ile checkout basarili olur.
   - Profilde membership aktif gorunur.

3. Reddedilen odeme:
   - `4000 0000 0000 0002` karti ile checkout denenir.
   - Kullaniciya hata mesaji gosterilir.

4. Free playback:
   - Free kullanici sarki calmak istediginde `playback/next` kullanilir.
   - Backend reklam dondururse once reklam calinir.
   - Reklam bitince `ad-complete` cagrilir.
   - Ardindan asil sarki calinir.

5. Premium playback:
   - Premium kullanici icin `playback/next` direkt `song` dondurur.
   - Reklam gosterilmez.

## 8. Sik Yapilan Hatalar ve Onlemler

- Free kullanicida `songs/{id}/stream-url` endpointini direkt cagirmak.
  - Onlem: Playback akisi `playback/next` uzerinden merkezilestirilecek.

- `playback/next` kullanildiktan sonra ayrica `me/plays` cagirmak.
  - Onlem: Yeni akista play kaydi tekrar atilmayacak.

- Reklam tamamlaninca `impressionId` gondermemek.
  - Onlem: Ad state icinde `impressionId` tutulacak ve reklam bitisinde
    `ad-complete` cagrilacak.

- Membership bilgisini mock olarak gostermek.
  - Onlem: Profil tamamen `GET /api/v1/me` response icindeki `membership`
    alanindan beslenecek.

- Odeme ekraninda fiyatlari hardcode etmek.
  - Onlem: Plan fiyatlari `memberships/plans` response uzerinden gosterilecek.

## 9. Onay Durumu

Kullanici tarafindan implementasyon onayi verilmistir. Batch 1 ile baslanacak, her batch
en fazla 5 alakali dosya olacak sekilde ilerleme korunacaktir.
