package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelResponseException;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellRequestHandler;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellErrorResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellSuccessResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RegelMaskinellKafkaFaultHandlingTest extends AbstractRegelMaskinellTest
{
   private static final String REGEL_RESPONSE_SEND_FAILURE_LOG = "Failed to send regel response";

   private static final String REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG = "Failed to send regel error response";

   @InjectMock
   RegelKafkaProducer regelKafkaProducer;

   @InjectMock
   RegelMaskinellServiceInterface regelMaskinellService;

   @InjectMock
   HandlaggningAdapter handlaggningAdapter;

   @Inject
   RegelMaskinellRequestHandler regelMaskinellRequestHandler;

   private final Logger handlerLogger = Logger.getLogger(RegelMaskinellRequestHandler.class.getName());

   private final List<LogRecord> logRecords = new CopyOnWriteArrayList<>();

   private final Handler logCaptor = new Handler()
   {
      @Override
      public void publish(LogRecord logRecord)
      {
         logRecords.add(logRecord);
      }

      @Override
      public void flush()
      {
      }

      @Override
      public void close()
      {
      }
   };

   @BeforeEach
   void setUp() throws HandlaggningException
   {
      logRecords.clear();
      handlerLogger.addHandler(logCaptor);
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenReturn(Mockito.mock(Handlaggning.class));
   }

   @AfterEach
   void tearDown()
   {
      handlerLogger.removeHandler(logCaptor);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av lyckat regelsvar loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_success_response_send_fails() throws HandlaggningException
   {
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(successResult());
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      Mockito.verify(handlaggningAdapter).updateHandlaggning(Mockito.any());
      assertSendFailureLoggedWithoutResend(REGEL_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Oväntat exception vid sändning av regelsvar loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_response_send_throws_unexpected_exception()
   {
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(successResult());
      failKafkaSendsWith(new IllegalStateException("Other failure"));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av felsvar för felresultat loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_error_result_response_send_fails()
   {
      var regelErrorInformation = new RegelErrorInformation();
      regelErrorInformation.setFelkod(RegelFelkod.RIMFROST_OTHER);
      regelErrorInformation.setFelmeddelande("Test");

      var regelErrorResponse = ImmutableRegelMaskinellErrorResult.builder()
            .regelErrorInformation(regelErrorInformation)
            .build();

      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(regelErrorResponse);
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av felsvar efter misslyckad hämtning av handläggning loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_read_failure_response_send_fails() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Test"));
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av felsvar efter exception från processRegel() loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_process_regel_failure_response_send_fails()
   {
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenThrow(new RuntimeException());
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av felsvar efter konflikt vid uppdatering av handläggning loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_write_conflict_response_send_fails() throws HandlaggningException
   {
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(successResult());
      Mockito.when(handlaggningAdapter.updateHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.CONFLICT, "Test"));
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av felsvar efter misslyckad uppdatering av handläggning loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_write_failure_response_send_fails() throws HandlaggningException
   {
      Mockito.when(regelMaskinellService.processRegel(Mockito.any())).thenReturn(successResult());
      Mockito.when(handlaggningAdapter.updateHandlaggning(Mockito.any()))
            .thenThrow(new HandlaggningException(HandlaggningException.ErrorType.UNEXPECTED_ERROR, "Test"));
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG);
   }

   @Test
   @DisplayName("FRMASK-FR-08.1: Fel vid sändning av felsvar efter oväntat fel loggas utan nytt regelsvar eller propagering")
   public void should_log_without_resend_when_unexpected_error_response_send_fails() throws HandlaggningException
   {
      Mockito.when(handlaggningAdapter.readHandlaggning(Mockito.any())).thenThrow(new RuntimeException());
      failKafkaSendsWith(new RegelResponseException("Kafka send failure", new RuntimeException()));

      assertDoesNotThrow(() -> regelMaskinellRequestHandler.handleRegelRequest(createRegelDataRequest()));
      assertSendFailureLoggedWithoutResend(REGEL_ERROR_RESPONSE_SEND_FAILURE_LOG);
   }

   private void assertSendFailureLoggedWithoutResend(String expectedLogPrefix)
   {
      Mockito.verify(regelKafkaProducer, Mockito.times(1)).sendRegelResponse(Mockito.any());
      assertTrue(logRecords.stream().anyMatch(r -> r.getLevel().intValue() >= Level.SEVERE.intValue()
            && r.getMessage().startsWith(expectedLogPrefix)));
   }

   private void failKafkaSendsWith(RuntimeException e)
   {
      Mockito.doThrow(e).when(regelKafkaProducer).sendRegelResponse(Mockito.any());
   }

   private ImmutableRegelMaskinellSuccessResult successResult()
   {
      return ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(Mockito.mock(HandlaggningUpdate.class))
            .utfall(Utfall.JA)
            .build();
   }

   private RegelDataRequest createRegelDataRequest()
   {
      return ImmutableRegelDataRequest.builder()
            .id(UUID.randomUUID())
            .handlaggningId(UUID.randomUUID())
            .aktivitetId(UUID.randomUUID())
            .kogitorootprocid(UUID.randomUUID().toString())
            .kogitorootprociid(UUID.randomUUID())
            .kogitoparentprociid(UUID.randomUUID())
            .kogitoprocid(UUID.randomUUID().toString())
            .kogitoprocinstanceid(UUID.randomUUID())
            .kogitoprocist(UUID.randomUUID().toString())
            .kogitoprocversion(UUID.randomUUID().toString())
            .type(UUID.randomUUID().toString())
            .build();
   }
}
