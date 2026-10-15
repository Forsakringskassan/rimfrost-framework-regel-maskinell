package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableHandlaggningUpdate;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgift;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellSuccessResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;

import static se.fk.rimfrost.framework.regel.maskinell.base.RegelMaskinellTestData.*;

@SuppressWarnings("unused")
@ApplicationScoped
@DefaultBean
public class RegelMaskinellTestService implements RegelMaskinellServiceInterface
{

   public static Utfall utfall = Utfall.JA;

   @Override
   public RegelMaskinellResult processRegel(RegelMaskinellRequest regelMaskinellRequest)
   {
      var uppgift = ImmutableUppgift.builder()
            .from(regelMaskinellRequest.uppgift())
            .underlag(createUnderlagListForTest())
            .resultat(createResultatForTest())
            .build();

      var handlaggningUpdate = ImmutableHandlaggningUpdate.builder()
            .handlaggning(regelMaskinellRequest.handlaggning())
            .uppgift(uppgift)
            .build();

      return ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(handlaggningUpdate)
            .utfall(utfall)
            .build();
   }
}
