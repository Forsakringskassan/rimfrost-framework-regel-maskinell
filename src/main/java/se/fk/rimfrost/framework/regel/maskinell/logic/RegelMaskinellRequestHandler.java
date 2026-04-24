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
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.RetriesExhaustedException;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.RetryUtil;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import se.fk.rimfrost.framework.uppgiftstatusprovider.UppgiftStatusProvider;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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

         RegelErrorInformation errorInformation = new RegelErrorInformation();
         errorInformation.setFelkod(RegelFelkod.HANDLAGGNING_READ_FAILURE);
         errorInformation.setFelmeddelande("Failed to read handlaggning. Handlaggning id: " + request.handlaggningId()
               + ", kogitoproc id: " + request.kogitoprocid() + ", aktivitet id: " + request.aktivitetId());
         sendResponse(request.handlaggningId(), cloudevent, errorInformation);
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

      try
      {
         RetryUtil.runWithRetries(() -> updateHandlaggning(regelResult.handlaggningUpdate()), retryIntervals);
      }
      catch (RetriesExhaustedException e)
      {
         logger.error("Failed to write handlaggning update. Handlaggning id: {}, kogitoproc id: {}, aktivitet id: {}",
               request.handlaggningId(), request.kogitoprocid(), request.aktivitetId());

         RegelErrorInformation errorInformation = new RegelErrorInformation();
         errorInformation.setFelkod(RegelFelkod.HANDLAGGNING_WRITE_FAILURE);
         errorInformation.setFelmeddelande("Failed to write handlaggning update. Handlaggning id: " + request.handlaggningId()
               + ", kogitoprocid: " + request.kogitoprocid() + ", aktivitetId: " + request.aktivitetId());
         sendResponse(request.handlaggningId(), cloudevent, errorInformation);
         return;
      }

      // Avsluta regel
      sendResponse(request.handlaggningId(), cloudevent, regelResult.utfall());
   }

   private Optional<Handlaggning> getHandlaggning(UUID handlaggningId)
   {
      try
      {
         return Optional.of(handlaggningAdapter.readHandlaggning(handlaggningId));
      }
      catch (WebApplicationException e)
      {
         return Optional.empty();
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
}
