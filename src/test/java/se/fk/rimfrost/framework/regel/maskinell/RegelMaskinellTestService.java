package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestdata.createProduceradeResultatForTest;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestdata.createUnderlagListForTest;

@ApplicationScoped
@DefaultBean
public class RegelMaskinellTestService implements RegelMaskinellServiceInterface
{

   public static Utfall utfall = Utfall.JA;

   @Override
   public RegelMaskinellResult processRegel(RegelMaskinellRequest regelMaskinellRequest)
   {
      return ImmutableRegelMaskinellResult.builder()
            .underlag(createUnderlagListForTest())
            .produceradeResultat(createProduceradeResultatForTest())
            .utfall(utfall)
            .build();
   }
}
