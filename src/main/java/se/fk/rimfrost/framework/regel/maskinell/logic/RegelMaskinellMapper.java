package se.fk.rimfrost.framework.regel.maskinell.logic;

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

   public HandlaggningUpdate toHandlaggningUpdate(Handlaggning handlaggning, Uppgift uppgift)
   {
      return ImmutableHandlaggningUpdate.builder()
            .handlaggning(handlaggning)
            .uppgift(uppgift)
            .build();
   }
}
