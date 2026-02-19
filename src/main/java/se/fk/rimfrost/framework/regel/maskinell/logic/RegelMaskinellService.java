package se.fk.rimfrost.framework.regel.maskinell.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;
import java.util.ArrayList;

@ApplicationScoped
public class RegelMaskinellService implements RegelMaskinellServiceInterface
{

   //
   // default implementation is required in order to build & test the base implementation
   //
   @Override
   public RegelMaskinellResult processRegel(RegelMaskinellRequest regelResult)
   {
      return ImmutableRegelMaskinellResult.builder()
            .ersattningar(new ArrayList<>())
            .underlag(new ArrayList<>())
            .utfall(Utfall.JA)
            .build();
   }
}
