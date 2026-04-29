package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellErrorResultTest;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellServiceInterface;

@QuarkusTest
public class RegelMaskinellErrorResultTest extends AbstractRegelMaskinellErrorResultTest
{

   @InjectMock
   RegelMaskinellServiceInterface regelMaskinellService;

   @Override
   protected RegelMaskinellServiceInterface getRegelMaskinellService()
   {
      return regelMaskinellService;
   }

}
