package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.RegelKafkaConnector;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellErrorResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellSuccessResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RegelMaskinellFaultHandlingTest extends AbstractRegelMaskinellTest
{
   @InjectMock
   RegelMaskinellServiceInterface regelMaskinellService;

   @InjectMock
   HandlaggningAdapter handlaggningAdapter;

   @Test
   @DisplayName("FRMASK-FR-06.3, FRMASK-FR-07.2: Felresultat från processRegel() propageras som felsvar med korrekt felkod och felmeddelande")
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
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(regelErrorInformation, regelResponse.getData().getError());
   }

   @Test
   @DisplayName("FRMASK-FR-04.2, FRMASK-FR-06.3: Exception från processRegel() fångas och publiceras som ERROR med felkod RIMFROST_OTHER")
   public void should_send_error_response_on_process_regel_exception() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenThrow(new RuntimeException());

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_OTHER, regelResponse.getData().getError().getFelkod());
   }

   @Test
   @DisplayName("FRMASK-FR-02.3, FRMASK-FR-06.3: Fel vid hämtning av handläggning publiceras med felkod RIMFROST_HANDLAGGNING_READ_FAILURE")
   public void should_send_error_response_on_handlaggning_adapter_read_failure() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Test"));

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_HANDLAGGNING_READ_FAILURE, regelResponse.getData().getError().getFelkod());
   }

   @Test
   @DisplayName("FRMASK-FR-05.3, FRMASK-FR-06.3: Fel vid uppdatering av handläggning publiceras med felkod RIMFROST_HANDLAGGNING_WRITE_FAILURE")
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
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_HANDLAGGNING_WRITE_FAILURE, regelResponse.getData().getError().getFelkod());
   }

   @Test
   @DisplayName("FRMASK-FR-02.2: Ramverket gör retry vid misslyckat GET och lyckas om nästa försök returnerar handläggningsdata")
   public void should_retry_handlaggning_read_on_transient_failure() throws HandlaggningException
   {
      var regelSuccessResponse = ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(Mockito.mock(HandlaggningUpdate.class))
            .utfall(Utfall.JA)
            .build();

      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Transient failure"))
            .thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelSuccessResponse);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.JA, regelResponse.getData().getUtfall());
      Mockito.verify(handlaggningAdapter, Mockito.times(2)).readHandlaggning(Mockito.any());
   }

   @Test
   @DisplayName("FRMASK-FR-05.2: Ramverket gör retry vid misslyckat PUT och lyckas om nästa försök lyckas uppdatera handläggningsdata")
   public void should_retry_handlaggning_write_on_transient_failure() throws HandlaggningException
   {
      var regelSuccessResponse = ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(Mockito.mock(HandlaggningUpdate.class))
            .utfall(Utfall.JA)
            .build();

      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelSuccessResponse);
      Mockito.when(handlaggningAdapter.updateHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Transient failure"))
            .thenReturn(null);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.JA, regelResponse.getData().getUtfall());
      Mockito.verify(handlaggningAdapter, Mockito.times(2)).updateHandlaggning(Mockito.any());
   }

   @Test
   @DisplayName("FRMASK-FR-04.1: processRegel() anropas med handläggning, uppgift och processinstans-ID")
   public void should_invoke_process_regel_with_handlaggning_uppgift_and_process_instans_id() throws HandlaggningException
   {
      var regelSuccessResponse = ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(Mockito.mock(HandlaggningUpdate.class))
            .utfall(Utfall.JA)
            .build();

      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelSuccessResponse);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      regelKafkaConnector.waitForRegelResponse();

      var captor = ArgumentCaptor.forClass(RegelMaskinellRequest.class);
      Mockito.verify(regelMaskinellService).processRegel(captor.capture());
      var request = captor.getValue();
      assertNotNull(request.handlaggning());
      assertNotNull(request.uppgift());
      assertNotNull(request.processInstansId());
   }

   @Test
   @DisplayName("FRMASK-FR-04.2, FRMASK-FR-06.3: Oväntat exception fångas och publiceras som ERROR med felkod RIMFROST_OTHER")
   public void should_send_error_response_on_unexpected_exception() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenThrow(new RuntimeException());

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_OTHER, regelResponse.getData().getError().getFelkod());
      assertTrue(regelResponse.getData().getError().getFelmeddelande().matches("(?i)^.*unexpected error.*"));
   }

   @Test
   @DisplayName("FRMASK-FR-06.4: replyTo-fältet i inkommande Kafka-meddelanden styr vilket topic regelsvaret skickas till")
   public void should_route_response_to_reply_to_topic() throws HandlaggningException
   {
      var regelSuccessResponse = ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(Mockito.mock(HandlaggningUpdate.class))
            .utfall(Utfall.JA)
            .build();

      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelSuccessResponse);

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      var customReplyTopic = "custom-reply-topic";
      regelKafkaConnector.sendRegelRequest(handlaggningId, customReplyTopic);
      var messages = regelKafkaConnector.waitForMessages(RegelKafkaConnector.regelResponsesChannel);

      assertEquals(1, messages.size());
      var meta = messages.getFirst().getMetadata(OutgoingKafkaRecordMetadata.class);
      assertTrue(meta.isPresent(), "OutgoingKafkaRecordMetadata should be present on the response message");
      assertEquals(customReplyTopic, meta.get().getTopic());
   }
}
