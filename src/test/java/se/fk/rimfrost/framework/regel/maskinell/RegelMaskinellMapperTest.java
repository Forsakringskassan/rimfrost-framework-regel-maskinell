package se.fk.rimfrost.framework.regel.maskinell;

import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RegelMaskinellMapperTest
{

   @Test
   void toHandlaggning_should_merge_producerade_resultat_and_replace_existing_with_updated()
   {

      //
      // setup test data
      //

      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();
      UUID id3 = UUID.randomUUID();

      var existingResultat1 = createProduceratResultat(id1, Yrkandestatus.YRKAT);
      var existingResultat2 = createProduceratResultat(id2, Yrkandestatus.YRKAT);
      var updatedResultat1 = createProduceratResultat(id2, Yrkandestatus.FASTSTALLT);
      var updatedResultat2 = createProduceratResultat(id3, Yrkandestatus.FASTSTALLT);

      Yrkande yrkande = ImmutableYrkande.builder()
            .id(UUID.randomUUID())
            .version(1)
            .erbjudandeId(UUID.randomUUID())
            .yrkandeDatum(OffsetDateTime.now())
            .yrkandeStatus(Yrkandestatus.YRKAT)
            .yrkandeFrom(OffsetDateTime.now())
            .yrkandeTom(OffsetDateTime.now())
            .avsikt("NY")
            .individYrkandeRoller(List.of())
            .produceradeResultat(List.of(existingResultat1, existingResultat2))
            .build();

      HandlaggningRead handlaggningRead = ImmutableHandlaggningRead.builder()
            .id(UUID.randomUUID())
            .version(1)
            .yrkande(yrkande)
            .processInstansId(UUID.randomUUID())
            .skapadTS(OffsetDateTime.now())
            .handlaggningspecifikationId(UUID.randomUUID())
            .build();

      var uppgiftSpecifikation = ImmutableUppgiftSpecifikation.builder()
            .id(UUID.randomUUID())
            .version(1)
            .build();

      var uppgift = ImmutableUppgift.builder()
            .id(UUID.randomUUID())
            .version(1)
            .skapadTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.TILLDELAD)
            .aktivitetId(UUID.randomUUID())
            .fSSAinformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .underlag(List.of())
            .uppgiftSpecifikation(uppgiftSpecifikation)
            .build();

      var mapper = new RegelMaskinellMapper();

      //
      // act
      //

      var handlaggning = mapper.toHandlaggning(
            handlaggningRead,
            List.of(),
            List.of(updatedResultat1, updatedResultat2),
            uppgift,
            UUID.randomUUID());
      //
      // assert
      //

      List<UUID> ids = handlaggning.yrkande().produceradeResultat().stream()
            .map(ProduceratResultat::id)
            .toList();

      // assert ids (no duplicates + correct merge)
      assertThat(ids).containsExactlyInAnyOrder(id1, id2, id3);

      Map<UUID, Yrkandestatus> statusById = handlaggning.yrkande().produceradeResultat().stream()
            .collect(Collectors.toMap(
                  ProduceratResultat::id,
                  ProduceratResultat::yrkandeStatus));

      // assert untouched element preserved
      assertThat(statusById).containsEntry(id1, Yrkandestatus.YRKAT);
      // assert replacement behavior
      assertThat(statusById).containsEntry(id2, Yrkandestatus.FASTSTALLT);
      // assert new element added
      assertThat(statusById).containsEntry(id3, Yrkandestatus.FASTSTALLT);
   }

   private ProduceratResultat createProduceratResultat(UUID id, Yrkandestatus yrkandestatus)
   {
      return ImmutableProduceratResultat.builder()
            .id(id)
            .version(1)
            .resultatFrom(OffsetDateTime.now())
            .resultatTom(OffsetDateTime.now())
            .yrkandeStatus(yrkandestatus)
            .typ("TestTyp")
            .data("TestData")
            .build();
   }
}
