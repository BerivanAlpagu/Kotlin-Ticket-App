# Ticket API — Mobil Geliştirici Dokümanı

Bu doküman, Kotlin mobil uygulamanın tüketeceği REST API'yi anlatır. API Express.js + PostgreSQL üzerinde çalışır.

---

## 1. Temel Bilgiler

| Konu | Değer |
| --- | --- |
| Base URL (lokal) | `http://localhost:3000` |
| Format | JSON (`Content-Type: application/json`) |
| Auth şeması | `Authorization: Bearer <accessToken>` |
| Para birimi | `*Cents` alanları **kuruş** (integer). Örn. `priceCents: 20000` = ₺200,00 |
| Tarih formatı | ISO-8601 UTC (`2026-05-19T18:00:00.000Z`) |
| ID formatı | UUID v4 |
| Karakter seti | UTF-8 |
| Swagger UI | `GET /docs` |
| OpenAPI JSON | `GET /docs.json` |

### Seed kullanıcıları (geliştirme)
| Email | Şifre | Rol |
| --- | --- | --- |
| `admin@example.com` | `admin123` | ADMIN |
| `staff@example.com` | `staff123` | STAFF |
| `user@example.com` | `user123` | USER |

---

## 2. Roller ve Erişim

- **USER** — Bilet alan son kullanıcı. Etkinlikleri görür, sat alım yapar, biletini görüntüler.
- **STAFF** — Kapı görevlisi. Yalnızca kendisine atanmış etkinliklerin biletlerini tarayabilir.
- **ADMIN** — Etkinlik/bilet türü yönetimi, staff atama, satış raporu. STAFF yetkilerini de kapsar.

Her endpoint'in altında "Yetki" satırı vardır:
- *Public* — Token gerekmez.
- *Authenticated* — Geçerli access token gerekir.
- *USER / STAFF / ADMIN* — Listelenen rollerden biri olmalıdır.

---

## 3. Token Akışı

1. `POST /auth/login` → `{ accessToken, refreshToken, user }`
2. Access token 15 dakika geçerli, refresh token 7 gün geçerli.
3. `accessToken` her korumalı istekte `Authorization: Bearer ...` başlığında gönderilir.
4. 401 alırsan `POST /auth/refresh` ile yeni token çifti al; eski refresh otomatik iptal olur (rotation).
5. Cihaz "logout" deyince `POST /auth/logout` ile refresh token'ı sunucudan revoke et.

> Mobil tarafta access ve refresh token'ı **şifreli** saklayın (Android Keystore / EncryptedSharedPreferences).

---

## 4. Hata Formatı

Tüm hatalar şu zarfta döner:

```json
{
  "error": {
    "code": "validation_error",
    "message": "İstek doğrulaması başarısız",
    "details": { /* opsiyonel: zod field hataları */ }
  }
}
```

**Sık karşılaşılan kodlar**

| HTTP | code | Anlam |
| --- | --- | --- |
| 400 | `validation_error` | İstek gövdesi/parametresi geçersiz |
| 400 | `invalid_state` | Kaynak bu işlem için uygun durumda değil |
| 401 | `missing_token` / `invalid_token` | Token yok / geçersiz |
| 401 | `invalid_credentials` | Email veya şifre hatalı |
| 401 | `invalid_refresh` | Refresh token geçersiz/süresi dolmuş |
| 403 | `forbidden` | Rol yetersiz |
| 403 | `not_assigned` | Staff bu etkinliğe atanmamış |
| 403 | `not_purchase_owner` | Sat alım sahibi değilsin |
| 404 | `event_not_found` / `ticket_type_not_found` / `purchase_not_found` / `ticket_not_found` / `route_not_found` | Kayıt yok |
| 409 | `email_taken` | Email zaten kayıtlı |
| 409 | `capacity_exceeded` | Stok yetersiz |
| 409 | `already_paid` | Sat alım zaten ödenmiş |
| 409 | `already_used` | Bilet daha önce kullanılmış |
| 500 | `internal_error` | Sunucu hatası |

---

## 5. Endpoint Referansı

### 5.1 Auth

#### `POST /auth/register`
Yeni kullanıcı kaydı. Otomatik **USER** rolüyle açılır ve token çifti döner — kullanıcıyı ayrıca login etmenize gerek yok.

- **Yetki:** Public
- **Request**
  ```json
  { "email": "ali@example.com", "password": "guclu-sifre-12" }
  ```
    - `email` — geçerli email, otomatik lowercase'e çevrilir
    - `password` — 8–128 karakter
- **201 Response**
  ```json
  {
    "user": { "id": "uuid", "email": "ali@example.com", "role": "USER" },
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
  ```
- **Hatalar:** 400 `validation_error`, 409 `email_taken`

---

#### `POST /auth/login`
Mevcut kullanıcı girişi.

- **Yetki:** Public
- **Request**
  ```json
  { "email": "user@example.com", "password": "user123" }
  ```
- **200 Response:** Register ile aynı yapı.
- **Hatalar:** 400, 401 `invalid_credentials`

---

#### `POST /auth/refresh`
Eski refresh ile yeni access + refresh çifti üretir. Eski refresh anında geçersizleşir (token rotation).

- **Yetki:** Public (refresh token gövdede gönderilir)
- **Request**
  ```json
  { "refreshToken": "eyJ..." }
  ```
- **200 Response**
  ```json
  { "accessToken": "eyJ...", "refreshToken": "eyJ..." }
  ```
- **Hatalar:** 401 `invalid_refresh`

---

#### `POST /auth/logout`
Refresh token'ı sunucudan iptal eder. Cihazdaki tokenları da silmeyi unutmayın.

- **Yetki:** Public (token gövdede)
- **Request:** `{ "refreshToken": "..." }`
- **204 Response:** Boş

---

### 5.2 Etkinlikler (genel)

#### `GET /events`
Etkinlik listesi. Her bilet türü için **kalan** kapasiteyi (`remaining = capacity - soldCount`) içerir. Mobil bunu doğrudan gösterebilir.

- **Yetki:** Public
- **Query parametreleri**
    - `upcoming=true` — yalnızca başlangıcı gelecekte olan etkinlikler
- **200 Response**
  ```json
  [
    {
      "id": "uuid",
      "name": "Demo Konseri",
      "description": "...",
      "venue": "Ana Sahne, İstanbul",
      "startsAt": "2026-05-19T18:00:00.000Z",
      "endsAt": "2026-05-19T22:00:00.000Z",
      "createdAt": "2026-05-05T12:00:00.000Z",
      "ticketTypes": [
        { "id": "uuid", "name": "VIP", "priceCents": 50000, "capacity": 50, "soldCount": 3, "remaining": 47 },
        { "id": "uuid", "name": "Standart", "priceCents": 20000, "capacity": 200, "soldCount": 12, "remaining": 188 }
      ]
    }
  ]
  ```

---

#### `GET /events/{id}`
Tek etkinliğin tüm detayı (yukarıdaki yapıdan tek obje).

- **Yetki:** Public
- **Hatalar:** 404 `event_not_found`

---

### 5.3 Sat Alımlar

#### `POST /purchases`
Yeni sat alım oluşturur. **Stok burada düşmez** — yalnızca `PENDING` durumunda kayıt açılır ve fiyatlar bu anda snapshot'lanır. Stok düşmesi `pay` çağrısında olur.

- **Yetki:** USER veya ADMIN
- **Request**
  ```json
  {
    "items": [
      { "ticketTypeId": "uuid", "quantity": 2 },
      { "ticketTypeId": "uuid", "quantity": 1 }
    ]
  }
  ```
    - `quantity` — 1–20 arası
    - `items` — 1–20 satır arası
- **201 Response**
  ```json
  {
    "id": "uuid",
    "userId": "uuid",
    "status": "PENDING",
    "totalCents": 120000,
    "createdAt": "...",
    "paidAt": null,
    "items": [
      { "id": "uuid", "ticketTypeId": "uuid", "quantity": 2, "unitPriceCents": 50000 },
      { "id": "uuid", "ticketTypeId": "uuid", "quantity": 1, "unitPriceCents": 20000 }
    ]
  }
  ```
- **Hatalar:** 400, 404 `ticket_type_not_found`, 409 `capacity_exceeded`

---

#### `POST /purchases/{id}/pay`
**Mock ödeme.** Bir transaction içinde:
1. Her bilet türünde `soldCount + qty <= capacity` atomik kontrol → ihlal varsa `409 capacity_exceeded`.
2. `soldCount` artırılır.
3. Her quantity için bir **Ticket** kaydı (`status: VALID`, `qrCode = uuid v4`) oluşturulur.
4. Purchase `PAID` olur, `paidAt` set edilir.

> Gerçek bir PSP entegrasyonu eklendiğinde mobilin gönderdiği gövde değişebilir; şu an boş gövde yeterli.

- **Yetki:** USER veya ADMIN (yalnızca sat alım sahibi)
- **Request:** Boş gövde
- **200 Response**
  ```json
  {
    "id": "uuid",
    "status": "PAID",
    "paidAt": "...",
    "totalCents": 120000,
    "items": [...],
    "tickets": [
      { "id": "uuid", "qrCode": "uuid-v4", "status": "VALID", "ticketTypeId": "uuid" }
    ]
  }
  ```
- **Hatalar:** 403 `not_purchase_owner`, 404 `purchase_not_found`, 409 `already_paid`, 409 `capacity_exceeded`

---

#### `GET /purchases/{id}`
Sat alımın detayı + üretilmiş biletler.

- **Yetki:** USER veya ADMIN (sahip)
- **Hatalar:** 404 `purchase_not_found`

---

### 5.4 Bana Ait (Me)

#### `GET /me/purchases`
Kullanıcının tüm sat alımları (PENDING + PAID).

- **Yetki:** Authenticated
- **200 Response:** `Purchase[]` (tickets dahil değil, hızlı liste)

---

#### `GET /me/tickets`
Kullanıcının tüm biletleri. Her bilet, etkinlik ve bilet türü bilgisini içerir — mobil "Biletlerim" ekranı için hazır veridir.

- **Yetki:** Authenticated
- **200 Response**
  ```json
  [
    {
      "id": "uuid",
      "qrCode": "uuid-v4",
      "status": "VALID",
      "usedAt": null,
      "checkedInBy": null,
      "ticketType": {
        "id": "uuid",
        "name": "VIP",
        "priceCents": 50000,
        "event": {
          "id": "uuid",
          "name": "Demo Konseri",
          "venue": "Ana Sahne, İstanbul",
          "startsAt": "2026-05-19T18:00:00.000Z"
        }
      }
    }
  ]
  ```

---

#### `GET /me/tickets/{id}`
Tek biletin tam detayı. **Mobilin QR ekranı için ana endpoint.** `qrCode` alanını QR koduna çevirip gösterin.

- **Yetki:** Authenticated (sahip)
- **Hatalar:** 404 `ticket_not_found`

> QR'da yalnızca `qrCode` UUID'si bulunur (örn. `b3a1c4...`). Ekstra payload eklemeyin — staff tarafı bu değeri tek başına bekler.

---

### 5.5 Check-in (Staff)

#### `GET /checkin/events`
Giriş yapan staff'ın atandığı etkinliklerin listesi. Mobil "Hangi etkinliği tarıyorum?" ekranı için.

- **Yetki:** STAFF veya ADMIN
- **200 Response:** `Event[]`

---

#### `POST /checkin/scan`
QR taramasının ardından **tek bilet** kontrolü ve check-in.

- **Yetki:** STAFF veya ADMIN
- **Request**
  ```json
  { "qrCode": "b3a1c4d6-..." }
  ```
- **200 Response**
  ```json
  {
    "ticketId": "uuid",
    "ticketType": "VIP",
    "event": {
      "id": "uuid",
      "name": "Demo Konseri",
      "venue": "Ana Sahne, İstanbul",
      "startsAt": "2026-05-19T18:00:00.000Z"
    },
    "checkedInAt": "2026-05-19T18:05:30.123Z"
  }
  ```
- **Hatalar:**
    - `404 ticket_not_found` → bilet yok / sahte QR
    - `403 not_assigned` → staff bu etkinliğe atanmamış
    - `409 already_used` → bilet daha önce taranmış (mobil "Bu bilet kullanılmış" toast'ı göstermeli)

> Endpoint **idempotent değildir.** Aynı QR için ikinci çağrı 409 döner — bu beklenen ve doğru davranıştır.

---

### 5.6 Admin

> Tüm `/admin/*` endpoint'leri ADMIN rolü ister; mobil "User" tarafında genelde kullanılmaz, ama panel/test amaçlı belirtilmiştir.

| Method | Path | Açıklama |
| --- | --- | --- |
| `POST` | `/admin/events` | Yeni etkinlik |
| `PATCH` | `/admin/events/{id}` | Etkinlik güncelle |
| `DELETE` | `/admin/events/{id}` | Etkinlik sil (cascade ile bilet türleri/biletler düşer) |
| `GET` | `/admin/events/{id}/sales` | Bilet türü bazında satılan/kalan/gelir özeti |
| `POST` | `/admin/events/{id}/ticket-types` | Bilet türü ekle |
| `PATCH` | `/admin/ticket-types/{id}` | Bilet türü güncelle |
| `POST` | `/admin/events/{eventId}/staff/{userId}` | STAFF rolündeki kullanıcıyı etkinliğe ata |
| `DELETE` | `/admin/events/{eventId}/staff/{userId}` | Atamayı kaldır |

**Ek not:** `POST /admin/events/{eventId}/staff/{userId}` çağrısında hedef kullanıcının rolü **STAFF** olmalı; aksi halde `400 not_staff`.

---

## 6. Diğer

### `GET /health`
Basit sağlık kontrolü. `200 { "ok": true }`. Mobilin gerek yoktur ama monitoring araçları için mevcuttur.

### `GET /docs`
Swagger UI — interaktif API explorer. Token'ı sağ üstteki "Authorize" butonuyla girip bütün endpointleri tarayıcıdan deneyebilirsiniz.

---

## 7. Mobil Tipik Akışlar

### Bilet Alma (USER)
1. `POST /auth/login`
2. `GET /events?upcoming=true`
3. Kullanıcı bilet türünü ve adedi seçer
4. `POST /purchases` → `purchase.id`
5. Ödeme ekranı (mock için doğrudan): `POST /purchases/:id/pay`
6. Yanıttaki `tickets[]` ile başarı ekranı + Biletlerim'e yönlendir
7. `GET /me/tickets` → liste
8. `GET /me/tickets/:id` → QR ekranı (`qrCode` alanını QR'a çevir)

### Kapıda Check-in (STAFF)
1. `POST /auth/login` (staff hesabı)
2. `GET /checkin/events` → atandığı etkinlikleri seç
3. Kamera ile QR oku → string al
4. `POST /checkin/scan` { "qrCode": "..." } → success / 409 / 403 / 404 mesajına göre UI güncelle

---

## 8. Bilinen Sınırlamalar (MVP)

- **Gerçek ödeme yok** — `pay` her zaman başarılı. Iyzico/Stripe entegrasyonu sonra.
- **İade / iptal yok.**
- **Email doğrulama / şifre sıfırlama yok.**
- **Push bildirim yok.**
- **Rate limit yok** — istemcide kötü niyetli kullanım varsayılmıyor.
- **Tek dil** — sunucu hata mesajları Türkçe; yerelleştirme istemcide yapılır.