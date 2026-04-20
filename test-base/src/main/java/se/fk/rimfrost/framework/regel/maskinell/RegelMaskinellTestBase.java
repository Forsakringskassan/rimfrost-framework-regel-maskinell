package se.fk.rimfrost.framework.regel.maskinell;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.test.AbstractRegelTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.rimfrost.framework.regel.test.RegelKafkaConnector.regelResponsesChannel;
import static se.fk.rimfrost.framework.regel.test.WireMockHandlaggning.waitForHandlaggningRequests;

/**
 * Base class for testing "maskinella regler" (machine-driven rules).
 *
 * <p>This test base provides common infrastructure testing of rules,
 * including:
 * <ul>
 *   <li>WireMock integration for simulating external HTTP services (handläggning)</li>
 *   <li>Reusable helper methods for asserting rule execution outcomes</li>
 * </ul>
 *
 * <p><b>Test isolation:</b>
 * External systems (WireMock) are reset between tests
 * to ensure no state leakage across test cases.
 *
 * <p>This base class is intended to be extended by concrete rule test classes
 * and provides a higher-level DSL for verifying rule behavior.
 */
public class RegelMaskinellTestBase extends AbstractRegelTest
{

   /**
    * Resets external system state before each test execution.
    *
    * <p>This includes resetting the WireMock server used for handläggning
    * HTTP simulation, if it is running.
    *
    * <p>This ensures test isolation by clearing previously recorded requests
    * and interactions.
    */
   @BeforeEach
   void resetState()
   {
      var wireMockServer = WireMockRegelMaskinell.getWireMockServer();
      if (wireMockServer != null && wireMockServer.isRunning())
      {
         wireMockServer.resetRequests();
      }
   }

   /**
    * Sends a rule evaluation request for the given handläggning ID.
    *
    * @param handlaggningId identifier of handlaggning
    */
   protected void sendRegelRequest(String handlaggningId)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
   }

   /**
    * Verifies that exactly one rule response message was produced.
    *
    * <p>Fails the test if the number of produced responses is not exactly one.
    */
   protected void verifyRegelResponseProduced()
   {
      Assertions.assertEquals(1, regelKafkaConnector.waitForMessages(regelResponsesChannel).size());
   }

   /**
    * Verifies the content of the produced rule response message.
    *
    * @param handlaggningId expected handläggning identifier
    * @param utfall expected outcome of the rule evaluation
    */
   protected void verifyRegelResponseContent(String handlaggningId, Utfall utfall)
   {
      var msg = regelKafkaConnector.waitForRegelResponse();
      Assertions.assertEquals(handlaggningId, msg.getData().getHandlaggningId());
      Assertions.assertEquals(utfall, msg.getData().getUtfall());
   }

   /**
    * Verifies that exactly one HTTP GET request was made to handläggning
    * for the given handläggning ID.
    *
    * @param handlaggningId identifier of the handlaggning
    */
   protected void verifyGetHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForHandlaggningRequests(handlaggningId, RequestMethod.GET, 1);
      assertEquals(1, requests.size());
   }

   /**
    * Verifies that exactly one HTTP PUT request was made to handläggning
    * for the given handläggning ID.
    *
    * @param handlaggningId identifier of the handlaggning
    */
   protected void verifyPutHandlaggningProduced(String handlaggningId)
   {
      var requests = waitForHandlaggningRequests(handlaggningId, RequestMethod.PUT, 1);
      assertEquals(1, requests.size());
   }
}
