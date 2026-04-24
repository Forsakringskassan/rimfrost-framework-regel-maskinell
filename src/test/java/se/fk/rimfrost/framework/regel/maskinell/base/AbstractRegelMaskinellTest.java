package se.fk.rimfrost.framework.regel.maskinell.base;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.regel.RegelTestBase;
import se.fk.rimfrost.framework.regel.maskinell.helpers.WireMockRegelMaskinell;

/**
 * Base class for testing "maskinella regler" (machine-driven rules).
 *
 * <p>This test base provides common infrastructure testing of rules,
 * including:
 * <ul>
 *   <li>WireMock integration for simulating external HTTP services (handläggning)</li>
 * </ul></p>
 *
 * <p><b>Test isolation:</b>
 * External systems (WireMock) are reset between tests
 * to ensure no state leakage across test cases.</p>
 *
 * <p>This base class is intended to be extended by concrete rule test classes.</p>
 */
@Disabled("Base test class - not executable")
public abstract class AbstractRegelMaskinellTest extends RegelTestBase
{

   /**
    * Adapter for handling case/handling operations in test scenarios.
    */
   @SuppressWarnings("unused")
   @Inject
   HandlaggningAdapter handlaggningAdapter;

   /**
    * Resets external system state before each test execution.
    *
    * <p>This includes resetting the WireMock server used for handläggning
    * HTTP simulation, if it is running.</p>
    *
    * <p>Also ensures test isolation by clearing previously recorded requests
    * and interactions.</p>
    */
   @BeforeEach
   void regelMaskinellResetState()
   {
      super.regelResetState();
      if (inMemoryConnector == null)
      {
         throw new IllegalStateException("inMemoryConnector not injected");
      }
      var wireMockServer = WireMockRegelMaskinell.getWireMockServer();
      if (wireMockServer == null)
      {
         throw new IllegalStateException("WireMock not initialized");
      }
      wireMockServer.resetRequests();
   }

}
