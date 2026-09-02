package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.oul.model.Erbjudande;
import se.fk.rimfrost.framework.regel.ErbjudandeReferensdataTestService;
import se.fk.rimfrost.framework.regel.RegelKafkaConnector;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.error.RegelFelkod;
import se.fk.rimfrost.framework.regel.logic.KompletteringKontrollInterface;
import se.fk.rimfrost.framework.regel.logic.KompletteringOulHandler;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringUnderlag;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RegelMaskinellKompletteringTest extends AbstractRegelMaskinellTest
{
   @InjectMock
   KompletteringKontrollInterface kompletteringKontroll;

   @InjectMock
   KompletteringOulHandler kompletteringOulHandler;

   /**
    * Verifies that a non-empty komplettering list delegates to {@code KompletteringOulHandler.initiate()}
    * and suppresses the regel response.
    */
   @Test
   @DisplayName("FRMASK-FR-08.1: Icke-tom kompletteringslista delegerar till KompletteringOulHandler.initiate() och inget regelsvar skickas")
   void should_delegate_to_komplettering_oul_handler_and_skip_regel_when_komplettering_needed() throws Exception
   {
      Mockito.when(kompletteringKontroll.checkKomplettering(Mockito.any()))
            .thenReturn(List.of(ImmutableKompletteringUnderlag.builder()
                  .underlagTyp("TEST_TYP")
                  .beskrivning("Saknas")
                  .build()));

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);

      Mockito.verify(kompletteringOulHandler, Mockito.timeout(5000))
            .initiate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

      assertTrue(inMemoryConnector.sink(RegelKafkaConnector.regelResponsesChannel).received().isEmpty(),
            "Inget regelsvar ska skickas när en kompletteringsuppgift skapas");
   }

   /**
    * Verifies that an {@code OulException} from {@code initiate()} results in an error response
    * with felkod {@code RIMFROST_OTHER}.
    */
   @Test
   @DisplayName("FRMASK-FR-08.2: OulException från initiate() publiceras som ERROR med felkod RIMFROST_OTHER")
   void should_send_error_response_when_initiate_throws_oul_exception() throws Exception
   {
      Mockito.when(kompletteringKontroll.checkKomplettering(Mockito.any()))
            .thenReturn(List.of(ImmutableKompletteringUnderlag.builder()
                  .underlagTyp("TEST_TYP")
                  .beskrivning("Saknas")
                  .build()));
      Mockito.doThrow(new OulException(OulException.ErrorType.UNEXPECTED_ERROR, "OUL failure"))
            .when(kompletteringOulHandler).initiate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var regelResponse = regelKafkaConnector.waitForRegelResponse();

      assertEquals(Utfall.ERROR, regelResponse.getData().getUtfall());
      assertEquals(RegelFelkod.RIMFROST_OTHER, regelResponse.getData().getError().getFelkod());
   }

   /**
    * Verifies that the erbjudande name looked up via {@link
    * se.fk.rimfrost.framework.referensdata.ErbjudandeReferensdataInterface} is passed to
    * {@code KompletteringOulHandler.initiate()} when a komplettering task is created.
    */
   @Test
   @DisplayName("FRMASK-FR-03.3: Erbjudandenamn slås upp från referensdata och inkluderas i OUL-skapandeanropet")
   void should_include_erbjudande_from_referensdata_in_oul_create_request() throws Exception
   {
      Mockito.when(kompletteringKontroll.checkKomplettering(Mockito.any()))
            .thenReturn(List.of(ImmutableKompletteringUnderlag.builder()
                  .underlagTyp("TEST_TYP")
                  .beskrivning("Saknas")
                  .build()));

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);

      var erbjudandeCaptor = ArgumentCaptor.forClass(Erbjudande.class);
      Mockito.verify(kompletteringOulHandler, Mockito.timeout(5000))
            .initiate(Mockito.any(), Mockito.any(), Mockito.any(), erbjudandeCaptor.capture());

      assertEquals(ErbjudandeReferensdataTestService.DEFAULT_ERBJUDANDE_NAMN,
            erbjudandeCaptor.getValue().getNamn());
   }
}
