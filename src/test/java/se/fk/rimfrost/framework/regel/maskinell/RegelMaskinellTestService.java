package se.fk.rimfrost.framework.regel.maskinell;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellService;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;
import java.util.ArrayList;

@ApplicationScoped
public class RegelMaskinellTestService extends RegelMaskinellService implements RegelMaskinellServiceInterface
{

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
