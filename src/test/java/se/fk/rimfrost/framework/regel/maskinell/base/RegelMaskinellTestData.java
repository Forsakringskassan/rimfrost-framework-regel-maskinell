package se.fk.rimfrost.framework.regel.maskinell.base;

import se.fk.rimfrost.framework.handlaggning.model.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegelMaskinellTestData
{

   public static ArrayList<Underlag> createUnderlagListForTest()
   {
      return new ArrayList<>(List.of(
            ImmutableUnderlag.builder()
                  .typ("TEST_UNDERLAG_TYP_1")
                  .version(1)
                  .data("TEST_UNDERLAG_DATA_1")
                  .build(),
            ImmutableUnderlag.builder()
                  .typ("TEST_UNDERLAG_TYP_2")
                  .version(2)
                  .data("TEST_UNDERLAG_DATA_2")
                  .build()));
   }

   public static ArrayList<ProduceratResultat> createProduceradeResultatForTest()
   {
      return new ArrayList<>(List.of(
            ImmutableProduceratResultat.builder()
                  .id(UUID.fromString("66666666-6666-6666-6666-666666661234"))
                  .typ("TEST_PRODUCERAT_RESULTAT_TYP_1")
                  .version(1)
                  .resultatFrom(OffsetDateTime.parse("2025-01-01T00:00:00Z"))
                  .resultatTom(OffsetDateTime.parse("2025-01-02T00:00:00Z"))
                  .yrkandeStatus("UNDER_UTREDNING")
                  .data("TEST_PRODUCERAT_RESULTAT_TYP_1")
                  .build(),
            ImmutableProduceratResultat.builder()
                  .id(UUID.fromString("d89ca33f-eeeb-48fa-850f-7b9d9b07cc87"))
                  .typ("TEST_PRODUCERAT_RESULTAT_TYP_2")
                  .version(1)
                  .resultatFrom(OffsetDateTime.parse("2025-02-01T00:00:00Z"))
                  .resultatTom(OffsetDateTime.parse("2025-02-02T00:00:00Z"))
                  .yrkandeStatus("YRKAT")
                  .data("TEST_PRODUCERAT_RESULTAT_TYP_2")
                  .build()));
   }
}
