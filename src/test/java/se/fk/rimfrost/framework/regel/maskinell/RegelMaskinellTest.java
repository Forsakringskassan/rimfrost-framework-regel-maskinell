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
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellResult;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import se.fk.rimfrost.framework.regel.test.RegelTest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutKundbehovsflodeRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.UppgiftStatus;

import java.util.ArrayList;
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

   private RegelDataRequest testRegelDataRequest(String kundbehovsflodeId)
   {
      return ImmutableRegelDataRequest
            .builder()
            .id(UUID.fromString("99994567-89ab-4cde-9012-3456789abcde"))
            .kundbehovsflodeId(UUID.fromString(kundbehovsflodeId))
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

   @ParameterizedTest
   @CsvSource(
   {
         "5367f6b8-cc4a-11f0-8de9-199901011234, Ja"
   // "5367f6b8-cc4a-11f0-8de9-199901013333,  Utredning",
   // "5367f6b8-cc4a-11f0-8de9-199901012222,  Ja",
   //"5367f6b8-cc4a-11f0-8de9-199901014444,  Nej"
   })
   void TestRegelMaskinell(String kundbehovsflodeId,
         String expectedUtfall)
   {
      //
      // Setup mocking of processRegel
      //
      Mockito.when(regelService.processRegel(Mockito.any())).thenReturn(
            ImmutableRegelMaskinellResult.builder()
                  .ersattningar(new ArrayList<>())
                  .underlag(new ArrayList<>())
                  .utfall(Utfall.JA)
                  .build());
      //
      // Trigger request to start workflow
      //
      var regelDataRequest = testRegelDataRequest(kundbehovsflodeId);
      regelMaskinellRequestHandler.handleRegelRequest(regelDataRequest);

      //
      // Verify number of kundbehovsflöde requests
      //
      var kundbehovsflodeRequests = waitForWireMockRequest(wiremockServer,
            kundbehovsflodeEndpoint + kundbehovsflodeId, 2);
      assertEquals(2, kundbehovsflodeRequests.size());
      assertEquals(1, kundbehovsflodeRequests.stream().filter(r -> r.getMethod().equals(RequestMethod.GET)).count());
      assertEquals(1, kundbehovsflodeRequests.stream().filter(r -> r.getMethod().equals(RequestMethod.PUT)).count());

      //
      // verify content of kundbehovsflöde PUT
      //
      var putRequest = kundbehovsflodeRequests
            .stream()
            .filter(r -> r.getMethod().equals(RequestMethod.PUT))
            .findFirst()
            .orElseThrow();
      PutKundbehovsflodeRequest sentPutKundbehovsflodeRequest;
      try
      {
         sentPutKundbehovsflodeRequest = mapper.readValue(putRequest.getBodyAsString(), PutKundbehovsflodeRequest.class);
      }
      catch (JsonProcessingException e)
      {
         throw new RuntimeException(e);
      }
      assertEquals(UppgiftStatus.AVSLUTAD, sentPutKundbehovsflodeRequest.getUppgift().getUppgiftStatus());
      assertEquals("TEST Uppgift specifikation namn",
            sentPutKundbehovsflodeRequest.getUppgift().getUppgiftspecifikation().getNamn());
      assertEquals("TEST Uppgift specifikation uppgiftbeskrivning",
            sentPutKundbehovsflodeRequest.getUppgift().getUppgiftspecifikation().getUppgiftbeskrivning());
      var sentUnderlag = sentPutKundbehovsflodeRequest.getUppgift().getUnderlag();
      assertEquals(0, sentUnderlag.size());
      assertEquals(kundbehovsflodeId, sentPutKundbehovsflodeRequest.getUppgift().getKundbehovsflodeId().toString());

      //
      // Verify rule response
      //
      var messages = waitForMessages(regelResponsesChannel);
      assertEquals(1, messages.size());

      var message = messages.getFirst().getPayload();
      assertInstanceOf(RegelResponseMessagePayload.class, message);

      var regelResponseMessagePayload = (RegelResponseMessagePayload) message;
      assertEquals(kundbehovsflodeId, regelResponseMessagePayload.getData().getKundbehovsflodeId());
      assertEquals(expectedUtfall, regelResponseMessagePayload.getData().getUtfall().getValue());
   }

}
