package se.fk.rimfrost.framework.regel.maskinell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.fk.rimfrost.framework.regel.integration.config.YamlConfigLoader;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the YAML configuration loader emits meaningful error signals on startup
 * when the configuration file is missing or invalid.
 */
public class RegelConfigLoadingTest
{

   @Test
   @DisplayName("FRMASK-NFR-03.2: FileNotFoundException kastas med sökvägen i meddelandet när YAML-filen saknas")
   void loadFromFile_should_throw_with_path_in_message_when_file_not_found()
   {
      var missingPath = Path.of("nonexistent/config.yaml");
      var exception = assertThrows(FileNotFoundException.class,
            () -> YamlConfigLoader.loadFromFile(missingPath, RegelConfig.class));
      assertTrue(exception.getMessage().contains(missingPath.toString()),
            "Exception message should include the missing path to aid diagnosis");
   }

   @Test
   @DisplayName("FRMASK-NFR-03.2: RuntimeException kastas med sökvägen i meddelandet när YAML-filen är ogiltig")
   void loadFromFile_should_throw_with_path_in_message_when_yaml_is_malformed(@TempDir Path tempDir) throws IOException
   {
      var invalidYaml = tempDir.resolve("invalid-config.yaml");
      Files.writeString(invalidYaml, "invalid: yaml: [unclosed bracket");

      var exception = assertThrows(RuntimeException.class,
            () -> YamlConfigLoader.loadFromFile(invalidYaml, RegelConfig.class));
      assertTrue(exception.getMessage().contains(invalidYaml.toString()),
            "Exception message should include the file path to aid diagnosis");
   }

   @Test
   @DisplayName("FRMASK-NFR-03.2: IllegalStateException kastas med resursnamnet i meddelandet när config.yaml saknas på classpath")
   void loadFromClasspath_should_throw_with_resource_name_in_message_when_not_found()
   {
      var missingResource = "nonexistent-config.yaml";
      var exception = assertThrows(IllegalStateException.class,
            () -> YamlConfigLoader.loadFromClasspath(missingResource, RegelConfig.class));
      assertEquals("YAML config not found on classpath: " + missingResource, exception.getMessage());
   }
}
