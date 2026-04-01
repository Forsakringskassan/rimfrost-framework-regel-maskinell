package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import jakarta.validation.constraints.NotNull;
import org.immutables.value.Value;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.Uppgift;

@Value.Immutable
public interface RegelMaskinellRequest
{

   @NotNull
   Handlaggning handlaggning();

   @NotNull
   Uppgift uppgift();

}
