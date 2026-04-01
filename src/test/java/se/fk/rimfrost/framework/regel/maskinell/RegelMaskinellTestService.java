package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableHandlaggningUpdate;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;
import static se.fk.rimfrost.framework.regel.logic.RegelUtils.createYrkandeWithUpdatedProduceradeResultat;
import static se.fk.rimfrost.framework.regel.maskinell.RegelMaskinellTestdata.*;

@ApplicationScoped
@DefaultBean
public class RegelMaskinellTestService implements RegelMaskinellServiceInterface
{

   public static Utfall utfall = Utfall.JA;

   public static String handlaggningId = "11111111-1111-1111-1111-111111111234";

   @Override
   public RegelMaskinellResult processRegel(RegelMaskinellRequest regelMaskinellRequest)
   {

      var updatedYrkande = createYrkandeWithUpdatedProduceradeResultat(
            regelMaskinellRequest.handlaggning().yrkande(),
            createProduceradeResultatForTest());

      var handlaggningUpdate = ImmutableHandlaggningUpdate.builder()
            .id(regelMaskinellRequest.handlaggning().id())
            .version(regelMaskinellRequest.handlaggning().version())
            .yrkande(updatedYrkande)
            .processInstansId(regelMaskinellRequest.handlaggning().processInstansId())
            .skapadTS(regelMaskinellRequest.handlaggning().skapadTS())
            .handlaggningspecifikationId(regelMaskinellRequest.handlaggning().handlaggningspecifikationId())
            .underlag(createUnderlagListForTest())
            .uppgift(regelMaskinellRequest.uppgift())
            .build();

      return ImmutableRegelMaskinellResult.builder()
            .handlaggningUpdate(handlaggningUpdate)
            .utfall(utfall)
            .build();
   }
}
