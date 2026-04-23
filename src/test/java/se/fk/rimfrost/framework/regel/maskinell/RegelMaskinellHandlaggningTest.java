package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import java.util.UUID;
import static se.fk.rimfrost.framework.regel.WireMockHandlaggning.getLastPutHandlaggning;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.HANDLAGGNING_ID;
import static se.fk.rimfrost.framework.regel.maskinell.TestData.createUnderlagListForTest;

@SuppressWarnings("SameParameterValue")
@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockRegelMaskinell.class)
})
public class RegelMaskinellHandlaggningTest extends AbstractRegelMaskinellTest
{

   @Test
   void should_create_get_handlaggning()
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyGetHandlaggningProduced(HANDLAGGNING_ID);
   }

   @Test
   void should_create_correct_put_handlaggning() throws Exception
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyPutHandlaggningContent(HANDLAGGNING_ID);
   }

   @Test
   void should_create_correct_put_handlaggning_underlag() throws Exception
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyPutHandlaggningUnderlag(HANDLAGGNING_ID);
   }

   @Test
   void should_create_correct_put_handlaggning_producerade_resultat() throws Exception
   {
      sendRegelRequest(HANDLAGGNING_ID);
      verifyPutHandlaggningProduceradeResultat(HANDLAGGNING_ID);
   }

   private void verifyPutHandlaggningProduceradeResultat(String handlaggningId) throws Exception
   {
      var handlaggningUpdate = getLastPutHandlaggning(handlaggningId);
      var sentProduceradeResultat = handlaggningUpdate.getHandlaggning().getYrkande().getProduceradeResultat();
      Assertions.assertEquals(3, sentProduceradeResultat.size());
      Assertions.assertEquals(UUID.fromString("66666666-6666-6666-6666-666666661234"), sentProduceradeResultat.get(0).getId());
      Assertions.assertEquals("UNDER_UTREDNING", sentProduceradeResultat.get(0).getYrkandestatus());
      Assertions.assertEquals(sentProduceradeResultat.get(1).getId(), UUID.fromString("d89ca33f-eeeb-48fa-850f-7b9d9b07cc87"));
      Assertions.assertEquals("YRKAT", sentProduceradeResultat.get(1).getYrkandestatus());
      Assertions.assertEquals(UUID.fromString("66666666-6666-6666-6666-666667771234"), sentProduceradeResultat.get(2).getId());
      Assertions.assertEquals("YRKAT", sentProduceradeResultat.get(2).getYrkandestatus());
   }

   @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
   private void verifyPutHandlaggningUnderlag(String handlaggningId) throws Exception
   {
      var handlaggningUpdate = getLastPutHandlaggning(handlaggningId);
      var expectedUnderlag = createUnderlagListForTest();
      var sentUnderlag = handlaggningUpdate.getHandlaggning().getUnderlag();
      Assertions.assertEquals(2, sentUnderlag.size());
      Assertions.assertEquals(expectedUnderlag.get(0).typ(), sentUnderlag.get(0).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(0).version(), sentUnderlag.get(0).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(0).data(), sentUnderlag.get(0).getData());
      Assertions.assertEquals(expectedUnderlag.get(1).typ(), sentUnderlag.get(1).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(1).version(), sentUnderlag.get(1).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(1).data(), sentUnderlag.get(1).getData());
   }

   private void verifyPutHandlaggningContent(String handlaggningId) throws Exception
   {
      var handlaggningUpdate = getLastPutHandlaggning(handlaggningId);
      var handlaggning = handlaggningUpdate.getHandlaggning();
      Assertions.assertEquals("YRKAT", handlaggning.getYrkande().getYrkandestatus());
      Assertions.assertEquals("AVSLUTAD", handlaggning.getUppgift().getUppgiftStatus());
      Assertions.assertEquals("a42ffaed-2f20-47e8-8499-f2f79ae2f45f",
            handlaggning.getUppgift().getUppgiftspecifikation().getId().toString());
   }
}
