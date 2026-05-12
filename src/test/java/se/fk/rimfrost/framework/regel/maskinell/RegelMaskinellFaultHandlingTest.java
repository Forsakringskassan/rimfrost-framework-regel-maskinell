package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellErrorResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellSuccessResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RegelMaskinellFaultHandlingTest extends AbstractRegelMaskinellTest
{
   @InjectMock
   RegelMaskinellServiceInterface regelMaskinellService;

   @InjectMock
   HandlaggningAdapter handlaggningAdapter;

   @Test
   public void should_send_error_response_on_error_result() throws HandlaggningException
   {
      var regelErrorInformation = new RegelErrorInformation();
      regelErrorInformation.setFelkod(RegelFelkod.RIMFROST_OTHER);
      regelErrorInformation.setFelmeddelande("Test");

      var regelErrorResponse = ImmutableRegelMaskinellErrorResult.builder()
            .regelErrorInformation(regelErrorInformation)
            .build();

      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelErrorResponse);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(regelErrorInformation, regelResponse.getData().getError());
   }

   @Test
   public void should_send_error_response_on_process_regel_exception() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenThrow(new RuntimeException());

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_OTHER, regelResponse.getData().getError().getFelkod());
   }

   @Test
   public void should_send_error_response_on_handlaggning_adapter_read_failure() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Test"));

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_HANDLAGGNING_READ_FAILURE, regelResponse.getData().getError().getFelkod());
   }

   @Test
   public void should_send_error_response_on_handlaggning_adapter_write_failure() throws HandlaggningException
   {
      var regelSuccessResponse = ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(Mockito.mock(HandlaggningUpdate.class))
            .utfall(Utfall.JA)
            .build();

      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(handlaggningAdapter.updateHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Test"));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelSuccessResponse);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_HANDLAGGNING_WRITE_FAILURE, regelResponse.getData().getError().getFelkod());
   }

   @Test
   public void should_send_error_response_on_unexpected_exception() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenThrow(new RuntimeException());

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_OTHER, regelResponse.getData().getError().getFelkod());
      assertTrue(regelResponse.getData().getError().getFelmeddelande().matches("(?i)^.*unexpected error.*"));
   }
}
