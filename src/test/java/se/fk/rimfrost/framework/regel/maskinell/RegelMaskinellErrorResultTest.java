package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.RegelFelkod;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellErrorResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class RegelMaskinellErrorResultTest extends AbstractRegelMaskinellTest
{
   @InjectMock
   RegelMaskinellServiceInterface regelMaskinellService;

   @Test
   public void should_send_error_response_on_error_result()
   {
      var regelErrorInformation = new RegelErrorInformation();
      regelErrorInformation.setFelkod(RegelFelkod.OTHER);
      regelErrorInformation.setFelmeddelande("Test");

      var regelErrorResponse = ImmutableRegelMaskinellErrorResult.builder()
            .regelErrorInformation(regelErrorInformation)
            .build();

      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelErrorResponse);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(regelErrorInformation, regelResponse.getData().getError());
   }
}
