package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import org.immutables.value.Value;

import se.fk.rimfrost.framework.handlaggning.model.Yrkande;

@Value.Immutable
public interface RegelMaskinellRequest
{

   Yrkande yrkande();

}
