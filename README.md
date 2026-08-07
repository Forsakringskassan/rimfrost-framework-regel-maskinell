# rimfrost-framework-regel-maskinell

Återanvändbart ramverksbibliotek för implementering av maskinella regler.
Ramverket hanterar den gemensamma infrastrukturen för att ta emot regelförfrågningar, hämta
handläggningsdata, anropa regellogik och publicera regelresultat – och delegerar den
regelspecifika affärslogiken till konkreta regelimplementationer. Syftet är att säkerställa ett
enhetligt och tillförlitligt mönster för alla maskinella regler i systemet.

Baseras på [rimfrost-framework-regel](https://github.com/Forsakringskassan/rimfrost-framework-regel)
som innehåller komponenter gemensamma för alla typer av regler.

## Aktörer

| Aktör | Roll |
|---|---|
| Kundbehovsflödet | Initierar regelkörningen via Kafka och tar emot regelsvaret |
| Regelimplementationer | Bygger vidare på detta ramverk och implementerar regelspecifik logik |
| Handläggningstjänsten | Tillhandahåller och tar emot handläggningsdata |

## Struktur

```
se.fk.rimfrost.framework.regel.maskinell/
├── logic/
│   ├── RegelMaskinellServiceInterface   # Implementeras av konkreta regler
│   ├── RegelMaskinellRequestHandler     # Orkestrerare: läser, kör, skriver, svarar
│   ├── RegelMaskinellMapper             # Konverterar regelresultat till HandlaggningUpdate
│   └── dto/                             # In- och utdatatyper för processRegel()
└── helpers/retry/                       # Generisk återförsöksmekanism med konfigurerbart intervall

src/test/java/.../
├── AbstractRegelMaskinellTest           # Basklass med WireMock + in-memory Kafka
├── AbstractRegelMaskinellResponseTest   # Testar Kafka-svar
├── AbstractRegelMaskinellHandlaggningTest # Testar handläggningsuppdatering
├── AbstractRegelMaskinellSequenceTest   # End-to-end-flöde
└── helpers/WireMockRegelMaskinell       # HTTP-mocking mot Handläggning API
```

---

## Mall

[https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell](https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell)
kan användas som mall för att skapa en ny regel baserad på detta ramverk.

---

## Implementera en maskinell regel

En regelimplementation behöver tillhandahålla två saker:

### 1. Serviceklass

Implementera `RegelMaskinellServiceInterface` och annotera klassen med `@ApplicationScoped`.

```java
@ApplicationScoped
public class MinRegelService implements RegelMaskinellServiceInterface {

    @Override
    public RegelMaskinellResult processRegel(RegelMaskinellRequest request) {
        // Implementera regelspecifik logik här.
        // Returnera RegelMaskinellSuccessResult med HandlaggningUpdate och utfall,
        // eller RegelMaskinellErrorResult med felinformation.
    }
}
```

`processRegel()` anropas av ramverket med handläggning, uppgift och processinstans-ID.
Vid ett lyckat resultat uppdaterar ramverket handläggningen och publicerar regelsvaret.
Kastas en exception fångas den av ramverket och ett felsvar med felkod `RIMFROST_OTHER` publiceras.

### 2. Konfiguration

```properties
# Kafka
mp.messaging.incoming.regel-requests.topic=<topic>
mp.messaging.outgoing.regel-responses.topic=<topic>

# Handläggningstjänsten
handlaggning.api.base-url=https://<handlaggning-host>

# Sökväg till YAML-fil med uppgiftsspecifikation
application.config.path=src/main/resources/config.yaml

# Återförsöksintervall i sekunder (valfritt — standardvärde används annars)
rimfrost.framework.regel.maskinell.retry.intervals=15,30,60,120,240,480,960,1920,3840,7680,15360,30720
```

YAML-filen med uppgiftsspecifikation anger regelns namn, beskrivning, verksamhetslogik och roll:

```yaml
uppgift:
  version: 1
  path: /regel/<regel-name>
  aktivitet: "<Uppgift aktivitet>"

specifikation:
  id: <uuid>
  version: 1
  namn: "<Uppgift specifikation namn>"
  uppgiftbeskrivning: "<Uppgift beskrivning>"
  verksamhetslogik: <värde>
  roll: ANSVARIG_HANDLAGGARE
  applikationsId: <id>
  applikationsversion: <version>
```

---

## Kafka

Ramverket hanterar följande Kafka-kanaler:

| Kanal | Riktning | Trigger |
|---|---|---|
| `regel-requests` | Inkommande | Regelförfrågan från kundbehovsflödet |
| `regel-responses` | Utgående | Regelbehandling klar (lyckat eller misslyckat) |

Ramverket stödjer `replyTo`-fältet i inkommande meddelanden för dynamisk dirigering av svar.
Meddelandescheman definieras i **rimfrost-framework-regel-asyncapi**.

---

## Test-JAR

Ramverket levererar en test-JAR med abstrakta basklasser och hjälpklasser för
regelimplementationernas tester.

### Abstrakta basklasser

| Klass | Täcker |
|---|---|
| `AbstractRegelMaskinellTest` | Grundkonfiguration med in-memory Kafka och WireMock |
| `AbstractRegelMaskinellResponseTest` | Verifiering av regelresultat |
| `AbstractRegelMaskinellHandlaggningTest` | Verifiering av handläggningsuppdateringar |
| `AbstractRegelMaskinellSequenceTest` | End-to-end-flöde |

```java
@QuarkusTest
@QuarkusTestResource(WireMockRegelMaskinell.class)
public class MinRegelHandlaggningTest extends AbstractRegelMaskinellHandlaggningTest {
}
```

### Hjälpklasser

| Klass | Användning |
|---|---|
| `RegelMaskinellTestData` | Metoder för att skapa testdata |
| `WireMockRegelMaskinell` | WireMock-setup för Handläggningstjänsten |