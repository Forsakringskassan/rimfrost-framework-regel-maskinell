package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.KundbehovsflodeAdapter;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.ImmutableKundbehovsflodeRequest;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.RegelRequestHandlerBase;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.Beslutsutfall;
import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public abstract class RegelMaskinellRequestHandlerBase extends RegelRequestHandlerBase implements RegelRequestHandlerInterface
{

   /*
    * Note: The name of the @PostConstruct method should if
    * possible be kept as init<classname> in order to avoid
    * being shadowed by any @PostConstruct methods in any
    * inheriting class that happens to have the same method
    * name.
    */
   @PostConstruct
   private void initRegelMaskinellRequestHandlerBase()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }

   @Override
   public void handleRegelRequest(RegelDataRequest request)
   {
      var kundbehovsResponse = kundbehovsflodeAdapter.getKundbehovsflodeInfo(
            ImmutableKundbehovsflodeRequest.builder().kundbehovsflodeId(request.kundbehovsflodeId()).build());

      var processRegelResponse = regelService.processRegel(kundbehovsResponse);

      var cloudevent = createCloudEvent(request);

      var regelData = ImmutableRegelData.builder()
            .kundbehovsflodeId(request.kundbehovsflodeId())
            .skapadTs(OffsetDateTime.now())
            .planeradTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.AVSLUTAD)
            .fssaInformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .ersattningar(processRegelResponse.ersattningar())
            .underlag(processRegelResponse.underlag())
            .build();

      updateKundbehovsFlode(regelData);
      sendResponse(regelData, cloudevent, decideUtfall(regelData));
   }

   private Utfall decideUtfall(RegelData regelData)
   {
      return regelData.ersattningar().stream().allMatch(e -> e.beslutsutfall() == Beslutsutfall.JA) ? Utfall.JA : Utfall.NEJ;
   }

}
