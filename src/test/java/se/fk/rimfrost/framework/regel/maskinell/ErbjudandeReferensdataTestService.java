package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.referensdata.ErbjudandeReferensdataInterface;

@SuppressWarnings("unused")
@ApplicationScoped
@DefaultBean
public class ErbjudandeReferensdataTestService implements ErbjudandeReferensdataInterface
{
   @Override
   public String getErbjudandeNamn(String id)
   {
      return "Test erbjudande";
   }
}
