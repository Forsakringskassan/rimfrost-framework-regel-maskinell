package se.fk.rimfrost.framework.regel.maskinell.helpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import se.fk.rimfrost.framework.regel.WireMockHandlaggning;
import java.util.HashMap;
import java.util.Map;

public class WireMockRegelMaskinell extends WireMockHandlaggning
{
   /**
    * Defines wiremock mappings for machine rules.
    * <p>
    * Note: Introducing this class creates a placeholder for adding mappings to all machine rules
    * (even if initially no such mappings have been identified).</p>
    *
    * @param server active WireMock server
    * @return property mappings
    */
   @SuppressWarnings("UnnecessaryLocalVariable")
   @Override
   protected Map<String, String> wiremockMapping(WireMockServer server)
   {
      Map<String, String> map = new HashMap<>(super.wiremockMapping(server));
      // Add more mappings if machine rules need to define extra mappings
      // map.put("something", "value");
      return map;
   }
}
