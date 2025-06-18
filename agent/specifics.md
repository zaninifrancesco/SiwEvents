# 📌 Progetto: Partecipa

**Partecipa** è un'applicazione web sviluppata in **Spring Boot### 📅 Event

| Campo         | Tipo     | Descrizione                            |
|---------------|----------|----------------------------------------|
| id            | Long     | Identificatore evento                  |
| title         | String   | Titolo dell'evento                     |
| description   | String   | Descrizione                            |
| dateTime      | LocalDateTime | Data e ora dell'evento           |
| location      | String   | Luogo (indirizzo testuale)             |
| maxParticipants | Integer | Numero massimo partecipanti (null = illimitato) |
| createdBy     | User     | Utente creatore (USER o ADMIN)        |
| createdAt     | LocalDateTime | Data creazione evento            |
| participants  | Set<Participation> | Lista partecipazioni         |rendering lato server tramite Thymeleaf** e **stile tramite Bootstrap**, progettata per la gestione e la partecipazione a eventi.

---

## ✅ Obiettivi

- Consentire a chiunque di **visualizzare tutti gli eventi** (sempre pubblici)
- Permettere agli utenti autenticati tramite **OAuth2 (Google)** di:
  - Partecipare ad eventi esistenti
  - Gestire le proprie partecipazioni
  - Creare nuovi eventi
- Fornire agli **admin** pieno controllo su:
  - Gestione di **tutti** gli eventi (inclusi quelli creati dagli utenti)
  - Gestione utenti e partecipazioni
  - Dashboard amministrativa completa
- Visualizzazione e interazione tramite **template HTML (Thymeleaf)** con interfaccia responsive grazie a **Bootstrap**

---

## 🛠️ Tecnologie utilizzate

- Java 17+
- Spring Boot 3.x
- Spring Security + OAuth2 Client (Google)
- Spring Data JPA
- Thymeleaf (per i template HTML)
- Bootstrap (CSS framework responsive)
- PostgreSQL
- Maven


---

## 🧱 Architettura generale

### 🔐 Sicurezza

- Autenticazione tramite Google OAuth2
- Registrazione automatica dell’utente al primo login
- Controllo accessi con Spring Security
- Pagine accessibili in base al ruolo (es. `/admin/**`, `/eventi/**`, `/login`, ecc.)

---

## 🖼️ Frontend con Thymeleaf + Bootstrap

- Tutte le **pagine sono generate lato server** con **Thymeleaf**
- Layout responsive e gradevole grazie a **Bootstrap 5**
- Pagine previste:
  - Home pubblica (lista eventi)
  - Dettaglio evento
  - Login con Google
  - Dashboard utente
  - Dashboard admin per gestione eventi
  - Area admin per gestione utenti/eventi

---

## 📄 Entità principali

### 🧑‍💼 User

| Campo     | Tipo   | Descrizione                  |
|-----------|--------|------------------------------|
| id        | Long   | Identificatore univoco       |
| email     | String | Email unica dell’utente      |
| name      | String | Nome visualizzato            |

---

### 📅 Event

| Campo         | Tipo     | Descrizione                            |
|---------------|----------|----------------------------------------|
| id            | Long     | Identificatore evento                  |
| title         | String   | Titolo dell’evento                     |
| description   | String   | Descrizione                            |
| dateTime      | LocalDateTime | Data e ora dell’evento           |
| location      | String   | Luogo (indirizzo testuale)             |
| maxParticipants | Integer | Numero massimo partecipanti (null = illimitato) |
| createdBy     | User     | Utente creatore (USER o ADMIN)        |
| createdAt     | LocalDateTime | Data creazione evento            |
| participants  | Set<Participation> | Lista partecipazioni         |

---

### 🤝 Participation

| Campo    | Tipo | Descrizione                      |
|----------|------|----------------------------------|
| id       | Long | Identificatore                   |
| user     | User | Utente partecipante              |
| event    | Event| Evento a cui partecipa           |
| joinedAt | LocalDateTime | Data iscrizione evento  |

---

## 🔀 Rotte previste

### Pubbliche (`GUEST`)
- `/` – Home (lista eventi)
- `/eventi/{id}` – Dettaglio evento

### Autenticato (`USER`)
- `/dashboard` – Pannello utente
- `/eventi/{id}/partecipa` – Iscrizione evento
- `/eventi/nuovo` – Form per creare un nuovo evento
- `/eventi/miei` – Lista eventi creati dall'utente

### Amministratore (`ADMIN`)
- `/admin/dashboard` – Dashboard amministrativa
- `/admin/utenti` – Lista utenti
- `/admin/eventi` – Gestione eventi
- `/admin/eventi/nuovo` – Form nuovo evento

---

## 🔑 Login OAuth2 con Google

- L’utente accede tramite il provider Google
- Se è il primo accesso, viene creato nel database
- Viene assegnato automaticamente il ruolo `USER`
- L’assegnazione del ruolo `ADMIN` può avvenire manualmente

---

## 🗃️ Database PostgreSQL

### Schema relazionale dettagliato:

#### 📊 Tabelle principali

1. **users** (Utenti)
   - `id` (PK, Serial)
   - `email` (Unique, Not Null)
   - `name` (Not Null)
   - `google_id` (Unique) - ID Google OAuth2
   - `created_at` (Timestamp)

4. **events** (Eventi)
   - `id` (PK, Serial)
   - `title` (Not Null)
   - `description` (Text)
   - `date_time` (Timestamp, Not Null)
   - `location` (Text) - Indirizzo testuale
   - `max_participants` (Integer, Nullable)
   - `created_by` (FK → users.id, Not Null)
   - `created_at` (Timestamp, Default: NOW())

5. **participations** (Partecipazioni)
   - `id` (PK, Serial)
   - `user_id` (FK → users.id, Not Null)
   - `event_id` (FK → events.id, Not Null)
   - `joined_at` (Timestamp, Default: NOW())
   - Constraint unico (user_id, event_id)

### 🔗 Relazioni JPA

- **User → Event**: `@OneToMany` (createdBy) - Un utente può creare molti eventi
- **User ↔ Event**: `@ManyToMany` attraverso `Participation` - Un utente può partecipare a molti eventi
- **Event → Participation**: `@OneToMany` - Un evento ha molte partecipazioni

### 📍 Funzionalità Mappa (non implementabile ora ma forse in un futuro)

- **Tecnologia**: OpenStreetMap + Leaflet.js (gratuito)
- **Coordinate**: Latitudine e longitudine salvate nel database
- **Geocoding**: Integrazione con servizio gratuito (Nominatim) per convertire indirizzo in coordinate
- **Visualizzazione**: Mappa interattiva nella pagina dettaglio evento

### 📅 Integrazione Google Calendar

- **API**: Google Calendar API (gratuito fino a 1M richieste/mese)
- **Funzionalità**: 
  - Pulsante "Aggiungi a Google Calendar" su ogni evento
  - Creazione automatica evento calendar con dettagli
  - Link diretto per aggiunta rapida
- **Implementazione**: Generazione URL per Google Calendar o chiamata API

### 🔒 Vincoli e Indici

```sql
-- Indici per performance
CREATE INDEX idx_events_datetime ON events(date_time);
CREATE INDEX idx_events_location ON events(latitude, longitude);
CREATE INDEX idx_participations_user ON participations(user_id);
CREATE INDEX idx_participations_event ON participations(event_id);

-- Vincoli di integrità
ALTER TABLE events ADD CONSTRAINT chk_coordinates 
    CHECK ((latitude IS NULL AND longitude IS NULL) OR 
           (latitude IS NOT NULL AND longitude IS NOT NULL));

ALTER TABLE events ADD CONSTRAINT chk_max_participants 
    CHECK (max_participants IS NULL OR max_participants > 0);
```

---

## 📦 Build & Run

```bash
./mvnw clean spring-boot:run
```
