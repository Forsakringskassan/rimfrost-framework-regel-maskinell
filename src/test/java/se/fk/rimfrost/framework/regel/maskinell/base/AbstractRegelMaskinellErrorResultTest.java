package se.fk.rimfrost.framework.regel.maskinell.base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.RegelFelkod;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellErrorResult;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base test class for verifying error result handling in Regel Maskinell.
 *
 * <p>This class tests the scenario where the rule service returns an error result,
 * ensuring that the error is properly propagated in the regel response.</p>
 *
 * <p><b>Extension points:</b></p>
 * <ul>
 *   <li>{@link #getRegelMaskinellService()} – provide the mocked RegelMaskinellServiceInterface</li>
 *   <li>{@link #createRegelErrorInformation()} – override to customize the error information</li>
 * </ul>
 *
 * <p>Subclasses must provide a mocked {@link RegelMaskinellServiceInterface} via
 * {@link #getRegelMaskinellService()}. The mock will be configured to return an error result.</p>
 */
@Disabled("Base test class - not executable")
public abstract class AbstractRegelMaskinellErrorResultTest extends AbstractRegelMaskinellTest
{

   /**
    * Returns the mocked RegelMaskinellServiceInterface.
    *
    * <p>Subclasses must implement this method to provide the mock,
    * typically using {@code @InjectMock} annotation.</p>
    *
    * @return the mocked RegelMaskinellServiceInterface
    */
   protected abstract RegelMaskinellServiceInterface getRegelMaskinellService();

   /**
    * Creates the RegelErrorInformation used in the error result test.
    *
    * <p>Subclasses may override this method to customize the error information.</p>
    *
    * @return the RegelErrorInformation to use in tests
    */
   protected RegelErrorInformation createRegelErrorInformation()
   {
      var regelErrorInformation = new RegelErrorInformation();
      regelErrorInformation.setFelkod(RegelFelkod.OTHER);
      regelErrorInformation.setFelmeddelande("Test");
      return regelErrorInformation;
   }

   /**
    * Returns the handläggning ID to use in tests.
    *
    * <p>Subclasses may override this method to use a different handläggning ID.</p>
    *
    * @return the handläggning ID
    */
   protected String getHandlaggningId()
   {
      return "11111111-1111-1111-1111-111111111234";
   }

   @Test
   void should_send_error_response_on_error_result()
   {
      var regelErrorInformation = createRegelErrorInformation();

      var regelErrorResponse = ImmutableRegelMaskinellErrorResult.builder()
            .regelErrorInformation(regelErrorInformation)
            .build();

      Mockito.when(getRegelMaskinellService().processRegel(Mockito.any())).thenReturn(regelErrorResponse);

      var handlaggningId = getHandlaggningId();
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(regelErrorInformation, regelResponse.getData().getError());
   }

}
