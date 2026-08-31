# Krav — rimfrost-framework-regel-maskinell

## 1. Funktionella krav

### FRMASK-FR-01 — Mottagning av regelförfrågan

- **FRMASK-FR-01.1** Ramverket ska konsumera regelförfrågningar från en konfigurerad Kafka-topic.

### FRMASK-FR-02 — Hämtning av handläggningsdata

- **FRMASK-FR-02.1** Ramverket ska hämta handläggningsdata från Handläggning API baserat på handläggnings-ID i förfrågan.
- **FRMASK-FR-02.2** Om hämtningen misslyckas ska ramverket utföra konfigurerbara retries med ökande fördröjningar.
- **FRMASK-FR-02.3** Om alla retries är uttömda ska ett felsvar med felkod `RIMFROST_HANDLAGGNING_READ_FAILURE` publiceras.

### FRMASK-FR-03 — Skapande av uppgifter

- **FRMASK-FR-03.1** Ramverket ska skapa ett uppgiftsobjekt utifrån uppgiftsspecifikation konfigurerad i en extern YAML-fil.
- **FRMASK-FR-03.2** Uppgiftsstatus ska alltid sättas till `null` för maskinella regler (ingen OUL-uppföljning).

### FRMASK-FR-04 — Anrop av regellogik

- **FRMASK-FR-04.1** Ramverket ska anropa den konkreta regelimplementationens `processRegel()`-metod med handläggning, uppgift och processinstans-ID.
- **FRMASK-FR-04.2** Om `processRegel()` kastar ett exception ska ramverket fånga detta och returnera ett felsvar med felkod `RIMFROST_OTHER`.

### FRMASK-FR-05 — Uppdatering av handläggningsdata

- **FRMASK-FR-05.1** Vid ett lyckat regelresultat ska ramverket uppdatera handläggningsdata via Handläggning API.
- **FRMASK-FR-05.2** Uppdateringen ska utföras med konfigurerbara retries på samma sätt som vid hämtning.
- **FRMASK-FR-05.3** Om alla retries är uttömda ska ett felsvar med felkod `RIMFROST_HANDLAGGNING_WRITE_FAILURE` publiceras.

### FRMASK-FR-06 — Publicering av regelresultat

- **FRMASK-FR-06.1** Ramverket ska publicera ett regelresultat till en konfigurerad Kafka-topic efter varje behandlad förfrågan.
- **FRMASK-FR-06.2** Resultatet ska innehålla handläggnings-ID och utfall (`JA`, `NEJ`, `UTREDNING` eller `ERROR`).
- **FRMASK-FR-06.3** Vid felsvar ska resultatet även innehålla felkod och felmeddelande.
- **FRMASK-FR-06.4** Ramverket ska stödja `replyTo`-fältet i Kafka-meddelanden för dirigering av svar.

### FRMASK-FR-07 — Regelresultatgränssnitt

- **FRMASK-FR-07.1** Ramverket ska tillhandahålla ett gränssnitt (`RegelMaskinellServiceInterface`) som konkreta regelimplementationer implementerar.
- **FRMASK-FR-07.2** Implementationen ska returnera antingen ett lyckat resultat med uppdaterad handläggning och utfall, eller ett felresultat med felinformation.

### FRMASK-FR-08 — Kompletteringskontroll

- **FRMASK-FR-08.1** När `checkKomplettering()` returnerar en icke-tom lista ska ramverket delegera till `KompletteringOulHandler.initiate()` för att skapa kompletteringsuppgiften. Inget regelsvar ska skickas.
- **FRMASK-FR-08.2** Om `initiate()` kastar `OulException` ska ramverket skicka ett felsvar med felkod `RIMFROST_OTHER`.

## 2. Statusmodell

Maskinella regler producerar ett utfall per regelkörning. Det finns ingen intern statuslivscykel i ramverket; utfallet är alltid slutgiltigt vid publicering.

| Utfall      | Beskrivning                        |
|-------------|------------------------------------|
| `JA`        | Regeln är uppfylld                 |
| `NEJ`       | Regeln är inte uppfylld            |
| `UTREDNING` | Resultatet kräver vidare utredning |
| `ERROR`     | Ett fel uppstod under behandling   |

## 3. Icke-funktionella krav

### FRMASK-NFR-01 — Tillförlitlighet

- **FRMASK-NFR-01.1** Ramverket ska hantera tillfälliga fel mot Handläggning API genom konfigurerbara retries med exponentiellt ökande väntetid.
- **FRMASK-NFR-01.2** Retry-intervall ska vara konfigurerbara per driftsättning.

### FRMASK-NFR-02 — Observerbarhet

- **FRMASK-NFR-02.1** Ramverket ska logga händelser vid fel och vid lyckad regelbehandling.

### FRMASK-NFR-03 — Konfigurerbarhet

- **FRMASK-NFR-03.1** Retry-intervall, Handläggning API-URL och Kafka-topics ska vara konfigurerbara utan kodändringar.
- **FRMASK-NFR-03.2** Uppgiftsspecifikation (namn, beskrivning, verksamhetslogik m.m.) ska kunna konfigureras via en extern YAML-fil.
