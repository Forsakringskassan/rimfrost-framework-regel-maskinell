package se.fk.rimfrost.framework.regel.maskinell.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.regel.Utfall;

@Disabled("Base test class - not executable")
public abstract class AbstractRegelMaskinellResponseTest extends AbstractRegelMaskinellTest
{

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, JA"
   })
   void should_return_correct_regel_response(String handlaggningId, String expectedUtfallString)
   {
      var expectedUtfall = Utfall.valueOf(expectedUtfallString);
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();
      Assertions.assertEquals(handlaggningId, regelResponse.getData().getHandlaggningId());
      Assertions.assertEquals(expectedUtfall, regelResponse.getData().getUtfall());
   }

}
