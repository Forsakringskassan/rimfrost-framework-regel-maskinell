package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import org.immutables.value.Value;
import se.fk.rimfrost.framework.regel.RegelErrorInformation;

@Value.Immutable
public non-sealed interface RegelMaskinellErrorResult extends RegelMaskinellResult
{
    RegelErrorInformation regelErrorInformation();
}
