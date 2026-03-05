# rimfrost-framework-regel-maskinell

Innehåller ramverkskomponenter för att implementera förmåners maskinella regler.<br>
Baseras på [rimfrost-framework-regel](https://github.com/Forsakringskassan/rimfrost-framework-regel) som innehåller komponenter gemensamma för alla typer av regler. <br>

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

JUnit-baserat test i _RegelMaskinellTest_ verifierar ramverkskomponenten genom att mocka _processRegel_ som regler implementerar.<br>
Regler kan även "extenda" _RegelMaskinellTest_ för att lägga till regel-specifika testfall.<br>
Handläggningsdata i testerna hanteras genom wiremock och mallar i _test/resources/mappings_.

## Mall

[https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell](https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell) <br>
kan användas som mall för att skapa ny regel baserat på detta ramverk.