package se.fk.rimfrost.framework.regel.maskinell.logic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.oul.model.Erbjudande;
import se.fk.rimfrost.framework.referensdata.ErbjudandeReferensdataInterface;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.logic.CloudEventAttributesMapper;
import se.fk.rimfrost.framework.regel.logic.KompletteringKontrollInterface;
import se.fk.rimfrost.framework.regel.logic.KompletteringOulHandler;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableCloudEventData;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellErrorResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellSuccessResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.Result;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.RetriesExhaustedException;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.RetryUtil;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelMaskinellRequestHandler extends RegelRequestHandlerBase implements RegelRequestHandlerInterface
{
   @Inject
   RegelMaskinellServiceInterface regelService;

   @Inject
   KompletteringKontrollInterface kompletteringKontroll;

   @Inject
   KompletteringOulHandler kompletteringOulHandler;

   @Inject
   RegelMaskinellMapper maskinellMapper;

   @Inject
   ErbjudandeReferensdataInterface erbjudandeReferensdata;

   @ConfigProperty(name = "rimfrost.framework.regel.maskinell.retry.intervals")
   List<Integer> retryIntervals;

   Logger logger = LoggerFactory.getLogger(RegelMaskinellRequestHandler.class);

   @Override
   public void handleRegelRequest(RegelDataRequest request)
   {
      // Hämta handläggningsinformation
      CloudEventData cloudevent;
      try
      {
         cloudevent = createCloudEvent(request);
      }
      catch (Exception e)
      {
         logger.error(
               "Failed to create cloud event data from RegelDataRequest for handlaggning. Handlaggning id: {}, kogitoproc instance id: {}, aktivitet id: {}",
               request.handlaggningId(), request.kogitoprocinstanceid(), request.aktivitetId(), e);
         return;
      }

      try
      {
         Handlaggning handlaggning = null;
         try
         {
            handlaggning = RetryUtil.getWithRetries(() -> getHandlaggning(request.handlaggningId()), retryIntervals);
         }
         catch (RetriesExhaustedException e)
         {
            logger.error("Failed to read handlaggning. Handlaggning id: {}, kogitoproc instance id: {}, aktivitet id: {}",
                  request.handlaggningId(), request.kogitoprocinstanceid(), request.aktivitetId());
            var regelErrorInfo = createRegelErrorInformation(RegelFelkod.RIMFROST_HANDLAGGNING_READ_FAILURE,
                  "Failed to read handlaggning. Handlaggning id: " + request.handlaggningId()
                        + ", kogitoproc instance id: " + request.kogitoprocinstanceid() + ", aktivitet id: "
                        + request.aktivitetId());
            sendErrorResponse(request.handlaggningId(), cloudevent, regelErrorInfo, request.replyTo());
            return;
         }

         var uppgift = createUppgift(request.aktivitetId(), null);

         // Uppdatera handläggningsinformation
         var regelMaskinellRequest = maskinellMapper.toRegelMaskinellRequest(handlaggning, uppgift,
               request.kogitoprocinstanceid());

         if (!kompletteringKontroll.checkKomplettering(regelMaskinellRequest.handlaggning()).isEmpty())
         {
            var erbjudandeId = regelMaskinellRequest.handlaggning().yrkande().erbjudandeId();
            var erbjudande = createErbjudande(erbjudandeId, erbjudandeReferensdata.getErbjudandeNamn(erbjudandeId));
            try
            {
               kompletteringOulHandler.initiate(
                     request,
                     CloudEventAttributesMapper.toAttributes(cloudevent),
                     regelConfig,
                     erbjudande);
            }
            catch (OulException e)
            {
               var regelErrorInfo = createRegelErrorInformation(RegelFelkod.RIMFROST_OTHER,
                     "Failed to initiate komplettering. Handlaggning id: " + request.handlaggningId()
                           + ", kogitoproc instance id: " + request.kogitoprocinstanceid() + ", aktivitet id: "
                           + request.aktivitetId());
               sendErrorResponse(request.handlaggningId(), cloudevent, regelErrorInfo, request.replyTo());
            }
            return;
         }

         RegelMaskinellResult regelResult;
         try
         {
            regelResult = regelService.processRegel(regelMaskinellRequest);
         }
         catch (Exception e)
         {
            logger.error("Failed to process regel request. Handlaggning id: {}, kogitoproc instance id: {}, aktivitet id: {}",
                  request.handlaggningId(), request.kogitoprocinstanceid(), request.aktivitetId(), e);
            var regelErrorInfo = createRegelErrorInformation(RegelFelkod.RIMFROST_OTHER,
                  "Failed to process regel request. Handlaggning id: " + request.handlaggningId()
                        + ", kogitoproc instance id: " + request.kogitoprocinstanceid() + ", aktivitet id: "
                        + request.aktivitetId());
            sendErrorResponse(request.handlaggningId(), cloudevent, regelErrorInfo, request.replyTo());
            return;
         }

         if (regelResult instanceof RegelMaskinellErrorResult)
         {
            sendErrorResponse(request.handlaggningId(), cloudevent,
                  ((RegelMaskinellErrorResult) regelResult).regelErrorInformation(), request.replyTo());
            return;
         }

         var regelSuccessResult = (RegelMaskinellSuccessResult) regelResult;

         try
         {
            RetryUtil.runWithRetries(() -> updateHandlaggning(regelSuccessResult.handlaggningUpdate()), retryIntervals);
         }
         catch (RetriesExhaustedException e)
         {
            logger.error("Failed to write handlaggning update. Handlaggning id: {}, kogitoproc instance id: {}, aktivitet id: {}",
                  request.handlaggningId(), request.kogitoprocinstanceid(), request.aktivitetId());

            var regelErrorInfo = createRegelErrorInformation(RegelFelkod.RIMFROST_HANDLAGGNING_WRITE_FAILURE,
                  "Failed to write handlaggning update. Handlaggning id: " + request.handlaggningId()
                        + ", kogitoproc instance id: " + request.kogitoprocinstanceid() + ", aktivitet id: "
                        + request.aktivitetId());
            sendErrorResponse(request.handlaggningId(), cloudevent, regelErrorInfo, request.replyTo());
            return;
         }

         // Avsluta regel
         sendResponse(request.handlaggningId(), cloudevent, regelSuccessResult.utfall(), request.replyTo());
      }
      catch (Exception e)
      {
         logger.error(
               "Failed to handle regel data request for handlaggning due to unexpected error. Handlaggning id: {}, kogitoproc instance id: {}, aktivitet id: {}",
               request.handlaggningId(), request.kogitoprocinstanceid(), request.aktivitetId(), e);

         if (cloudevent != null)
         {
            tryDeleteCloudEventData(request.handlaggningId());
         }

         var regelErrorInfo = createRegelErrorInformation(RegelFelkod.RIMFROST_OTHER,
               "Failed to handle regel data request for handlaggning due to unexpected error. Handlaggning id: "
                     + request.handlaggningId() + ", kogitoproc instance id: " + request.kogitoprocinstanceid()
                     + ", aktivitet id: " + request.aktivitetId());
         sendErrorResponse(request.handlaggningId(), cloudevent, regelErrorInfo, request.replyTo());
         return;
      }
   }

   private Result<Handlaggning> getHandlaggning(UUID handlaggningId)
   {
      try
      {
         return Result.of(handlaggningAdapter.readHandlaggning(handlaggningId));
      }
      catch (HandlaggningException e)
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
      catch (HandlaggningException e)
      {
         // NOOP
      }

      return false;
   }
}
