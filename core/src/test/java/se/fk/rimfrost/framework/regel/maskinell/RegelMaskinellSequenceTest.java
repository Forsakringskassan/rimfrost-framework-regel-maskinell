package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.HANDLAGGNING_ID;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class RegelMaskinellSequenceTest extends AbstractRegelMaskinellTest
{

   @BeforeEach
   void setup()
   {
      resetState();
   }

   @Test
   void TestRegelMaskinell()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyGetHandlaggningProduced(HANDLAGGNING_ID);
      verifyPutHandlaggningProduced(HANDLAGGNING_ID);
      verifyRegelResponseProduced();
   }

}
