package se.fk.rimfrost.framework.regel.maskinell.logic;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;

@ApplicationScoped
public class RegelMaskinellMapper
{

   public RegelMaskinellRequest toRegelMaskinellRequest(Handlaggning handlaggning, Uppgift uppgift, UUID processInstansId)
   {
      return ImmutableRegelMaskinellRequest.builder()
            .handlaggning(handlaggning)
            .uppgift(uppgift)
            .processInstansId(processInstansId)
            .build();
   }

   public HandlaggningUpdate toHandlaggningUpdate(Handlaggning handlaggning,
         List<Underlag> underlag,
         Yrkande yrkande,
         Uppgift uppgift,
         UUID processinstanceId)
   {
      return ImmutableHandlaggningUpdate.builder()
            .id(handlaggning.id())
            .version(handlaggning.version())
            .yrkande(yrkande)
            .processInstansId(processinstanceId)
            .skapadTS(handlaggning.skapadTS())
            .avslutadTS(handlaggning.avslutadTS())
            .handlaggningspecifikationId(handlaggning.handlaggningspecifikationId())
            .underlag(underlag)
            .uppgift(uppgift)
            .build();
   }
}
