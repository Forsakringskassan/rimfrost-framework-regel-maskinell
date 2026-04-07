package se.fk.rimfrost.framework.regel.maskinell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.test.AbstractRegelTest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutHandlaggningRequest;
import static org.junit.Assert.assertEquals;
import static se.fk.rimfrost.framework.regel.test.RegelTestData.newRegelRequestMessagePayload;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractRegelMaskinellTest extends AbstractRegelTest
{

   @SuppressWarnings("unused")
   @Inject
   HandlaggningAdapter handlaggningAdapter;

   @BeforeEach
   void resetState()
   {
      wiremockServer.resetRequests();
      inMemoryConnector.sink(regelResponsesChannel).clear();
   }

   //
   // Regel create / response
   //

   protected void sendRegelRequest(String handlaggningId)
   {
      var payload = newRegelRequestMessagePayload(handlaggningId);
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

}
