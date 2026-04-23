package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.HANDLAGGNING_ID;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockRegelMaskinell.class)
})
public class RegelMaskinellResponseTest extends AbstractRegelMaskinellTest
{

   @ParameterizedTest
   @CsvSource(
   {
         "JA",
         "UTREDNING",
         "NEJ"
   })
   void should_return_correct_regel_response(String expectedUtfall)
   {
      RegelMaskinellTestService.utfall = Utfall.valueOf(expectedUtfall);
      sendRegelRequest(HANDLAGGNING_ID);
      verifyRegelResponseContent(HANDLAGGNING_ID, Utfall.valueOf(expectedUtfall));
   }

}
