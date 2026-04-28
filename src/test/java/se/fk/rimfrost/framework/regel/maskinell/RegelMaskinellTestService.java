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

import java.util.Objects;
import static se.fk.rimfrost.framework.regel.logic.RegelUtils.createYrkandeWithUpdatedProduceradeResultat;
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

      var updatedYrkande = createYrkandeWithUpdatedProduceradeResultat(
            regelMaskinellRequest.handlaggning().yrkande(),
            createProduceradeResultatForTest());

      var uppgiftUpdate = ImmutableUppgift.builder().from(regelMaskinellRequest.uppgift())
            .uppgiftStatus("3")
            .build();

      var handlaggningUpdate = ImmutableHandlaggningUpdate.builder()
            .id(regelMaskinellRequest.handlaggning().id())
            .version(regelMaskinellRequest.handlaggning().version())
            .yrkande(updatedYrkande)
            .processInstansId(Objects.requireNonNull(regelMaskinellRequest.handlaggning().processInstansId()))
            .skapadTS(regelMaskinellRequest.handlaggning().skapadTS())
            .handlaggningspecifikationId(regelMaskinellRequest.handlaggning().handlaggningspecifikationId())
            .underlag(createUnderlagListForTest())
            .uppgift(uppgiftUpdate)
            .build();

      return ImmutableRegelMaskinellSuccessResult.builder()
            .handlaggningUpdate(handlaggningUpdate)
            .utfall(utfall)
            .build();
   }
}
