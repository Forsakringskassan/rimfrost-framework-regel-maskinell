package se.fk.rimfrost.framework.regel.maskinell.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.handlaggning.model.Underlag;
import se.fk.rimfrost.framework.regel.maskinell.helpers.WireMockRegelMaskinell;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.ProduceratResultat;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutHandlaggningRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Base test class for verifying Regel Maskinell handläggning flows.
 *
 * <p>This class contains common tests that assert how a
 * PutHandlaggningRequest is produced and sent. It is intended to be
 * extended by downstream test classes that may customize expected data.</p>
 *
 * <p><b>Extension points:</b></p>
 * <ul>
 *   <li>{@link #createExpectedUnderlag()} – override to control expected underlag data</li>
 *   <li>{@link #createExpectedProduceradeResultat()} – override to control expected producerade resultat</li>
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
    * Provides the expected Underlag entries used in
    * {@code should_put_handlaggning_request_with_underlag}.
    *
    * <p>Subclasses may override this method to:</p>
    * <ul>
    *   <li>Change the expected entries</li>
    *   <li>Modify content (typ, version, data)</li>
    *   <li>Adapt expectations for specific rules</li>
    * </ul>
    *
    * <p>The returned list is compared index-by-index with the actual response,
    * so ordering must match the system under test.</p>
    *
    * @return list of expected {@link Underlag}
    */
   protected ArrayList<Underlag> createExpectedUnderlag()
   {
      return RegelMaskinellTestData.createUnderlagListForTest();
   }

   /**
    * Provides the expected ProduceratResultat entries used in
    * {@code should_put_handlaggning_request_with_producerade_resultat}.
    *
    * <p>Subclasses may override this method to:</p>
    * <ul>
    *   <li>Change the expected results</li>
    *   <li>Customize IDs and yrkandestatus values</li>
    *   <li>Adapt expectations for different rules</li>
    * </ul>
    *
    * <p>The returned list is compared index-by-index with the actual response,
    * so ordering must match the system under test.</p>
    *
    * @return list of expected {@link ProduceratResultat}
    */
   protected List<ProduceratResultat> createExpectedProduceradeResultat()
   {
      return RegelMaskinellTestData.createExpectedProduceradeResultat();
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void should_create_initial_handlaggning_request(String handlaggningId)
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningRequests = WireMockRegelMaskinell.waitForHandlaggningRequests(handlaggningId, RequestMethod.GET, 1);
      Assertions.assertEquals(1, handlaggningRequests.size());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, YRKAT"
   })
   void should_put_handlaggning_request_with_yrkandestatus(String handlaggningId, String yrkandeStatus)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(yrkandeStatus, handlaggningPutRequest.getHandlaggning().getYrkande().getYrkandestatus());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, 3"
   })
   void should_put_handlaggning_request_with_uppgiftstatus(String handlaggningId, String uppgiftStatus)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(uppgiftStatus, handlaggningPutRequest.getHandlaggning().getUppgift().getUppgiftStatus());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234, a42ffaed-2f20-47e8-8499-f2f79ae2f45f"
   })
   void should_put_handlaggning_request_with_uppgiftspecifikation_id(String handlaggningId, String uppgiftspecifikationId)
         throws JsonProcessingException
   {
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      Assertions.assertEquals(uppgiftspecifikationId,
            handlaggningPutRequest.getHandlaggning().getUppgift().getUppgiftspecifikation().getId().toString());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void should_put_handlaggning_request_with_underlag(String handlaggningId) throws JsonProcessingException
   {
      var expectedUnderlag = createExpectedUnderlag();
      regelKafkaConnector.sendRegelRequest(handlaggningId);
      var handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);
      var sentUnderlag = handlaggningPutRequest.getHandlaggning().getUnderlag();
      Assertions.assertEquals(expectedUnderlag.size(), sentUnderlag.size());
      // Assert each entry
      for (int i = 0; i < expectedUnderlag.size(); i++)
      {
         Underlag expected = expectedUnderlag.get(i);
         var actual = sentUnderlag.get(i);

         Assertions.assertEquals(expected.typ(), actual.getTyp());
         Assertions.assertEquals(expected.version(), actual.getVersion());
         Assertions.assertEquals(expected.data(), actual.getData());
      }
   }

   @ParameterizedTest
   @CsvSource(
   {
         "11111111-1111-1111-1111-111111111234"
   })
   void should_put_handlaggning_request_with_producerade_resultat(String handlaggningId)
         throws JsonProcessingException
   {

      List<ProduceratResultat> expected = createExpectedProduceradeResultat();

      this.regelKafkaConnector.sendRegelRequest(handlaggningId);

      PutHandlaggningRequest handlaggningPutRequest = WireMockRegelMaskinell.getLastPutHandlaggning(handlaggningId);

      List<ProduceratResultat> actual = handlaggningPutRequest.getHandlaggning()
            .getYrkande()
            .getProduceradeResultat();

      Assertions.assertEquals(expected.size(), actual.size());

      for (int i = 0; i < expected.size(); i++)
      {
         ProduceratResultat exp = expected.get(i);
         ProduceratResultat act = actual.get(i);

         Assertions.assertEquals(exp.getId(), act.getId());
         Assertions.assertEquals(exp.getYrkandestatus(), act.getYrkandestatus());
      }
   }
}
