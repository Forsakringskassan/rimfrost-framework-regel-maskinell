# rimfrost-framework-regel-maskinell

Ramverkskomponent med definitioner gemensamma för alla maskinella regler.
Innehåller både framework-logik och hjälpklasser vid test av regler.

```text
root
├── src/main
│   └── (framework implementation)
├── src/test
│   └── (tester av ramverket)
├── src/test/base
│   └── (abstrakta testklasser)
├── src/test/helpers
    └── (helpers för testklasser)
```

Baseras på [rimfrost-framework-regel](https://github.com/Forsakringskassan/rimfrost-framework-regel) som innehåller komponenter gemensamma för alla typer av regler. <br>

## Mall

[https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell](https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell) <br>
kan användas som mall för att skapa ny regel baserat på detta ramverk.

# src/main

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

## src/test

JUnit-baserade tester i _RegelMaskinell*Test_ verifierar ramverkskomponenten genom att mocka _processRegel_ som regler implementerar.<br>
Handläggningsdata i testerna hanteras genom wiremock och mallar i _test/resources/mappings_.<br>

Innehåller även abstrakta testklasser som är byggda för att kunna användas och köras i den färdiga regeln.<br>
Ramverkstestklasserna _Regel*Test.java_ extendar de abstrakta testklasserna i _src/test/base_ för att kunna köras även för verifiering av ramverket.

## src/test/base

### AbstractRegelMaskinellTest

Innehåller testkomponenter som är gemensamma för alla maskinella regler.
Ärver komponenter från rimfrost-framework-regel.<br>

### RegelMaskinellTestData

Utility-klass som skapar testdata.

## src/test/helpers

### WireMockRegelMaskinell

Utility-klass för hantering av maskinella reglers Wiremock-setup.

