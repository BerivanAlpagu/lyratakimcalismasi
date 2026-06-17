# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

### Dependency Injection Kütüphanesi

- Seçim*: **Hilt**

- Son Güncelleme Tarihi*: 04.06.2026

- Alternatifler: **Koin**

- Sebep: **Opsiyonel**


### Navigasyon

- Seçim: **Compose Navigation**

- Son Güncelleme Tarihi: 09.06.2026

- Bağımlılık: `androidx.navigation:navigation-compose` **2.9.5** (version catalog: `navigationCompose`).

- Uygulama: Tek `NavHost` (`ui/navigation/LyraNavHost.kt`) Auth grafiğini barındırır (başlangıç
  hedefi Login). Navigasyon MVI ile uyumlu kurulur: ViewModel'de navigasyon API'si yoktur
  (bkz. [architecture/mvi-viewmodel-rules.md](architecture/mvi-viewmodel-rules.md) §6); navigasyon
  `Intent → Effect` üzerinden akar, `Route` Effect'i tüketip `NavHost`'tan gelen lambda'ları çağırır.


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 09.06.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve
  referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 09.06.2026

- Sürümler: Hilt **2.59.2**, KSP **2.2.10-2.0.2** (Kotlin 2.2.10 ile birebir uyumlu).

- Compose'da ViewModel: `androidx.hilt:hilt-lifecycle-viewmodel-compose` (`hiltViewModel()`).
  Compose Navigation henüz kurulmadığından navigation-compose bağımlılığı eklenmemiştir.

- Sebep: KSP, kapt'a göre belirgin biçimde hızlıdır ve Kotlin 2.2 ile uyumludur.


### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: `gradle.properties` içinde **`android.disallowKotlinSourceSets=false`** zorunludur.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: AGP 9 built-in Kotlin kullanır; KSP'nin ürettiği kaynak dizinlerini eklemesi bu bayrak
  olmadan derlemeyi kırar. Bayrak deneysel (experimental) olarak işaretlidir ancak gereklidir.


### Alt Gezinme Çubuğu (Bottom Navigation Bar)

- Seçim: **Material 3 `NavigationBar`** — tek `NavHost` + iskelet seviyesinde tek dış `Scaffold`.

- Son Güncelleme Tarihi: 11.06.2026

- Uygulama: `ui/navigation/LyraBottomBar.kt` (bileşen + `LyraBottomBarTab` sekme tanımları) ve
  `ui/navigation/LyraNavHost.kt` (Scaffold `bottomBar` entegrasyonu). Çubuk yalnızca üst düzey
  sekme rotalarında görünür (Auth ekranlarında gizli); böylece her ana sayfanın altında otomatik
  yer alır. Sekme geçişi standart desenle yapılır: `popUpTo(Home) { saveState = true }` +
  `launchSingleTop` + `restoreState`. Dış Scaffold'ın `contentWindowInsets`'i sıfırdır; sistem
  çubuğu boşluklarını ekranlar kendisi yönetir, içerik dolgusu yalnızca alt çubuk yüksekliğini taşır.

- MVI kapsamı: BNB navigasyon iskeletidir (chrome), feature ekranı değildir; State/Intent/Effect
  sözleşmesi yoktur. Seçili sekme `currentBackStackEntryAsState()` ile nav back stack'ten türetilir
  (tek doğruluk kaynağı back stack'tir). Sekme ekranları MVI ile yazıldığında yalnızca
  `LyraNavHost` içindeki geçici placeholder rotaları gerçek `Route`'lara bağlanacaktır.

- Sebep: Tek doğruluk kaynağı (back stack) ile durum tekrarına yer bırakmaz; sekme başına ayrı
  `NavHost`/ViewModel karmaşıklığından kaçınılır; mevcut Auth grafiği değişmeden korunur.


### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API
  geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.


### Ağ Katmanı (Networking)

- Seçim: **Retrofit + OkHttp + kotlinx.serialization**

- Son Güncelleme Tarihi: 16.06.2026

- Sürümler: Retrofit **2.11.0** (`retrofit` + `converter-kotlinx-serialization`), OkHttp **4.12.0**
  (`okhttp` + `logging-interceptor`), kotlinx-serialization-json **1.8.1**, Kotlin serialization
  plugin Kotlin sürümüyle birebir (**2.2.10**).

- Uygulama: `data/network/NetworkModule.kt` (`@Provides` ile `Json`, `OkHttpClient`, `Retrofit`,
  `SongsApi`). Base URL `https://streaming-api.halitkalayci.com/` (OpenAPI servers[0]). `Json`,
  bilinmeyen alanları yok sayar (`ignoreUnknownKeys`), bu nedenle DTO'lar şemanın tamamını
  modellemez. `INTERNET` izni `AndroidManifest.xml`'e eklenmiştir.

- Sebep: Android'de en yaygın yığın; Hilt ile temiz `@Provides` entegrasyonu, kotlinx.serialization
  Kotlin-native ve reflection'sızdır.


### Şarkı Listesi API Entegrasyonu

- Karar: Ana sayfadaki **"Şarkılar"** bölümü `GET /api/v1/songs` ile beslenir; diğer bölümler
  (quickPicks, recentlyPlayed, playlists) backend hazır olmadığından **statik** kalır.

- Son Güncelleme Tarihi: 16.06.2026

- Uygulama: `data/songs/` (Retrofit `SongsApi` + `SongDto`/`SongsPageDto`),
  `data/home/DefaultHomeRepository.kt` (API'dan çeker + statik bölümleri verir; `HomeModule`
  bağlaması `MockHomeRepository`'den buraya çevrildi, mock sınıfı silindi). Bu fazda yalnızca ilk
  sayfa çekilir (`limit=20`); `nextCursor` (sonsuz kaydırma) sonraki faza bırakılmıştır.

- Kapak renkleri: API'da görsel/BG bilgisi yok. Her şarkıya **`id`'den deterministik** türetilen
  gradyan renk çifti atanır (HSL→ARGB; ton id hash'inden). Saf rastgele yerine deterministik
  seçildi: aynı şarkı her zaman aynı rengi alır, recomposition/yeniden yüklemede titreme olmaz.

- Çalma (playback): Bu faz **kapsam dışı**. Tıklama/oynatma (Media3/ExoPlayer, iki adımlı
  `stream-url` akışı) sonraki faza bırakılmıştır; şarkı satırları şu an davranışsızdır.