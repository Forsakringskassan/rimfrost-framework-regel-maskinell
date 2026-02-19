package se.fk.rimfrost.framework.regel.maskinell.logic;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableRegelResult;
import se.fk.rimfrost.framework.regel.logic.entity.RegelResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;

@ApplicationScoped
public class RegelMaskinellMapper
{

   public RegelResult toRegelResult(RegelMaskinellResult result)
   {

      return ImmutableRegelResult.builder()
            .uppgiftId(UUID.randomUUID())
            .skapadTs(OffsetDateTime.now())
            .planeradTs(OffsetDateTime.now())
            .utfordTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.AVSLUTAD)
            .fssaInformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .addAllErsattningar(result.ersattningar())
            .addAllUnderlag(result.underlag())
            .utfall(result.utfall())
            .build();
   }
}
