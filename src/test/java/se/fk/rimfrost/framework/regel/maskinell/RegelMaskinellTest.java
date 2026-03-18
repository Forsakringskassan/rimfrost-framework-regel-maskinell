package se.fk.rimfrost.framework.regel.maskinell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUnderlag;
import se.fk.rimfrost.framework.handlaggning.model.Underlag;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellResult;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import se.fk.rimfrost.framework.regel.test.RegelTest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutHandlaggningRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.UppgiftStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class RegelMaskinellTest extends RegelTest
{

   @InjectMock
   RegelMaskinellServiceInterface regelService;

   @Inject
   RegelRequestHandlerInterface regelMaskinellRequestHandler;

   //
   // test data
   //

   private RegelDataRequest testRegelDataRequest(String handlaggningId)
   {
      return ImmutableRegelDataRequest
            .builder()
            .id(UUID.fromString("99994567-89ab-4cde-9012-3456789abcde"))
            .handlaggningId(UUID.fromString(handlaggningId))
            .aktivitetId(UUID.fromString("0644fc72-f18b-4552-ba37-80df1ee6629c"))
            .kogitorootprocid("123456")
            .kogitorootprociid(UUID.fromString("77774567-89ab-4cde-9012-3456789abcde"))
            .kogitoparentprociid(UUID.fromString("88884567-89ab-4cde-9012-3456789abcde"))
            .kogitoprocid("234567")
            .kogitoprocinstanceid(UUID.fromString("66664567-89ab-4cde-9012-3456789abcde"))
            .kogitoprocist("345678")
            .kogitoprocversion("111")
            .type(regelRequestsChannel)
            .build();
   }

   private ArrayList<Underlag> getTestUnderlag()
   {
      return new ArrayList<>(List.of(
            ImmutableUnderlag.builder()
                  .typ("TEST_UNDERLAG_TYP_1")
                  .version(1)
                  .data("TEST_UNDERLAG_DATA_1")
                  .build(),
            ImmutableUnderlag.builder()
                  .typ("TEST_UNDERLAG_TYP_2")
                  .version(2)
                  .data("TEST_UNDERLAG_DATA_2")
                  .build()));
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, Ja, JA, Ja",
         "11111111-1111-1111-1111-111111113333,  Utredning, NEJ, Utredning",
         "11111111-1111-1111-1111-111111112222,  Nej, NEJ, Nej"
   })
   void TestRegelMaskinell(
         String handlaggningId,
         String expectedUtfall,
         String processRegelBeslutsutfall,
         String processRegelUtfall)
   {
      //
      // Clear out any previous requests
      //
      wiremockServer.resetRequests();
      inMemoryConnector.sink(regelResponsesChannel).clear();
      //
      // Setup mocking of processRegel
      //
      ArrayList<Underlag> testUnderlag = getTestUnderlag();
      Utfall testUtfall = Utfall.fromValue(processRegelUtfall);
      Mockito.when(regelService.processRegel(Mockito.any())).thenReturn(
            ImmutableRegelMaskinellResult.builder()
                  .underlag(testUnderlag)
                  .utfall(testUtfall)
                  .build());
      //
      // Trigger request to start workflow
      //
      var regelDataRequest = testRegelDataRequest(handlaggningId);
      regelMaskinellRequestHandler.handleRegelRequest(regelDataRequest);

      //
      // Verify number of handläggning requests
      //
      var handlaggningRequests = waitForWireMockRequest(wiremockServer,
            handlaggningEndpoint + handlaggningId, 2);
      assertEquals(2, handlaggningRequests.size());
      assertEquals(1, handlaggningRequests.stream().filter(r -> r.getMethod().equals(RequestMethod.GET)).count());
      assertEquals(1, handlaggningRequests.stream().filter(r -> r.getMethod().equals(RequestMethod.PUT)).count());

      //
      // verify content of handläggning PUT
      //
      var putRequest = handlaggningRequests
            .stream()
            .filter(r -> r.getMethod().equals(RequestMethod.PUT))
            .findFirst()
            .orElseThrow();
      PutHandlaggningRequest sentPutHandlaggningRequest;
      try
      {
         sentPutHandlaggningRequest = mapper.readValue(putRequest.getBodyAsString(), PutHandlaggningRequest.class);
      }
      catch (JsonProcessingException e)
      {
         throw new RuntimeException(e);
      }

      assertEquals(UppgiftStatus.AVSLUTAD, sentPutHandlaggningRequest.getHandlaggning().getUppgift().getUppgiftStatus());

      assertEquals("a42ffaed-2f20-47e8-8499-f2f79ae2f45f",
            sentPutHandlaggningRequest.getHandlaggning().getUppgift().getUppgiftspecifikation().getId().toString());

      // TODO ska vi skicka uppgiftbeskrivning?
      //      assertEquals("TEST Uppgift specifikation uppgiftbeskrivning",
      //            sentPutHandlaggningRequest.getUppgift().getUppgiftspecifikation().getUppgiftbeskrivning());

      var sentUnderlag = sentPutHandlaggningRequest.getHandlaggning().getUnderlag();
      assertEquals(2, sentUnderlag.size());
      //noinspection SequencedCollectionMethodCanBeUsed
      assertEquals(testUnderlag.get(0).typ(), sentUnderlag.get(0).getTyp());
      assertEquals(testUnderlag.get(0).version(), sentUnderlag.get(0).getVersion());
      assertEquals(testUnderlag.get(0).data(), sentUnderlag.get(0).getData());
      assertEquals(testUnderlag.get(1).typ(), sentUnderlag.get(1).getTyp());
      assertEquals(testUnderlag.get(1).version(), sentUnderlag.get(1).getVersion());
      assertEquals(testUnderlag.get(1).data(), sentUnderlag.get(1).getData());

      assertEquals(handlaggningId, sentPutHandlaggningRequest.getHandlaggning().getId().toString());

      //
      // Verify rule response
      //
      var messages = waitForMessages(regelResponsesChannel);
      assertEquals(1, messages.size());

      var message = messages.getFirst().getPayload();
      assertInstanceOf(RegelResponseMessagePayload.class, message);

      var regelResponseMessagePayload = (RegelResponseMessagePayload) message;
      assertEquals(handlaggningId, regelResponseMessagePayload.getData().getHandlaggningId());
      assertEquals(expectedUtfall, regelResponseMessagePayload.getData().getUtfall().getValue());
   }

}
