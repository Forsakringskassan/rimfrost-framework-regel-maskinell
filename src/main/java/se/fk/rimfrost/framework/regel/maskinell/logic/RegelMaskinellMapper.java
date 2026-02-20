package se.fk.rimfrost.framework.regel.maskinell.logic;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.KundbehovsflodeResponse;
import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableRegelResult;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableUppgiftData;
import se.fk.rimfrost.framework.regel.logic.entity.RegelResult;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableErsattning;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.ImmutableRegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellRequest;
import se.fk.rimfrost.framework.regel.maskinell.logic.dto.RegelMaskinellResult;

@ApplicationScoped
public class RegelMaskinellMapper
{

   public RegelResult toRegelResult(RegelMaskinellResult result)
   {

      var uppgiftData = ImmutableUppgiftData.builder()
            .uppgiftId(UUID.randomUUID())
            .skapadTs(OffsetDateTime.now())
            .planeradTs(OffsetDateTime.now())
            .utfordTs(OffsetDateTime.now())
            .uppgiftStatus(UppgiftStatus.AVSLUTAD)
            .fssaInformation(FSSAinformation.HANDLAGGNING_PAGAR)
            .build();

      return ImmutableRegelResult.builder()
            .uppgiftData(uppgiftData)
            .addAllErsattningar(result.ersattningar())
            .addAllUnderlag(result.underlag())
            .utfall(result.utfall())
            .build();
   }

   public RegelMaskinellRequest toRegelMaskinellRequest(KundbehovsflodeResponse kundbehovsflodeResponse)
   {
      return ImmutableRegelMaskinellRequest.builder()
            .kundbehovsflodeId(kundbehovsflodeResponse.kundbehovsflodeId())
            .personnummer(kundbehovsflodeResponse.personnummer())
            .formanstyp(kundbehovsflodeResponse.formanstyp())
            .ersattning(
                  kundbehovsflodeResponse.ersattning()
                        .stream()
                        .map(e -> ImmutableErsattning.builder()
                              .ersattningsId(e.ersattningsId())
                              .ersattningsTyp(e.ersattningsTyp())
                              .omfattningsProcent(e.omfattningsProcent())
                              .belopp(e.belopp())
                              .berakningsgrund(e.berakningsgrund())
                              .beslutsutfall(e.beslutsutfall())
                              .franOchMed(e.franOchMed())
                              .tillOchMed(e.tillOchMed())
                              .build())
                        .collect(Collectors.toList()))
            .build();
   }
}
