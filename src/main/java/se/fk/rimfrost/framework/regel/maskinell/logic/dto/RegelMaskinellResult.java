package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import java.util.List;
import org.immutables.value.Value;
import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.framework.handlaggning.model.ProduceratResultat;
import se.fk.rimfrost.framework.handlaggning.model.Underlag;
import se.fk.rimfrost.framework.regel.Utfall;

@Value.Immutable
public interface RegelMaskinellResult
{

   @NotNull
   List<Underlag> underlag();

   @NotNull
   Utfall utfall();

   List<ProduceratResultat> produceradeResultat();
}
