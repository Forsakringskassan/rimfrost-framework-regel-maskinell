# rimfrost-framework-regel-maskinell

Innehåller ramverkskomponenter för att implementera förmåners maskinella regler.<br>

Ramverkskomponent med definitioner gemensamma för alla maskinella regler.
Innehåller både framework-logik och hjälpklasser vid test av regler.

- **`core`** – Framework-logik
- **`test-base`** – Återanvändbara testkomponenter för implementation av maskinella reglers tester

```text
root
├── core
│   └── (framework implementation)
├── test-base
│   └── (test-klasser)
└── pom.xml (parent)
```

Baseras på [rimfrost-framework-regel](https://github.com/Forsakringskassan/rimfrost-framework-regel) som innehåller komponenter gemensamma för alla typer av regler. <br>

## Mall

[https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell](https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell) <br>
kan användas som mall för att skapa ny regel baserat på detta ramverk.

# Core

Ramverket hanterar:

- Konsumption av _regel request_ kafka-meddelanden
- Produktion av _regel response_ kafka-meddelanden
- Initial uppläsning av handläggningsinfo
- Avslutande uppdatering av ersättningar och underlag till handläggningsinfo

Ramverket definierar ett interface _RegelMaskinellServiceInterface_ som implementeras av alla maskinella regler.<br>
Reglerna implementerar

```
RegelMaskinellResult processRegel(RegelMaskinellRequest regelMaskinellRequest)
```

vilket är den regel-specifika logik som producerar ett regel-resultat baserat på indata som hämtas från handläggningsinformation.

## DTOs

- RegelMaskinellRequest
- RegelMaskinellResult

Definierar DTO's för indata- resp. utdata för regelns processande.

Notera att ramverket även innehållar mapper för t.ex. konvertering från _RegelMaskinellResult_ till _RegelResult_
(det resultatformat som är gemensamt för alla typer av regler, både manuella och maskinella)

## Tester

JUnit-baserade tester i _RegelMaskinell*Test_ verifierar ramverkskomponenten genom att mocka _processRegel_ som regler implementerar.<br>
Handläggningsdata i testerna hanteras genom wiremock och mallar i _test/resources/mappings_.

# test-base

## RegelMaskinellTestBase

Innehåller testkomponenter som är gemensamma för alla maskinella regler.
Ärver komponenter från rimfrost-framework-regel.<br>

## RegelMaskinellTestData

Utility-klass som skapar testdata.

## WireMockRegelMaskinell

Utility-klass för hantering av maskinell reglers Wiremock-setup.

