package se.fk.rimfrost.framework.regel.maskinell.logic;

import se.fk.rimfrost.framework.regel.logic.dto.ProcessRegelRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;

public interface RegelMaskinellServiceInterface
{

   RegelMaskinellResult processRegel(ProcessRegelRequest regelResult);
}
