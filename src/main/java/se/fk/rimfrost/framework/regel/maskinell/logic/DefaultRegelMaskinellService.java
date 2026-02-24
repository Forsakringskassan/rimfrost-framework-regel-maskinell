package se.fk.rimfrost.framework.regel.maskinell.logic;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;

@ApplicationScoped
@DefaultBean
public class DefaultRegelMaskinellService implements RegelMaskinellServiceInterface
{

   //
   // default implementation is required in order to build & test the base implementation
   //
   @Override
   public RegelMaskinellResult processRegel(RegelMaskinellRequest regelMaskinellRequest)
   {
      throw new IllegalStateException("processRegel has to be implemented by rule!");
   }
}
