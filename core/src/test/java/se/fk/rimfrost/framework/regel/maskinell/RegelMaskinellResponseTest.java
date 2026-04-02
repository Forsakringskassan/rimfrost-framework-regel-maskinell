package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.regel.Utfall;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.HANDLAGGNING_ID;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class RegelMaskinellResponseTest extends AbstractRegelMaskinellTest
{

   @BeforeEach
   void setup()
   {
      resetState();
   }

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
