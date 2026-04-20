package se.fk.rimfrost.framework.regel.maskinell;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.test.AbstractRegelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.rimfrost.framework.regel.test.RegelKafkaConnector.regelResponsesChannel;
import static se.fk.rimfrost.framework.regel.test.WireMockHandlaggning.waitForHandlaggningRequests;

public class RegelMaskinellTestBase extends AbstractRegelTest
{

   @BeforeEach
   void resetState()
   {
      var wireMockServer = WireMockRegelMaskinell.getWireMockServer();
      if (wireMockServer != null && wireMockServer.isRunning())
      {
         wireMockServer.resetRequests();
      }
   }

   //
   // Regel create / response
   //

   protected void sendRegelRequest(String handlaggningId)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
   }

   protected void verifyRegelResponseProduced()
   {
      Assertions.assertEquals(1, regelKafkaConnector.waitForMessages(regelResponsesChannel).size());
   }

   protected void verifyRegelResponseContent(String handlaggningId, Utfall utfall)
   {
      var msg = regelKafkaConnector.waitForRegelResponse();
      Assertions.assertEquals(handlaggningId, msg.getData().getHandlaggningId());
      Assertions.assertEquals(utfall, msg.getData().getUtfall());
   }

   //
   // Handläggning
   //

   protected void verifyGetHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForHandlaggningRequests(handlaggningId, RequestMethod.GET, 1);
      assertEquals(1, requests.size());
   }

   protected void verifyPutHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForHandlaggningRequests(handlaggningId, RequestMethod.PUT, 1);
      assertEquals(1, requests.size());
   }
}
