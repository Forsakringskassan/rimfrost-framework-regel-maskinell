package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelMaskinellRequestHandler extends RegelRequestHandlerBase implements RegelRequestHandlerInterface
{
   @Inject
   private RegelMaskinellServiceInterface regelService;

   @Inject
   private RegelMaskinellMapper maskinellMapper;

   @Override
   public void handleRegelRequest(RegelDataRequest request)
   {
      // Hämta handläggningsinformation
      var cloudevent = createCloudEvent(request);
      var handlaggning = handlaggningAdapter.readHandlaggning(request.handlaggningId());

      var uppgiftSpecifikation = ImmutableUppgiftSpecifikation.builder()
            .id(regelConfig.getSpecifikation().getId())
            .version(regelConfig.getSpecifikation().getVersion())
            .build();

      var uppgift = ImmutableUppgift.builder()
            .id(UUID.randomUUID())
            .version(1)
            .skapadTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.TILLDELAD)
            .aktivitetId(request.aktivitetId())
            .fSSAinformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .uppgiftSpecifikation(uppgiftSpecifikation)
            .build();

      // Uppdatera handläggningsinformation
      var regelMaskinellRequest = maskinellMapper.toRegelMaskinellRequest(handlaggning, uppgift, request.kogitoprocinstanceid());
      var regelResult = regelService.processRegel(regelMaskinellRequest);

      handlaggningAdapter.updateHandlaggning(regelResult.handlaggningUpdate());

      // Avsluta regel
      sendResponse(request.handlaggningId(), cloudevent, regelResult.utfall());
   }
}
