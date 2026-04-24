package se.fk.rimfrost.framework.regel.maskinell.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.regel.maskinell.helpers.WireMockRegelMaskinell;
import java.util.UUID;
import static se.fk.rimfrost.framework.regel.maskinell.base.RegelMaskinellTestData.createUnderlagListForTest;

@SuppressWarnings("SpellCheckingInspection")
@Disabled("Base test class - not executable")
public abstract class AbstractRegelMaskinellHandlaggningTest extends AbstractRegelMaskinellTest
{

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void should_create_initial_handlaggning_request(String handlaggningId)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningRequests = WireMockRegelMaskinell.waitForHandlaggningRequests(handlaggningId, RequestMethod.GET, 1);
      Assertions.assertEquals(1, handlaggningRequests.size());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, YRKAT"
   })
   void should_put_handlaggning_request_with_yrkandestatus(String handlaggningId, String yrkandeStatus)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(yrkandeStatus, handlaggningPutRequest.getHandlaggning().getYrkande().getYrkandestatus());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, AVSLUTAD"
   })
   void should_put_handlaggning_request_with_uppgiftstatus(String handlaggningId, String uppgiftStatus)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(uppgiftStatus, handlaggningPutRequest.getHandlaggning().getUppgift().getUppgiftStatus());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, a42ffaed-2f20-47e8-8499-f2f79ae2f45f"
   })
   void should_put_handlaggning_request_with_uppgiftspecifikation_id(String handlaggningId, String uppgiftspecifikationId)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(uppgiftspecifikationId,
            handlaggningPutRequest.getHandlaggning().getUppgift().getUppgiftspecifikation().getId().toString());
   }

   @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void should_put_handlaggning_request_with_underlag(String handlaggningId) throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      var expectedUnderlag = createUnderlagListForTest();
      var sentUnderlag = handlaggningPutRequest.getHandlaggning().getUnderlag();
      Assertions.assertEquals(2, sentUnderlag.size());
      Assertions.assertEquals(expectedUnderlag.get(0).typ(), sentUnderlag.get(0).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(0).version(), sentUnderlag.get(0).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(0).data(), sentUnderlag.get(0).getData());
      Assertions.assertEquals(expectedUnderlag.get(1).typ(), sentUnderlag.get(1).getTyp());
      Assertions.assertEquals(expectedUnderlag.get(1).version(), sentUnderlag.get(1).getVersion());
      Assertions.assertEquals(expectedUnderlag.get(1).data(), sentUnderlag.get(1).getData());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void should_put_handlaggning_request_with_producerade_resultat(String handlaggningId) throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      var sentProduceradeResultat = handlaggningPutRequest.getHandlaggning().getYrkande().getProduceradeResultat();
      Assertions.assertEquals(3, sentProduceradeResultat.size());
      Assertions.assertEquals(UUID.fromString("66666666-6666-6666-6666-666666661234"), sentProduceradeResultat.get(0).getId());
      Assertions.assertEquals("UNDER_UTREDNING", sentProduceradeResultat.get(0).getYrkandestatus());
      Assertions.assertEquals(sentProduceradeResultat.get(1).getId(), UUID.fromString("d89ca33f-eeeb-48fa-850f-7b9d9b07cc87"));
      Assertions.assertEquals("YRKAT", sentProduceradeResultat.get(1).getYrkandestatus());
      Assertions.assertEquals(UUID.fromString("66666666-6666-6666-6666-666667771234"), sentProduceradeResultat.get(2).getId());
      Assertions.assertEquals("YRKAT", sentProduceradeResultat.get(2).getYrkandestatus());
   }
}
