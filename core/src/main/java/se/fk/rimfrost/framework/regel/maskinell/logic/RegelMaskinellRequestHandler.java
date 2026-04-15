package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import se.fk.rimfrost.framework.uppgiftstatusprovider.UppgiftStatusProvider;

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

   @Inject
   UppgiftStatusProvider uppgiftStatusProvider;

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
            .uppgiftStatus(uppgiftStatusProvider.getTilldeladId())
            .aktivitetId(request.aktivitetId())
            .fSSAinformation("HANDLAGGNING_PAGAR") // TODO: Replace with correct value once available
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
