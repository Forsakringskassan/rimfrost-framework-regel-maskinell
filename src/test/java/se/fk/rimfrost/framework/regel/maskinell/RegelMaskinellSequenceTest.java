package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.HANDLAGGNING_ID;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockRegelMaskinell.class)
})
public class RegelMaskinellSequenceTest extends AbstractRegelMaskinellTest
{

   @Test
   void TestRegelMaskinell()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyGetHandlaggningProduced(HANDLAGGNING_ID);
      verifyPutHandlaggningProduced(HANDLAGGNING_ID);
      verifyRegelResponseProduced();
   }

}
