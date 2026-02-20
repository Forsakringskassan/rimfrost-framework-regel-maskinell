package se.fk.rimfrost.framework.regel.maskinell.logic;

import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;

public interface RegelMaskinellServiceInterface
{
   RegelMaskinellResult processRegel(RegelMaskinellRequest regelResult);
}
