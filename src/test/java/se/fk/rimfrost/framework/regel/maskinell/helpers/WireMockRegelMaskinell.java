package se.fk.rimfrost.framework.regel.maskinell.helpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import se.fk.rimfrost.framework.regel.WireMockHandlaggning;
import java.util.HashMap;
import java.util.Map;

public class WireMockRegelMaskinell extends WireMockHandlaggning
{
   /**
    * Extends the base handläggning WireMock mapping with machine-rule-specific
    * properties, currently {@code referensdata.api.base-url}.
    *
    * @param server active WireMock server
    * @return property mappings
    */
   @Override
   protected Map<String, String> wiremockMapping(WireMockServer server)
   {
      Map<String, String> map = new HashMap<>(super.wiremockMapping(server));
      map.put("referensdata.api.base-url", server.baseUrl());
      return map;
   }
}
