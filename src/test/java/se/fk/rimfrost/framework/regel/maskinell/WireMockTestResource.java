package se.fk.rimfrost.framework.regel.maskinell;

import se.fk.rimfrost.framework.regel.test.AbstractWireMockTestResource;
import java.util.HashMap;
import java.util.Map;

public class WireMockTestResource extends AbstractWireMockTestResource
{

   @Override
   protected Map<String, String> getProperties()
   {

      Map<String, String> properties = new HashMap<>();
      properties.put("handlaggning.api.base-url", getWireMockServer().baseUrl());

      return properties;
   }
}
