package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableKundbehovsflodeRequest;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.dto.Beslutsutfall;
import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
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
      var cloudevent = createCloudEvent(request);

      var kundbehovsResponse = kundbehovsflodeAdapter.getKundbehovsflodeInfo(
            ImmutableKundbehovsflodeRequest.builder().kundbehovsflodeId(request.kundbehovsflodeId()).build());

      var result = regelService.processRegel(maskinellMapper.toRegelMaskinellRequest(kundbehovsResponse));

      updateKundbehovsFlode(request.kundbehovsflodeId(), maskinellMapper.toRegelResult(result));
      sendResponse(request.kundbehovsflodeId(), cloudevent, result.utfall());
   }

   private void updateKundbehovsFlode(UUID kundbehovsflodeId, RegelResult regelResult)
   {
      patchKundbehovsflode(kundbehovsflodeId, regelResult.ersattningar());
      putKundbehovsflode(kundbehovsflodeId, regelResult.uppgiftData(), regelResult.underlag());
   }

}
