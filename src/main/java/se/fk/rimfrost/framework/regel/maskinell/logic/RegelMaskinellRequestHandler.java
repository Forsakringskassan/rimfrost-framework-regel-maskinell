package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.rimfrost.framework.handlaggning.model.FSSAinformation;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgift;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgiftSpecifikation;
import se.fk.rimfrost.framework.handlaggning.model.UppgiftStatus;
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
      var handlaggningRead = handlaggningAdapter.readHandlaggning(request.handlaggningId());

      // Exekvera regellogik
      var uppgiftStarted = OffsetDateTime.now();
      var uppgiftId = UUID.randomUUID();
      var regelMaskinellRequest = maskinellMapper.toRegelMaskinellRequest(request.handlaggningId(), request.aktivitetId());

      // Uppdatera handläggningsinformation
      var result = regelService.processRegel(regelMaskinellRequest);
      var uppgiftSpecifikation = ImmutableUppgiftSpecifikation.builder()
            .id(regelConfig.getSpecifikation().getId())
            .version(regelConfig.getSpecifikation().getVersion())
            .build();
      var uppgift = ImmutableUppgift.builder()
            .id(uppgiftId)
            .version(1) // TODO behöver denna någonsin bumpas ??
            .skapadTs(uppgiftStarted)
            .utfordTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.AVSLUTAD)
            .aktivitetId(request.aktivitetId())
            .fSSAinformation(FSSAinformation.HANDLAGGNING_PAGAR) // TODO något annat!!!
            .underlag(result.underlag()) // TODO kan tas bort ?? är bara en del av uppdatering av handläggningen ??
            .uppgiftSpecifikation(uppgiftSpecifikation)
            .build();
      var handlaggning = maskinellMapper.toHandlaggning(handlaggningRead, result.underlag(), uppgift,
            request.kogitoprocinstanceid());
      handlaggningAdapter.updateHandlaggning(handlaggning);

      // Avsluta regel
      sendResponse(request.handlaggningId(), cloudevent, result.utfall());
   }

}
