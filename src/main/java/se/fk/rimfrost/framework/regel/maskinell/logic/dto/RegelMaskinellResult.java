package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import java.util.List;

import org.immutables.value.Value;

import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.logic.entity.ErsattningData;
import se.fk.rimfrost.framework.regel.logic.entity.Underlag;

@Value.Immutable
public interface RegelMaskinellResult
{

   @NotNull
   List<ErsattningData> ersattningar();

   @NotNull
   List<Underlag> underlag();

   @NotNull
   Utfall utfall();
}
