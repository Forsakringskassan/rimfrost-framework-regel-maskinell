package se.fk.rimfrost.framework.regel.maskinell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.RegelRequestMessagePayload;
import se.fk.rimfrost.framework.regel.RegelRequestMessagePayloadData;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.test.RegelTest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutHandlaggningRequest;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestdata.createUnderlagListForTest;

@SuppressWarnings(
{
      "SameParameterValue", "unused"
})
public abstract class AbstractRegelMaskinellTest extends RegelTest
{
   UUID handlaggningId = UUID.fromString("11111111-1111-1111-1111-111111111234");

   @Inject
   RegelMaskinellServiceInterface regelMaskinellService;

   @Inject
   HandlaggningAdapter handlaggningAdapter;

   protected void resetState()
   {
      wiremockServer.resetRequests();
      inMemoryConnector.sink(regelResponsesChannel).clear();
   }

   //
   // Regel create / response
   //

   protected void sendRegelRequest(String handlaggningId)
   {
      RegelRequestMessagePayload payload = new RegelRequestMessagePayload();
      RegelRequestMessagePayloadData data = new RegelRequestMessagePayloadData();
      data.setHandlaggningId(handlaggningId);
      data.setAktivitetId("9b9d8261-559b-48db-b8bb-cbf61401c0ae");
      payload.setSpecversion(se.fk.rimfrost.framework.regel.SpecVersion.NUMBER_1_DOT_0);
      payload.setId("99994567-89ab-4cde-9012-3456789abcde");
      payload.setSource("TestSource-001");
      payload.setType(regelRequestsChannel);
      payload.setKogitoprocid("234567");
      payload.setKogitorootprocid("123456");
      payload.setKogitorootprociid("77774567-89ab-4cde-9012-3456789abcde");
      payload.setKogitoparentprociid("88884567-89ab-4cde-9012-3456789abcde");
      payload.setKogitoprocinstanceid("66664567-89ab-4cde-9012-3456789abcde");
      payload.setKogitoprocist("345678");
      payload.setKogitoprocversion("111");
      payload.setKogitoproctype(se.fk.rimfrost.framework.regel.KogitoProcType.BPMN);
      payload.setKogitoprocrefid("56789");
      payload.setData(data);
      inMemoryConnector.source(regelRequestsChannel).send(payload);
   }

   protected void verifyRegelResponseProduced()
   {
      Assertions.assertEquals(1, waitForMessages(regelResponsesChannel).size());
   }

   protected void verifyRegelResponseContent(String handlaggningId, Utfall utfall)
   {
      var msg = (RegelResponseMessagePayload) waitForMessages(regelResponsesChannel)
            .getFirst().getPayload();
      Assertions.assertEquals(handlaggningId, msg.getData().getHandlaggningId());
      Assertions.assertEquals(utfall, msg.getData().getUtfall());
   }

   //
   // Handläggning
   //

   protected Handlaggning getWiremockHandlaggning(UUID handlaggningId)
   {
      return handlaggningAdapter.readHandlaggning(handlaggningId);
   }

   protected void verifyGetHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForWireMockRequest(wiremockServer, handlaggningEndpoint + handlaggningId, 1);
      assertEquals(1, requests.stream().filter(p -> p.getMethod().equals(RequestMethod.GET)).count());
   }

   protected void verifyPutHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForWireMockRequest(wiremockServer, handlaggningEndpoint + handlaggningId, 1);
      assertEquals(1, requests.stream().filter(p -> p.getMethod().equals(RequestMethod.PUT)).count());
   }

   protected void verifyPutHandlaggningProduceradeResultat(String handlaggningId)
   {
      var handlaggningUpdate = getLastPutHandlaggningUpdate(handlaggningId);
      var sentProduceradeResultat = handlaggningUpdate.getYrkande().getProduceradeResultat();
      Assertions.assertEquals(3, sentProduceradeResultat.size());
      Assertions.assertEquals(sentProduceradeResultat.get(0).getId(), UUID.fromString("66666666-6666-6666-6666-666666661234"));
      Assertions.assertEquals(sentProduceradeResultat.get(0).getYrkandestatus(),
            se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.UNDER_UTREDNING);
      Assertions.assertEquals(sentProduceradeResultat.get(1).getId(), UUID.fromString("d89ca33f-eeeb-48fa-850f-7b9d9b07cc87"));
      Assertions.assertEquals(sentProduceradeResultat.get(1).getYrkandestatus(),
            se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.YRKAT);
      Assertions.assertEquals(sentProduceradeResultat.get(2).getId(), UUID.fromString("66666666-6666-6666-6666-666667771234"));
      Assertions.assertEquals(sentProduceradeResultat.get(2).getYrkandestatus(),
            se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.YRKAT);
   }

   @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
   protected void verifyPutHandlaggningUnderlag(String handlaggningId)
   {
      var handlaggningUpdate = getLastPutHandlaggningUpdate(handlaggningId);
      var expectedUnderlag = createUnderlagListForTest();
      var sentUnderlag = handlaggningUpdate.getUnderlag();
      Assertions.assertEquals(2, sentUnderlag.size());
      Assertions.assertEquals(expectedUnderlag.get(0).typ(), sentUnderlag.get(0).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(0).version(), sentUnderlag.get(0).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(0).data(), sentUnderlag.get(0).getData());
      Assertions.assertEquals(expectedUnderlag.get(1).typ(), sentUnderlag.get(1).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(1).version(), sentUnderlag.get(1).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(1).data(), sentUnderlag.get(1).getData());
   }

   protected void verifyPutHandlaggningContent(String handlaggningId)
   {
      var handlaggningUpdate = getLastPutHandlaggningUpdate(handlaggningId);
      assertEquals(se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.YRKAT,
            handlaggningUpdate.getYrkande().getYrkandestatus());
      assertEquals(se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.UppgiftStatus.PLANERAD,
            handlaggningUpdate.getUppgift().getUppgiftStatus());
      assertEquals("a42ffaed-2f20-47e8-8499-f2f79ae2f45f",
            handlaggningUpdate.getUppgift().getUppgiftspecifikation().getId().toString());
   }

   protected se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.HandlaggningUpdate getLastPutHandlaggningUpdate(
         String handlaggningId)
   {
      var request = getLastPutHandlaggningRequest(handlaggningId);
      PutHandlaggningRequest putHandlaggningRequest;
      try
      {
         putHandlaggningRequest = mapper.readValue(request.getBodyAsString(), PutHandlaggningRequest.class);
      }
      catch (JsonProcessingException e)
      {
         throw new RuntimeException(e);
      }
      return putHandlaggningRequest.getHandlaggning();
   }

   private LoggedRequest getLastPutHandlaggningRequest(String handlaggningId)
   {
      var requests = waitForWireMockRequest(wiremockServer, handlaggningEndpoint + handlaggningId, 1);
      return requests.stream()
            .filter(r -> r.getMethod().equals(RequestMethod.PUT))
            .reduce((first, second) -> second)
            .orElseThrow();
   }

}
