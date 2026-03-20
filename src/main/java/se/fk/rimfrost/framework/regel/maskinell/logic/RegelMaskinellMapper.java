package se.fk.rimfrost.framework.regel.maskinell.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;

@ApplicationScoped
public class RegelMaskinellMapper
{

   public RegelMaskinellRequest toRegelMaskinellRequest(UUID handlaggningId, UUID aktivitetId)
   {
      return ImmutableRegelMaskinellRequest.builder()
            .handlaggningId(handlaggningId)
            .aktivitetId(aktivitetId)
            .build();
   }

   public Handlaggning toHandlaggning(HandlaggningRead handlaggningRead,
         List<Underlag> underlag,
         List<ProduceratResultat> uppdateradeResultat,
         Uppgift uppgift,
         UUID processinstanceId)
   {
      return ImmutableHandlaggning.builder()
            .id(handlaggningRead.id())
            .version(handlaggningRead.version())
            .yrkande(addProduceradeResultat(handlaggningRead.yrkande(), uppdateradeResultat))
            .processInstansId(processinstanceId)
            .skapadTS(handlaggningRead.skapadTS())
            .avslutadTS(handlaggningRead.avslutadTS())
            .handlaggningspecifikationId(handlaggningRead.handlaggningspecifikationId())
            .underlag(underlag)
            .uppgift(uppgift)
            .build();
   }

   private Yrkande addProduceradeResultat(Yrkande yrkande, List<ProduceratResultat> uppdateradeResultat)
   {
      return ImmutableYrkande.builder()
            .id(yrkande.id())
            .version(yrkande.version())
            .erbjudandeId(yrkande.erbjudandeId())
            .yrkandeDatum(yrkande.yrkandeDatum())
            .yrkandeStatus(yrkande.yrkandeStatus())
            .yrkandeFrom(yrkande.yrkandeFrom())
            .yrkandeTom(yrkande.yrkandeTom())
            .avsikt(yrkande.avsikt())
            .individYrkandeRoller(yrkande.individYrkandeRoller())
            .produceradeResultat(mergeProduceradeResultat(uppdateradeResultat, yrkande.produceradeResultat()))
            .build();
   }

   private List<ProduceratResultat> mergeProduceradeResultat(List<ProduceratResultat> uppdateradeResultat,
         List<ProduceratResultat> tidigareResultat)
   {
      Set<UUID> idsInUppdateradeResultat = uppdateradeResultat.stream()
            .map(ProduceratResultat::id)
            .collect(Collectors.toSet());
      List<ProduceratResultat> result = new ArrayList<>(uppdateradeResultat);
      tidigareResultat.stream()
            .filter(a -> !idsInUppdateradeResultat.contains(a.id()))
            .forEach(result::add);
      return result;
   }
}
