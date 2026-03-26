package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestdata.HANDLAGGNING_ID;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class RegelMaskinellHandlaggningTest extends AbstractRegelMaskinellTest
{

   @BeforeEach
   void setup()
   {
      resetState();
   }

   @Test
   void should_create_get_handlaggning()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyGetHandlaggningProduced(HANDLAGGNING_ID);
   }

   @Test
   void should_create_correct_put_handlaggning()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyPutHandlaggningContent(HANDLAGGNING_ID);
   }

   @Test
   void should_create_correct_put_handlaggning_underlag()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyPutHandlaggningUnderlag(HANDLAGGNING_ID);
   }

   @Test
   void should_create_correct_put_handlaggning_producerade_resultat()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyPutHandlaggningProduceradeResultat(HANDLAGGNING_ID);
   }

}
