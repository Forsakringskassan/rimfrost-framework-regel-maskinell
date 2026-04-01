package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import org.immutables.value.Value;
import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
import se.fk.rimfrost.framework.regel.Utfall;

@Value.Immutable
public interface RegelMaskinellResult {

    @NotNull
    HandlaggningUpdate handlaggningUpdate();

    @NotNull
    Utfall utfall();

}
