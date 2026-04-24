package se.fk.rimfrost.framework.regel.maskinell.base;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static se.fk.rimfrost.framework.regel.RegelKafkaConnector.regelResponsesChannel;
import static se.fk.rimfrost.framework.regel.WireMockHandlaggning.waitForHandlaggningRequests;

@Disabled("Base test class - not executable")
public abstract class AbstractRegelMaskinellSequenceTest extends AbstractRegelMaskinellTest
{

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void smoke_test_maskinell_sequence(String handlaggningId)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningGetRequests = waitForHandlaggningRequests(handlaggningId, RequestMethod.GET, 1);
      Assertions.assertEquals(1, handlaggningGetRequests.size());
      var handlaggningPutRequests = waitForHandlaggningRequests(handlaggningId, RequestMethod.PUT, 1);
      Assertions.assertEquals(1, handlaggningPutRequests.size());
      var regelResponses = regelKafkaConnector.waitForMessages(regelResponsesChannel);
      Assertions.assertEquals(1, regelResponses.size());
   }
}
