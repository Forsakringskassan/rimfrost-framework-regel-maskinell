package se.fk.rimfrost.framework.regel.maskinell;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.KundbehovsflodeResponse;
import se.fk.rimfrost.framework.regel.logic.ImmutableProcessRegelResponse;
import se.fk.rimfrost.framework.regel.logic.ProcessRegelResponse;
import se.fk.rimfrost.framework.regel.logic.RegelServiceInterface;
import java.util.ArrayList;

@ApplicationScoped
public class RegelMaskinellTestService implements RegelServiceInterface {

    @Override
    public ProcessRegelResponse processRegel(KundbehovsflodeResponse regelData) {
        return ImmutableProcessRegelResponse.builder()
                .ersattningar(new ArrayList<>())
                .underlag(new ArrayList<>())
                .build();    }

}
