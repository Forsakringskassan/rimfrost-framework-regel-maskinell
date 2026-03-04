package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.rimfrost.framework.handlaggning.adapter.dto.ImmutableHandlaggningRequest;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
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
      var cloudevent = createCloudEvent(request);

      var handlaggningResponse = handlaggningAdapter.getHandlaggningInfo(
            ImmutableHandlaggningRequest.builder().handlaggningId(request.handlaggningId()).build());

      var result = regelService.processRegel(maskinellMapper.toRegelMaskinellRequest(handlaggningResponse));

      updateHandlaggning(request.handlaggningId(), maskinellMapper.toRegelResult(result));
      sendResponse(request.handlaggningId(), cloudevent, result.utfall());
   }

   private void updateHandlaggning(UUID handlaggningId, RegelResult regelResult)
   {
      patchHandlaggning(handlaggningId, regelResult.ersattningar());
      putHandlaggning(handlaggningId, regelResult.uppgiftData(), regelResult.underlag());
   }

}
