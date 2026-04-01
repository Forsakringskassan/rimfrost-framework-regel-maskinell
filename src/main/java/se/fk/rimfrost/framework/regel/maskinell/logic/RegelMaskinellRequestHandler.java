package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.RegelUtils;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

      // Exekvera regellogik
      var uppgiftStarted = OffsetDateTime.now();
      var uppgiftId = UUID.randomUUID();
      var regelMaskinellRequest = maskinellMapper.toRegelMaskinellRequest(handlaggning);

      // Uppdatera handläggningsinformation
      var regelResult = regelService.processRegel(regelMaskinellRequest);
      var uppgiftSpecifikation = ImmutableUppgiftSpecifikation.builder()
            .id(regelConfig.getSpecifikation().getId())
            .version(regelConfig.getSpecifikation().getVersion())
            .build();

      var uppgift = ImmutableUppgift.builder()
            .id(uppgiftId)
            .version(1)
            .skapadTs(uppgiftStarted)
            .utfordTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.AVSLUTAD)
            .aktivitetId(request.aktivitetId())
            .fSSAinformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .uppgiftSpecifikation(uppgiftSpecifikation)
            .build();

      var updatedYrkande = RegelUtils.createYrkandeWithUpdatedProduceradeResultat(handlaggning.yrkande(),
            regelResult.handlaggningUpdate().yrkande().produceradeResultat());

      var handlaggningUpdate = ImmutableHandlaggningUpdate.builder().from(regelResult.handlaggningUpdate())
            .yrkande(updatedYrkande)
            .uppgift(uppgift)
            .build();
      handlaggningAdapter.updateHandlaggning(handlaggningUpdate);

      // Avsluta regel
      sendResponse(request.handlaggningId(), cloudevent, regelResult.utfall());
   }
}
