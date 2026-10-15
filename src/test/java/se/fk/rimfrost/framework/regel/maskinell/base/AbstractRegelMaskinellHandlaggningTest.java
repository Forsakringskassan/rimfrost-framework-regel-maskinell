package se.fk.rimfrost.framework.regel.maskinell.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdata;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdatakopia;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdatakoppling;
import se.fk.rimfrost.framework.regel.maskinell.helpers.WireMockRegelMaskinell;

import java.util.List;

/**
 * Base test class for verifying Regel Maskinell handläggning flows.
 *
 * <p>This class contains common tests that assert how a
 * HandlaggningUpdate is produced and sent. It is intended to be
 * extended by downstream test classes that may customize expected data.</p>
 *
 * <p><b>Extension points:</b></p>
 * <ul>
 *   <li>{@link #createExpectedUnderlag()} – override to control expected underlag data</li>
 *   <li>{@link #createExpectedResultat()} – override to control expected resultat</li>
 * </ul>
 *
 * <p>By default, expected data is sourced from {@code RegelMaskinellTestData}, and used by framework tests.
 * Subclasses can override these methods to adapt assertions to actual rules.</p>
 *
 */
@Disabled("Base test class - not executable")
public abstract class AbstractRegelMaskinellHandlaggningTest extends AbstractRegelMaskinellTest
{
   /**
    * Provides the expected underlag entries on the uppgift, used in
    * {@code should_put_handlaggning_request_with_underlag}.
    *
    * <p>Subclasses may override this method to:</p>
    * <ul>
    *   <li>Change the expected entries</li>
    *   <li>Mix {@link Uppgiftsdatakopia} and {@link Uppgiftsdatakoppling} entries</li>
    *   <li>Adapt expectations for specific rules</li>
    * </ul>
    *
    * <p>The returned list is compared index-by-index with the actual request,
    * so ordering must match the system under test.</p>
    *
    * @return list of expected {@link Uppgiftsdata}
    */
   protected List<Uppgiftsdata> createExpectedUnderlag()
   {
      return RegelMaskinellTestData.createUnderlagListForTest();
   }

   /**
    * Provides the expected resultat entries on the uppgift, used in
    * {@code should_put_handlaggning_request_with_resultat}.
    *
    * <p>Subclasses may override this method to:</p>
    * <ul>
    *   <li>Change the expected results</li>
    *   <li>Customize informationsobjekt IDs and versions</li>
    *   <li>Adapt expectations for different rules</li>
    * </ul>
    *
    * <p>The returned list is compared index-by-index with the actual request,
    * so ordering must match the system under test.</p>
    *
    * @return list of expected {@link Uppgiftsdatakoppling}
    */
   protected List<Uppgiftsdatakoppling> createExpectedResultat()
   {
      return RegelMaskinellTestData.createResultatForTest();
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   @DisplayName("FRMASK-FR-02.1: GET-anrop skickas till Handläggning API för att hämta handläggningsdata")
   void should_create_initial_handlaggning_request(String handlaggningId)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var handlaggningRequests = WireMockRegelMaskinell.waitForHandlaggningRequests(handlaggningId, RequestMethod.GET, 1);
      Assertions.assertEquals(1, handlaggningRequests.size());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, YRKAT"
   })
   @DisplayName("FRMASK-FR-05.1: PUT-anrop uppdaterar handläggning med korrekt yrkandestatus")
   void should_put_handlaggning_request_with_yrkandestatus(String handlaggningId, String yrkandeStatus)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var handlaggningUpdate = WireMockRegelMaskinell.getLastPutHandlaggningUpdate(handlaggningId);
      Assertions.assertEquals(yrkandeStatus, handlaggningUpdate.getHandlaggning().getYrkande().getYrkandestatus());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   @DisplayName("FRMASK-FR-03.2, FRMASK-FR-05.1: Uppgiftsstatus är null i handläggningsuppdateringen")
   void should_put_handlaggning_request_with_null_uppgiftstatus(String handlaggningId)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var uppgift = WireMockRegelMaskinell.getUppgiftFromLastPutHandlaggning(handlaggningId);
      Assertions.assertNull(uppgift.getUppgiftStatus());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, a42ffaed-2f20-47e8-8499-f2f79ae2f45f"
   })
   @DisplayName("FRMASK-FR-03.1, FRMASK-FR-05.1: Uppgiftsspecifikations-ID från YAML-konfiguration inkluderas i handläggningsuppdateringen")
   void should_put_handlaggning_request_with_uppgiftspecifikation_id(String handlaggningId, String uppgiftspecifikationId)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);
      var uppgift = WireMockRegelMaskinell.getUppgiftFromLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(uppgiftspecifikationId, uppgift.getUppgiftspecifikation().getId().toString());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   @DisplayName("FRMASK-FR-05.1: PUT-anrop innehåller förväntade underlag på uppgiften med korrekt typ och innehåll")
   void should_put_handlaggning_request_with_underlag(String handlaggningId) throws JsonProcessingException
   {
      var expectedUnderlag = createExpectedUnderlag();

      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);

      var sentUnderlag = WireMockRegelMaskinell.getUppgiftFromLastPutHandlaggning(handlaggningId).getUnderlag();

      Assertions.assertEquals(expectedUnderlag.size(), sentUnderlag.size());

      // Assert each entry
      for (int i = 0; i < expectedUnderlag.size(); i++)
      {
         var expected = expectedUnderlag.get(i);
         var actual = sentUnderlag.get(i);
         if (expected instanceof Uppgiftsdatakopia)
         {
            var expectedKopia = (Uppgiftsdatakopia) expected;
            var actualKopia = Assertions.assertInstanceOf(
                  se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Uppgiftsdatakopia.class, actual);
            Assertions.assertEquals(expectedKopia.informationsobjekttyp(), actualKopia.getInformationsobjekttyp());
            Assertions.assertEquals(expectedKopia.data(), actualKopia.getData());
         }
         else if (expected instanceof Uppgiftsdatakoppling)
         {
            var expectedKoppling = (Uppgiftsdatakoppling) expected;
            var actualKoppling = Assertions.assertInstanceOf(
                  se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Uppgiftsdatakoppling.class, actual);
            Assertions.assertEquals(expectedKoppling.informationsobjektId(), actualKoppling.getInformationsobjektId());
            Assertions.assertEquals(expectedKoppling.informationsobjektversion(),
                  actualKoppling.getInformationsobjektversion());
         }
         else
         {
            Assertions.fail("Unexpected underlag type: " + expected.getClass().getName());
         }
      }
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   @DisplayName("FRMASK-FR-05.1: PUT-anrop innehåller förväntade resultat på uppgiften med korrekt informationsobjekt-ID och version")
   void should_put_handlaggning_request_with_resultat(String handlaggningId)
         throws JsonProcessingException
   {
      var expected = createExpectedResultat();

      regelKafkaConnector.sendRegelRequest(handlaggningId, responsesTopic);

      var actual = WireMockRegelMaskinell.getUppgiftFromLastPutHandlaggning(handlaggningId).getResultat();

      Assertions.assertEquals(expected.size(), actual.size());

      for (int i = 0; i < expected.size(); i++)
      {
         var exp = expected.get(i);
         var act = actual.get(i);

         Assertions.assertEquals(exp.informationsobjektId(), act.getInformationsobjektId());
         Assertions.assertEquals(exp.informationsobjektversion(), act.getInformationsobjektversion());
      }
   }
}
