package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.model.CreateOperativUppgiftRequest;
import se.fk.rimfrost.framework.regel.RegelKafkaConnector;
import se.fk.rimfrost.framework.regel.logic.KompletteringKontrollInterface;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringUnderlag;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellTest;
import se.fk.rimfrost.framework.regel.storage.ProcessTopicInfoStorage;
import se.fk.rimfrost.framework.regel.storage.entity.ProcessTopicInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RegelMaskinellKompletteringTest extends AbstractRegelMaskinellTest
{
   @InjectMock
   KompletteringKontrollInterface kompletteringKontroll;

   @InjectMock
   OulAdapter oulAdapter;

   @InjectMock
   ProcessTopicInfoStorage processTopicInfoStorage;

   @Test
   @DisplayName("Icke-tom komplettering: skapar operativ uppgift och skickar inget regelsvar")
   void should_create_operativ_uppgift_and_skip_regel_when_komplettering_needed() throws Exception
   {
      Mockito.when(kompletteringKontroll.checkKomplettering(Mockito.any()))
            .thenReturn(List.of(ImmutableKompletteringUnderlag.builder()
                  .underlagTyp("TEST_TYP")
                  .beskrivning("Saknas")
                  .build()));

      var handlaggningId = "11111111-1111-1111-1111-111111111234";
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);

      var processTopicInfoCaptor = ArgumentCaptor.forClass(ProcessTopicInfo.class);
      Mockito.verify(processTopicInfoStorage, Mockito.timeout(5000))
            .setProcessTopicInfo(Mockito.any(), processTopicInfoCaptor.capture());
      assertEquals(responsesTopic, processTopicInfoCaptor.getValue().replyTopic());

      var oulRequestCaptor = ArgumentCaptor.forClass(CreateOperativUppgiftRequest.class);
      Mockito.verify(oulAdapter).createOperativUppgift(oulRequestCaptor.capture());
      var oulRequest = oulRequestCaptor.getValue();
      assertEquals("TEST Uppgift specifikation namn", oulRequest.getRegel());
      assertEquals("TEST Uppgift specifikation uppgiftbeskrivning", oulRequest.getBeskrivning());
      assertEquals("/regel/framework-maskinell-test", oulRequest.getUrl());
      assertEquals("33333333-3333-3333-3333-333333331234", oulRequest.getErbjudande().getId());
      assertEquals("Test erbjudande", oulRequest.getErbjudande().getNamn());

      assertTrue(inMemoryConnector.sink(RegelKafkaConnector.regelResponsesChannel).received().isEmpty(),
            "Inget regelsvar ska skickas när en kompletteringsuppgift skapas");
   }
}
