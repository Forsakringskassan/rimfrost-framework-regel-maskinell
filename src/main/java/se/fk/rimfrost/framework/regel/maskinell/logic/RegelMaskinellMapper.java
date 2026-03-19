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

   public RegelMaskinellRequest toRegelMaskinellRequest(UUID handlaggningId, UUID aktivitetId)
   {
      return ImmutableRegelMaskinellRequest.builder()
            .handlaggningId(handlaggningId)
            .aktivitetId(aktivitetId)
            .build();
   }

   public Handlaggning toHandlaggning(HandlaggningRead handlaggningRead,
                                      List<Underlag> underlag,
                                      Uppgift uppgift,
                                      UUID processinstanceId)
   {
      return ImmutableHandlaggning.builder()
            .id(handlaggningRead.id())
            .version(handlaggningRead.version())
            .yrkande(handlaggningRead.yrkande())
            .processInstansId(processinstanceId)
            .skapadTS(handlaggningRead.skapadTS())
            .avslutadTS(handlaggningRead.avslutadTS())
            .handlaggningspecifikationId(handlaggningRead.handlaggningspecifikationId())
            .underlag(underlag)
            .uppgift(uppgift)
            .build();
   }
}
