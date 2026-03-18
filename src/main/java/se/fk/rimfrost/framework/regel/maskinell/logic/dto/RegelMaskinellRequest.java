package se.fk.rimfrost.framework.regel.maskinell.logic.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

@Value.Immutable
public interface RegelMaskinellRequest
{

   UUID handlaggningId();

   UUID aktivitetId();

}
