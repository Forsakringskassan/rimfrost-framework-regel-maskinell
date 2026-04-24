package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import se.fk.rimfrost.framework.regel.maskinell.base.AbstractRegelMaskinellSequenceTest;
import se.fk.rimfrost.framework.regel.maskinell.helpers.WireMockRegelMaskinell;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockRegelMaskinell.class)
})
public class RegelMaskinellSequenceTest extends AbstractRegelMaskinellSequenceTest
{
}
