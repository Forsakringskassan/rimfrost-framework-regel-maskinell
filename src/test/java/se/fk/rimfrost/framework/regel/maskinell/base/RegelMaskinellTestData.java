package se.fk.rimfrost.framework.regel.maskinell.base;

import se.fk.rimfrost.framework.handlaggning.model.ImmutableProduceratResultat;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUnderlag;
import se.fk.rimfrost.framework.handlaggning.model.Underlag;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.ProduceratResultat;
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

   public static ArrayList<se.fk.rimfrost.framework.handlaggning.model.ProduceratResultat> createProduceradeResultatForTest()
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
                  .id(UUID.fromString("66666666-6666-6666-6666-666667771234"))
                  .typ("TEST_PRODUCERAT_RESULTAT_TYP_2")
                  .version(1)
                  .resultatFrom(OffsetDateTime.parse("2025-02-01T00:00:00Z"))
                  .resultatTom(OffsetDateTime.parse("2025-02-02T00:00:00Z"))
                  .yrkandeStatus("YRKAT")
                  .data("TEST_PRODUCERAT_RESULTAT_TYP_2")
                  .build()));
   }

   public static ArrayList<ProduceratResultat> createExpectedProduceradeResultat()
   {
      ArrayList<ProduceratResultat> list = new ArrayList<>();

      ProduceratResultat r1 = new ProduceratResultat();
      r1.setId(UUID.fromString("66666666-6666-6666-6666-666666661234"));
      r1.setTyp("TEST_PRODUCERAT_RESULTAT_TYP_1");
      r1.setVersion(1);
      r1.setFrom(OffsetDateTime.parse("2025-01-01T00:00:00Z"));
      r1.setTom(OffsetDateTime.parse("2025-01-02T00:00:00Z"));
      r1.setYrkandestatus("UNDER_UTREDNING");
      r1.setData("TEST_PRODUCERAT_RESULTAT_TYP_1");

      ProduceratResultat r2 = new ProduceratResultat();
      r2.setId(UUID.fromString("66666666-6666-6666-6666-666667771234"));
      r2.setTyp("TEST_PRODUCERAT_RESULTAT_TYP_2");
      r2.setVersion(1);
      r2.setFrom(OffsetDateTime.parse("2025-02-01T00:00:00Z"));
      r2.setTom(OffsetDateTime.parse("2025-02-02T00:00:00Z"));
      r2.setYrkandestatus("YRKAT");
      r2.setData("TEST_PRODUCERAT_RESULTAT_TYP_2");

      list.add(r1);
      list.add(r2);

      return list;
   }
}
