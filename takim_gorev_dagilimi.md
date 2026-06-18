# LyraApp - Takım Görev Dağılımı ve Branch Stratejisi

> 3 kişilik ekip için ekranlar arası bağımlılık minimize edilerek hazırlanmıştır.
> Mevcut durum: Ana Sayfa, Login, Register ekranları tamamlanmış. Kalan 4 ekran paylaştırılacak.

---

## Ekran Bağımlılık Analizi

### API Bağımlılıkları (OpenAPI'den çıkarıldı)

| Ekran | API Endpoint | Bağımlılık Türü |
|-------|-------------|-----------------|
| Arama | `GET /api/v1/songs?q=...` | `SongsApi` (mevcut, paylaşılan) |
| Kütüphane | `GET /api/v1/playlists` + `GET /api/v1/playlists/{id}` | Yeni `PlaylistsApi` |
| Favoriler | Backend endpoint YOK | `FakeFavoritesRepository` (local-only) |
| Profil | Backend endpoint YOK | `FakeProfileRepository` (local-only) |

### Kod Bağımlılıkları

- Arama ekranı `SongsApi`'yi yeniden kullanır → `data/songs/SongsApi.kt` ve `SongDto.kt` **sadece okuma** yapacak.
- Kütüphane ekranı yeni `PlaylistsApi`, `PlaylistDto`, `PlaylistRepository` oluşturur → bağımsız `data/library/` paketi.
- Favoriler ve Profil'in backend bağımlılığı yoktur; tamamen birbirinden bağımsız.
- 4 ekran da `LyraNavHost.kt`'deki `PlaceholderScreen` satırlarını kendi `Route` composable'larıyla değiştirir → **bu dosyada çakışma riski** (bkz. aşağıda çözüm).

---

## Görev Dağılımı

### Kişi A — `feature/search`

**Ekran:** Arama (Search)

**Kapsam:**
- `ui/search/SearchContract.kt` — `SearchUiState`, `SearchIntent`, `SearchEffect`
- `ui/search/SearchViewModel.kt` — `@HiltViewModel`, arama query'si debounce + `SongsApi` çağrısı
- `ui/search/SearchScreen.kt` — `SearchRoute` + `SearchScreen` (composable'lar)
- `LyraNavHost.kt` içindeki `PlaceholderScreen("Ara")` satırı → `SearchRoute(...)` ile değiştirilir

**Kullandığı mevcut dosyalar (salt okunur):**
- `data/songs/SongsApi.kt` (mevcut `getSongs(q=...)` parametresi kullanılır)
- `data/songs/SongDto.kt`
- `data/network/NetworkModule.kt`

**Yeni DI dosyası gerekmez** (SongsApi zaten `HomeModule` üzerinden inject ediliyor; Search ayrı bir `SearchModule` oluşturabilir ya da `HomeModule`'ü `SongsModule` olarak yeniden adlandırmak için takımla koordinasyon yapılır).

**Bağımlılık durumu:** Kütüphane, Favoriler ve Profil'den tamamen bağımsız.

---

### Kişi B — `feature/library`

**Ekran:** Kütüphane (Library)

**Kapsam:**
- `data/playlists/PlaylistDto.kt` — `PlaylistDto`, `PlaylistWithSongsDto`
- `data/playlists/PlaylistsApi.kt` — Retrofit interface (`GET /api/v1/playlists`, `GET /api/v1/playlists/{id}`)
- `data/library/LibraryRepository.kt` — interface
- `data/library/DefaultLibraryRepository.kt` — implementasyon (`@Inject constructor`)
- `di/LibraryModule.kt` — `@Binds` bağlaması
- `ui/library/LibraryContract.kt` — `LibraryUiState`, `LibraryIntent`, `LibraryEffect`
- `ui/library/LibraryViewModel.kt` — `@HiltViewModel`
- `ui/library/LibraryScreen.kt` — `LibraryRoute` + `LibraryScreen`
- `LyraNavHost.kt` içindeki `PlaceholderScreen("Kütüphane")` satırı → `LibraryRoute(...)` ile değiştirilir

**Bağımlılık durumu:** Arama, Favoriler ve Profil'den tamamen bağımsız. `NetworkModule.kt`'deki `Retrofit`'i kullanır ancak değiştirmez.

---

### Kişi C — `feature/favorites-profile`

**Ekranlar:** Favoriler (Favorites) + Profil (Profile)

Bu iki ekranın backend desteği yoktur; ikisi de tamamen yerel/sahte veriyle çalışacak ve birbirinden bağımsız MVI sözleşmeleri taşıdığından tek kişi rahatça üstlenebilir.

**Kapsam — Favoriler:**
- `data/favorites/FavoritesRepository.kt` — interface
- `data/favorites/FakeFavoritesRepository.kt` — in-memory implementasyon (backend hazır değil)
- `di/FavoritesModule.kt`
- `ui/favorites/FavoritesContract.kt`
- `ui/favorites/FavoritesViewModel.kt`
- `ui/favorites/FavoritesScreen.kt`

**Kapsam — Profil:**
- `data/profile/ProfileRepository.kt` — interface
- `data/profile/FakeProfileRepository.kt` — statik profil verisi
- `di/ProfileModule.kt`
- `ui/profile/ProfileContract.kt`
- `ui/profile/ProfileViewModel.kt`
- `ui/profile/ProfileScreen.kt`

- `LyraNavHost.kt` içindeki `PlaceholderScreen("Favoriler")` ve `PlaceholderScreen("Profil")` satırları → gerçek Route'larla değiştirilir.

**Bağımlılık durumu:** Arama ve Kütüphane'den tamamen bağımsız.

---

## LyraNavHost.kt Çakışma Çözümü

`LyraNavHost.kt` içinde 4 placeholder satırı 3 ayrı branch'te değiştirilecek. Merge sırasında çakışma kaçınılmaz. Önerilen yaklaşım:

**Sıralı merge stratejisi:**
1. İlk biten feature branch'i main'e merge edilir (örneğin `feature/search`).
2. Diğer branch'ler, merge öncesinde `git rebase main` ile güncellenir.
3. `LyraNavHost.kt` içinde yalnızca kendi ekranlarına ait placeholder satırını değiştiren kişi, rebase sırasında çakışmayı manuel olarak çözer.

**Alternatif (daha temiz):** Her kişi placeholder satırını değiştirmez; bunun yerine yalnızca `Route` composable'larını yazar. `LyraNavHost.kt`'yi tek bir kişi (Kişi A veya ders sorumlusu) en son günceller.

---

## Branch İsimlendirme

```
main
├── feature/search          → Kişi A
├── feature/library         → Kişi B
└── feature/favorites-profile → Kişi C
```

Her feature branch `main`'den açılır:

```bash
# Kişi A
git checkout main && git pull
git checkout -b feature/search

# Kişi B
git checkout main && git pull
git checkout -b feature/library

# Kişi C
git checkout main && git pull
git checkout -b feature/favorites-profile
```

---

## Dosya Dökümü (Özet)

| Dosya | Durum | Kişi |
|-------|-------|------|
| `ui/search/SearchContract.kt` | YENİ | A |
| `ui/search/SearchViewModel.kt` | YENİ | A |
| `ui/search/SearchScreen.kt` | YENİ | A |
| `data/playlists/PlaylistDto.kt` | YENİ | B |
| `data/playlists/PlaylistsApi.kt` | YENİ | B |
| `data/library/LibraryRepository.kt` | YENİ | B |
| `data/library/DefaultLibraryRepository.kt` | YENİ | B |
| `di/LibraryModule.kt` | YENİ | B |
| `ui/library/LibraryContract.kt` | YENİ | B |
| `ui/library/LibraryViewModel.kt` | YENİ | B |
| `ui/library/LibraryScreen.kt` | YENİ | B |
| `data/favorites/FavoritesRepository.kt` | YENİ | C |
| `data/favorites/FakeFavoritesRepository.kt` | YENİ | C |
| `di/FavoritesModule.kt` | YENİ | C |
| `ui/favorites/FavoritesContract.kt` | YENİ | C |
| `ui/favorites/FavoritesViewModel.kt` | YENİ | C |
| `ui/favorites/FavoritesScreen.kt` | YENİ | C |
| `data/profile/ProfileRepository.kt` | YENİ | C |
| `data/profile/FakeProfileRepository.kt` | YENİ | C |
| `di/ProfileModule.kt` | YENİ | C |
| `ui/profile/ProfileContract.kt` | YENİ | C |
| `ui/profile/ProfileViewModel.kt` | YENİ | C |
| `ui/profile/ProfileScreen.kt` | YENİ | C |
| `ui/navigation/LyraNavHost.kt` | DEGİŞECEK | Sıralı merge |
| `docs/decisions.md` | DEGİŞECEK | Her kişi kendi kararını ekler |

---

## Olası Hatalar ve Uyarılar

1. `LyraNavHost.kt` merge çakışması en riskli noktadır. Rebase alışkanlığı edinilmesi önerilir.
2. `SongsApi` hem Home hem Search tarafından kullanılır; `HomeModule` içinde `@Provides SongsApi` tanımlıdır. Kişi A bunu değiştirmeden inject edebilir.
3. Favoriler ve Profil için backend endpoint bulunmamaktadır. `agents.md §2.2` gereği bu veriler uydurulmamalı; `Fake` repository ile in-memory tutulmalıdır.
4. Her yeni ekran `decisions.md`'ye ilgili mimari kararını eklemek zorundadır (`agents.md §2.4`).
5. MVI referans implementasyonu Login ekranıdır; her kişi `ui/auth/login/` klasörünü inceleyerek başlamalıdır.
