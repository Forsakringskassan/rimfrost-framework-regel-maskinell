package se.fk.rimfrost.framework.regel.maskinell;

import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegelMaskinellTestdata
{
   //
   // Test data
   //

   public static final String HANDLAGGNING_ID = "11111111-1111-1111-1111-111111111234";

   public static RegelDataRequest createRegelDataRequestForTest(String handlaggningId)
   {
      return ImmutableRegelDataRequest
            .builder()
            .id(UUID.fromString("99994567-89ab-4cde-9012-3456789abcde"))
            .handlaggningId(UUID.fromString(handlaggningId))
            .aktivitetId(UUID.fromString("0644fc72-f18b-4552-ba37-80df1ee6629c"))
            .kogitorootprocid("123456")
            .kogitorootprociid(UUID.fromString("77774567-89ab-4cde-9012-3456789abcde"))
            .kogitoparentprociid(UUID.fromString("88884567-89ab-4cde-9012-3456789abcde"))
            .kogitoprocid("234567")
            .kogitoprocinstanceid(UUID.fromString("66664567-89ab-4cde-9012-3456789abcde"))
            .kogitoprocist("345678")
            .kogitoprocversion("111")
            .type("regel-requests")
            .build();
   }

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
                  .yrkandeStatus(Yrkandestatus.UNDER_UTREDNING)
                  .data("TEST_PRODUCERAT_RESULTAT_TYP_1")
                  .build(),
            ImmutableProduceratResultat.builder()
                  .id(UUID.fromString("d89ca33f-eeeb-48fa-850f-7b9d9b07cc87"))
                  .typ("TEST_PRODUCERAT_RESULTAT_TYP_2")
                  .version(1)
                  .resultatFrom(OffsetDateTime.parse("2025-02-01T00:00:00Z"))
                  .resultatTom(OffsetDateTime.parse("2025-02-02T00:00:00Z"))
                  .yrkandeStatus(Yrkandestatus.YRKAT)
                  .data("TEST_PRODUCERAT_RESULTAT_TYP_2")
                  .build()));
   }

   public static Iterable<Yrkande.IndividYrkandeRoll> createIndividYrkandeRollerForTest()
   {
      var yrkandeRoll = ImmutableIndividYrkandeRoll.builder()
            .individId(UUID.randomUUID())
            .yrkandeRollId(UUID.randomUUID())
            .build();
      return List.of(yrkandeRoll);
   }

   public static Yrkande createYrkandeForTest()
   {
      return ImmutableYrkande.builder()
            .id(UUID.randomUUID())
            .version(1)
            .erbjudandeId(UUID.randomUUID())
            .yrkandeDatum(OffsetDateTime.now())
            .yrkandeStatus(Yrkandestatus.YRKAT)
            .yrkandeFrom(OffsetDateTime.now())
            .yrkandeTom(OffsetDateTime.now())
            .avsikt("TestAvsikt")
            .individYrkandeRoller(createIndividYrkandeRollerForTest())
            .build();
   }

   public static Uppgift createUppgiftForTest()
   {
      var uppgiftSpecifikation = ImmutableUppgiftSpecifikation.builder()
            .id(UUID.randomUUID())
            .version(1)
            .build();
      return ImmutableUppgift.builder()
            .id(UUID.randomUUID())
            .version(1)
            .skapadTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.PLANERAD)
            .aktivitetId(UUID.randomUUID())
            .fSSAinformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .underlag(createUnderlagListForTest())
            .uppgiftSpecifikation(uppgiftSpecifikation)
            .build();
   }

   public static HandlaggningUpdate createHandlaggningUpdateForTest(String handlaggningId)
   {
      return ImmutableHandlaggningUpdate.builder()
            .id(UUID.fromString(handlaggningId))
            .version(1)
            .yrkande(createYrkandeForTest())
            .processInstansId(UUID.randomUUID())
            .skapadTS(OffsetDateTime.now())
            .handlaggningspecifikationId(UUID.randomUUID())
            .underlag(createUnderlagListForTest())
            .uppgift(createUppgiftForTest())
            .build();
   }
}
