package se.fk.rimfrost.framework.regel.maskinell;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kie.dmn.api.core.DMNDecisionResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.DmnService;
import se.fk.rimfrost.framework.regel.maskinell.logic.DmnService.DmnEvaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DmnServiceTest
{
   private static final String RESOURCE = "dmn/TestDecisionModel.dmn";
   private static final String NAMESPACE = "https://se.fk/github/testdmnservice";
   private static final String MODEL_NAME = "TestDecisionModel";
   private static final String DECISION = "utfall";

   private DmnService dmnService;

   @BeforeEach
   void setUp()
   {
      dmnService = new DmnService();
   }

   private DmnEvaluation evaluation()
   {
      return dmnService.evaluation(RESOURCE, NAMESPACE, MODEL_NAME);
   }

   @ParameterizedTest
   @CsvSource({
         "true,  true,  JA",
         "false, true,  UTREDNING",
         "true,  false, JA",
         "false, false, NEJ"
   })
   @DisplayName("evaluateDecision() returnerar utfallet från beslutstabellen")
   void evaluate_decision_should_return_decision_table_result(boolean folkbokford, boolean harAnstallning, String expected)
   {
      var result = evaluation()
            .set("folkbokford", folkbokford)
            .set("harAnstallning", harAnstallning)
            .evaluateDecision(DECISION);

      assertEquals(Optional.of(expected), result);
   }

   @Test
   @DisplayName("evaluate() returnerar ett DMNResult utan fel med beslutets resultat")
   void evaluate_should_return_dmn_result()
   {
      var result = evaluation()
            .set("folkbokford", false)
            .set("harAnstallning", true)
            .evaluate();

      assertFalse(result.hasErrors(), () -> result.getMessages().toString());
      var decisionResult = result.getDecisionResultByName(DECISION);
      assertEquals(DMNDecisionResult.DecisionEvaluationStatus.SUCCEEDED, decisionResult.getEvaluationStatus());
      assertEquals("UTREDNING", decisionResult.getResult());
   }

   @Test
   @DisplayName("setAll() sätter alla indata från en map")
   void set_all_should_set_all_inputs()
   {
      var result = evaluation()
            .setAll(Map.of("folkbokford", false, "harAnstallning", false))
            .evaluateDecision(DECISION);

      assertEquals(Optional.of("NEJ"), result);
   }

   @Test
   @DisplayName("evaluateDecision() returnerar empty när indata saknas")
   void evaluate_decision_should_return_empty_when_inputs_are_missing()
   {
      assertEquals(Optional.empty(), evaluation().evaluateDecision(DECISION));
   }

   @Test
   @DisplayName("evaluateDecision() returnerar empty för okänt beslut")
   void evaluate_decision_should_return_empty_for_unknown_decision()
   {
      var result = evaluation()
            .set("folkbokford", true)
            .set("harAnstallning", true)
            .evaluateDecision("finnsInte");

      assertEquals(Optional.empty(), result);
   }

   @Test
   @DisplayName("Indata delas inte mellan olika evalueringar av samma modell")
   void evaluations_should_not_share_inputs()
   {
      var first = evaluation()
            .set("folkbokford", true)
            .set("harAnstallning", true)
            .evaluateDecision(DECISION);
      var second = evaluation()
            .set("folkbokford", false)
            .set("harAnstallning", false)
            .evaluateDecision(DECISION);
      var third = evaluation().evaluateDecision(DECISION);

      assertEquals(Optional.of("JA"), first);
      assertEquals(Optional.of("NEJ"), second);
      assertEquals(Optional.empty(), third);
   }

   @Test
   @DisplayName("evaluation() kastar IllegalArgumentException vid fel namespace")
   void evaluation_should_throw_for_unknown_namespace()
   {
      assertThrows(IllegalArgumentException.class,
            () -> dmnService.evaluation(RESOURCE, "https://finns/inte", MODEL_NAME));
   }

   @Test
   @DisplayName("evaluation() kastar IllegalArgumentException vid fel modellnamn")
   void evaluation_should_throw_for_unknown_model_name()
   {
      assertThrows(IllegalArgumentException.class,
            () -> dmnService.evaluation(RESOURCE, NAMESPACE, "FinnsInte"));
   }

   @Test
   @DisplayName("evaluation() kastar exception när DMN-resursen inte finns")
   void evaluation_should_throw_for_missing_resource()
   {
      assertThrows(RuntimeException.class,
            () -> dmnService.evaluation("dmn/FinnsInte.dmn", NAMESPACE, MODEL_NAME));
   }
}
