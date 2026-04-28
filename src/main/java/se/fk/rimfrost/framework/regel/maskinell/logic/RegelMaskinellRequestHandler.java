package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.RegelFelkod;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellErrorResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellSuccessResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.Result;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.RetriesExhaustedException;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.RetryUtil;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import se.fk.rimfrost.framework.uppgiftstatusprovider.UppgiftStatusProvider;
import java.time.OffsetDateTime;
import java.util.List;
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

   @ConfigProperty(name = "rimfrost.framework.regel.maskinell.retry.intervals")
   List<Integer> retryIntervals;

   Logger logger = LoggerFactory.getLogger(RegelMaskinellRequestHandler.class);

   @Override
   public void handleRegelRequest(RegelDataRequest request)
   {
      // Hämta handläggningsinformation
      var cloudevent = createCloudEvent(request);

      Handlaggning handlaggning = null;
      try
      {
         handlaggning = RetryUtil.getWithRetries(() -> getHandlaggning(request.handlaggningId()), retryIntervals);
      }
      catch (RetriesExhaustedException e)
      {
         logger.error("Failed to read handlaggning. Handlaggning id: {}, kogitoprocid: {}, aktivitetId: {}",
               request.handlaggningId(), request.kogitoprocid(), request.aktivitetId());

         sendErrorResponse(request.handlaggningId(), cloudevent, RegelFelkod.HANDLAGGNING_READ_FAILURE,
               "Failed to read handlaggning. Handlaggning id: " + request.handlaggningId()
                     + ", kogitoproc id: " + request.kogitoprocid() + ", aktivitet id: " + request.aktivitetId());
         return;
      }

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

      if (regelResult instanceof RegelMaskinellErrorResult)
      {
         sendErrorResponse(request.handlaggningId(), cloudevent,
               ((RegelMaskinellErrorResult) regelResult).regelErrorInformation());
         return;
      }

      var regelSuccessResult = (RegelMaskinellSuccessResult) regelResult;

      try
      {
         RetryUtil.runWithRetries(() -> updateHandlaggning(regelSuccessResult.handlaggningUpdate()), retryIntervals);
      }
      catch (RetriesExhaustedException e)
      {
         logger.error("Failed to write handlaggning update. Handlaggning id: {}, kogitoproc id: {}, aktivitet id: {}",
               request.handlaggningId(), request.kogitoprocid(), request.aktivitetId());

         sendErrorResponse(request.handlaggningId(), cloudevent, RegelFelkod.HANDLAGGNING_WRITE_FAILURE,
               "Failed to write handlaggning update. Handlaggning id: " + request.handlaggningId()
                     + ", kogitoprocid: " + request.kogitoprocid() + ", aktivitetId: " + request.aktivitetId());
         return;
      }

      // Avsluta regel
      sendResponse(request.handlaggningId(), cloudevent, regelSuccessResult.utfall());
   }

   private Result<Handlaggning> getHandlaggning(UUID handlaggningId)
   {
      try
      {
         return Result.of(handlaggningAdapter.readHandlaggning(handlaggningId));
      }
      catch (WebApplicationException e)
      {
         return Result.empty();
      }
   }

   private boolean updateHandlaggning(HandlaggningUpdate handlaggning)
   {
      try
      {
         handlaggningAdapter.updateHandlaggning(handlaggning);
         return true;
      }
      catch (WebApplicationException e)
      {
         // NOOP
      }

      return false;
   }

   private void sendErrorResponse(UUID handlaggningId, CloudEventData cloudEvent, RegelFelkod regelFelkod, String meddelande)
   {
      RegelErrorInformation errorInformation = new RegelErrorInformation();
      errorInformation.setFelkod(regelFelkod);
      errorInformation.setFelmeddelande(meddelande);

      sendErrorResponse(handlaggningId, cloudEvent, errorInformation);
   }

   private void sendErrorResponse(UUID handlaggningId, CloudEventData cloudEvent, RegelErrorInformation regelErrorInformation)
   {
      sendResponse(handlaggningId, cloudEvent, regelErrorInformation);
   }
}
