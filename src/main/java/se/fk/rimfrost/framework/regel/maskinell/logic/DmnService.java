package se.fk.rimfrost.framework.regel.maskinell.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.io.ResourceFactory;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Generic helper for evaluating DMN models from the classpath.
 * <p>
 * Each DMN resource is built once on first use and the resulting {@link DMNRuntime} is cached.
 * <p>
 * Usage:
 *
 * <pre>
 * DMNResult result = dmnService.evaluation("dmn/MyModel.dmn", "https://my/namespace", "MyModel")
 *       .set("input1", value1)
 *       .set("input2", value2)
 *       .evaluate();
 * </pre>
 */
@ApplicationScoped
public class DmnService
{

   private final Map<String, DMNRuntime> runtimes = new ConcurrentHashMap<>();

   /**
    * Starts a new evaluation of the given DMN model.
    *
    * @param resourcePath classpath path to the .dmn file, e.g. "dmn/RtfDecisionModel.dmn"
    * @param namespace the namespace attribute of the DMN definitions element
    * @param modelName the name attribute of the DMN definitions element
    */
   public DmnEvaluation evaluation(String resourcePath, String namespace, String modelName)
   {
      DMNRuntime runtime = getRuntime(resourcePath);
      DMNModel model = runtime.getModel(namespace, modelName);
      if (model == null)
      {
         throw new IllegalArgumentException(
               "DMN model not found. resource: " + resourcePath + ", namespace: " + namespace + ", modelName: " + modelName);
      }
      return new DmnEvaluation(runtime, model);
   }

   /**
    * Returns the (cached) DMN runtime for the given classpath resource.
    */
   private DMNRuntime getRuntime(String resourcePath)
   {
      return runtimes.computeIfAbsent(resourcePath, DmnService::buildRuntime);
   }

   private static DMNRuntime buildRuntime(String resourcePath)
   {
      KieServices ks = KieServices.Factory.get();

      // Give each resource its own release id so that building several DMN files
      // does not overwrite each other in the shared KieRepository
      ReleaseId releaseId = ks.newReleaseId("se.fk.rimfrost.dmn", toArtifactId(resourcePath), "1.0.0");

      KieFileSystem kfs = ks.newKieFileSystem();
      kfs.generateAndWritePomXML(releaseId);
      kfs.write(ResourceFactory.newClassPathResource(resourcePath));

      KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
      Results results = kb.getResults();
      if (results.hasMessages(Message.Level.ERROR))
      {
         throw new IllegalStateException("Error building DMN model " + resourcePath + ": " + results.getMessages());
      }

      KieContainer kContainer = ks.newKieContainer(releaseId);
      return KieRuntimeFactory.of(kContainer.getKieBase()).get(DMNRuntime.class);
   }

   private static String toArtifactId(String resourcePath)
   {
      return resourcePath.replaceAll("[^A-Za-z0-9_.-]", "_");
   }

   /**
    * A single evaluation of a DMN model. Set input variables with {@link #set(String, Object)}
    * and run with {@link #evaluate()} or {@link #evaluateDecision(String)}.
    */
   public static class DmnEvaluation
   {
      private final DMNRuntime runtime;
      private final DMNModel model;
      private final Map<String, Object> variables = new HashMap<>();

      private DmnEvaluation(DMNRuntime runtime, DMNModel model)
      {
         this.runtime = runtime;
         this.model = model;
      }

      public DmnEvaluation set(String name, Object value)
      {
         variables.put(name, value);
         return this;
      }

      public DmnEvaluation setAll(Map<String, Object> values)
      {
         variables.putAll(values);
         return this;
      }

      /**
       * Evaluates all decisions in the model.
       */
      public DMNResult evaluate()
      {
         var ctx = runtime.newContext();
         variables.forEach(ctx::set);
         return runtime.evaluateAll(model, ctx);
      }

      /**
       * Evaluates the model and returns the result of the named decision,
       * or empty if the decision is missing or evaluated to null.
       */
      public Optional<Object> evaluateDecision(String decisionName)
      {
         var decisionResult = evaluate().getDecisionResultByName(decisionName);
         return Optional.ofNullable(decisionResult).map(r -> r.getResult());
      }
   }
}
