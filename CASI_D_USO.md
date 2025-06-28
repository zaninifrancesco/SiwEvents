# Casi d'Uso - Siw Events

## 1. Registrazione e Autenticazione Utente

**Attore Principale:** Visitatore non registrato

**Precondizioni:**
- L'utente ha accesso a internet
- L'utente ha un account Google valido

**Flusso Principale:**
1. L'utente visita la homepage di Siw Events
2. L'utente clicca su "Registrati / Accedi" o "Login con Google"
3. Il sistema reindirizza l'utente al servizio di autenticazione OAuth2 di Google
4. L'utente inserisce le credenziali Google
5. Google conferma l'identità e reindirizza l'utente all'applicazione
6. Il sistema crea automaticamente un account utente con i dati forniti da Google
7. L'utente viene reindirizzato alla dashboard personale

**Postcondizioni:**
- L'utente è autenticato nel sistema
- L'utente ha accesso alle funzionalità riservate agli utenti registrati

---

## 2. Creazione di un Nuovo Evento

**Attore Principale:** Utente autenticato

**Precondizioni:**
- L'utente è autenticato nel sistema
- L'utente ha accesso alla funzionalità di creazione eventi

**Flusso Principale:**
1. L'utente accede alla dashboard o homepage
2. L'utente clicca su "Crea Evento"
3. Il sistema mostra il form di creazione evento
4. L'utente compila i campi obbligatori:
   - Titolo dell'evento
   - Descrizione
   - Data e ora
   - Luogo
   - Numero massimo di partecipanti (opzionale)
5. L'utente carica un'immagine per l'evento (opzionale)
6. L'utente clicca su "Crea Evento"
7. Il sistema valida i dati inseriti
8. Il sistema salva l'evento nel database
9. Il sistema mostra un messaggio di conferma
10. L'utente viene reindirizzato alla pagina dettagli dell'evento creato

**Flussi Alternativi:**
- Se la validazione fallisce, il sistema mostra gli errori e mantiene i dati inseriti
- Se il caricamento dell'immagine fallisce, l'evento viene comunque creato senza immagine

**Postcondizioni:**
- Un nuovo evento è stato creato nel sistema
- L'evento è visibile nella lista degli eventi pubblici
- L'utente creatore è automaticamente associato all'evento

---

## 3. Partecipazione a un Evento

**Attore Principale:** Utente autenticato

**Precondizioni:**
- L'utente è autenticato nel sistema
- Esiste almeno un evento disponibile
- L'evento non ha raggiunto il numero massimo di partecipanti
- L'utente non è già iscritto all'evento

**Flusso Principale:**
1. L'utente naviga nella lista degli eventi (homepage o dashboard)
2. L'utente clicca su "Dettagli" di un evento di interesse
3. Il sistema mostra la pagina dettagli dell'evento
4. L'utente clicca su "Partecipa" o "Iscriviti"
5. Il sistema registra la partecipazione dell'utente
6. Il sistema aggiorna il contatore dei partecipanti
7. Il sistema mostra un messaggio di conferma
8. La pagina viene aggiornata mostrando che l'utente è iscritto

**Flussi Alternativi:**
- Se l'evento è al completo, il sistema mostra "Posti Esauriti" e non permette l'iscrizione
- Se l'utente è già iscritto, il sistema mostra "Già iscritto" o "Annulla partecipazione"

**Postcondizioni:**
- L'utente è registrato come partecipante all'evento
- Il contatore dei partecipanti è aggiornato
- L'evento appare nella lista "I miei eventi" dell'utente

---

## 4. Visualizzazione e Gestione Eventi Personali

**Attore Principale:** Utente autenticato

**Precondizioni:**
- L'utente è autenticato nel sistema
- L'utente ha partecipato o creato almeno un evento

**Flusso Principale:**
1. L'utente accede alla dashboard personale
2. Il sistema mostra la sezione "I miei eventi"
3. L'utente può visualizzare:
   - Eventi creati (con opzioni di modifica/eliminazione)
   - Eventi a cui partecipa
   - Eventi passati
4. L'utente può filtrare gli eventi per:
   - Tipologia (creati/partecipazione)
   - Data (futuri/passati)
   - Stato (attivi/cancellati)
5. Per gli eventi creati, l'utente può:
   - Modificare i dettagli
   - Visualizzare la lista dei partecipanti
   - Cancellare l'evento
6. Per gli eventi di partecipazione, l'utente può:
   - Visualizzare i dettagli
   - Annullare la partecipazione

**Postcondizioni:**
- L'utente ha una panoramica completa dei suoi eventi
- L'utente può gestire efficacemente i propri eventi

---

## 5. Amministrazione del Sistema

**Attore Principale:** Amministratore

**Precondizioni:**
- L'utente è autenticato con ruolo di amministratore
- L'utente ha accesso alla dashboard admin

**Flusso Principale:**
1. L'amministratore accede alla dashboard admin
2. Il sistema mostra le opzioni di amministrazione:
   - Gestione utenti
   - Gestione eventi
   - Statistiche del sistema
3. **Gestione Utenti:**
   - Visualizzazione lista utenti
   - Ricerca utenti per email/nome
   - Modifica ruoli utente
   - Sospensione/riattivazione account
4. **Gestione Eventi:**
   - Visualizzazione tutti gli eventi
   - Ricerca eventi per titolo/data/creatore
   - Modifica eventi
   - Cancellazione eventi inappropriati
   - Messa in evidenza di eventi
5. **Statistiche:**
   - Numero totale utenti
   - Numero totale eventi
   - Eventi più popolari
   - Statistiche di partecipazione

**Postcondizioni:**
- Il sistema è monitorato e gestito correttamente
- Gli utenti e gli eventi sono moderati secondo le policy

---

## 6. Caricamento e Gestione Foto Eventi

**Attore Principale:** Utente autenticato (creatore dell'evento)

**Precondizioni:**
- L'utente è autenticato nel sistema
- L'utente è il creatore dell'evento
- L'evento esiste nel sistema

**Flusso Principale:**
1. L'utente accede alla pagina dettagli del proprio evento
2. L'utente clicca su "Gestisci Foto" o "Carica Foto"
3. Il sistema mostra la galleria foto dell'evento
4. L'utente può:
   - Caricare nuove foto
   - Visualizzare foto esistenti
   - Eliminare foto
   - Impostare una foto come immagine principale
5. **Caricamento Foto:**
   - L'utente seleziona uno o più file immagine
   - Il sistema valida formato e dimensioni
   - Il sistema genera nomi file univoci
   - Il sistema salva le immagini nella directory eventi
   - Il sistema aggiorna il database con i riferimenti
6. **Gestione Foto:**
   - L'utente può eliminare foto esistenti
   - L'utente può cambiare l'immagine principale dell'evento
   - L'utente può riordinare le foto nella galleria

**Flussi Alternativi:**
- Se il file non è un'immagine valida, il sistema mostra un errore
- Se il file è troppo grande, il sistema può ridimensionarlo automaticamente
- Se il caricamento fallisce, il sistema mantiene le foto esistenti

**Postcondizioni:**
- Le foto sono caricate e associate all'evento
- L'evento ha una galleria foto aggiornata
- L'immagine principale dell'evento può essere visualizzata nelle liste

