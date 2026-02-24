package se.fk.rimfrost.framework.regel.maskinell;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import se.fk.rimfrost.framework.regel.maskinell.logic.RegelMaskinellRequestHandler;

@Alternative
@Priority(1)
@ApplicationScoped
public class RegelMaskinellTestRequestHandler
      extends RegelMaskinellRequestHandler
{
}
