package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import java.util.UUID;

import org.immutables.value.Value;

import se.fk.rimfrost.framework.handlaggning.model.Yrkande;

@Value.Immutable
public interface RegelMaskinellRequest
{

   UUID handlaggningId();

   UUID aktivitetId();

   Yrkande yrkande();

}
