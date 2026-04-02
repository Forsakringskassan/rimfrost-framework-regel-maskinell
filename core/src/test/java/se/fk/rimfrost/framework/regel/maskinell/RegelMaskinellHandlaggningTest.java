package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.UppgiftStatus;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus;
import java.util.UUID;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.HANDLAGGNING_ID;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestData.createUnderlagListForTest;

@SuppressWarnings("SameParameterValue")
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

   private void verifyPutHandlaggningProduceradeResultat(String handlaggningId)
   {
      var handlaggningUpdate = getLastPutHandlaggningUpdate(handlaggningId);
      var sentProduceradeResultat = handlaggningUpdate.getYrkande().getProduceradeResultat();
      Assertions.assertEquals(3, sentProduceradeResultat.size());
      Assertions.assertEquals(sentProduceradeResultat.get(0).getId(), UUID.fromString("66666666-6666-6666-6666-666666661234"));
      Assertions.assertEquals(sentProduceradeResultat.get(0).getYrkandestatus(),
            se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.UNDER_UTREDNING);
      Assertions.assertEquals(sentProduceradeResultat.get(1).getId(), UUID.fromString("d89ca33f-eeeb-48fa-850f-7b9d9b07cc87"));
      Assertions.assertEquals(sentProduceradeResultat.get(1).getYrkandestatus(),
            se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.YRKAT);
      Assertions.assertEquals(sentProduceradeResultat.get(2).getId(), UUID.fromString("66666666-6666-6666-6666-666667771234"));
      Assertions.assertEquals(sentProduceradeResultat.get(2).getYrkandestatus(),
            se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Yrkandestatus.YRKAT);
   }

   @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
   private void verifyPutHandlaggningUnderlag(String handlaggningId)
   {
      var handlaggningUpdate = getLastPutHandlaggningUpdate(handlaggningId);
      var expectedUnderlag = createUnderlagListForTest();
      var sentUnderlag = handlaggningUpdate.getUnderlag();
      Assertions.assertEquals(2, sentUnderlag.size());
      Assertions.assertEquals(expectedUnderlag.get(0).typ(), sentUnderlag.get(0).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(0).version(), sentUnderlag.get(0).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(0).data(), sentUnderlag.get(0).getData());
      Assertions.assertEquals(expectedUnderlag.get(1).typ(), sentUnderlag.get(1).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(1).version(), sentUnderlag.get(1).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(1).data(), sentUnderlag.get(1).getData());
   }

   private void verifyPutHandlaggningContent(String handlaggningId)
   {
      var handlaggningUpdate = getLastPutHandlaggningUpdate(handlaggningId);
      Assertions.assertEquals(Yrkandestatus.YRKAT, handlaggningUpdate.getYrkande().getYrkandestatus());
      Assertions.assertEquals(UppgiftStatus.AVSLUTAD, handlaggningUpdate.getUppgift().getUppgiftStatus());
      Assertions.assertEquals("a42ffaed-2f20-47e8-8499-f2f79ae2f45f",
            handlaggningUpdate.getUppgift().getUppgiftspecifikation().getId().toString());
   }
}
