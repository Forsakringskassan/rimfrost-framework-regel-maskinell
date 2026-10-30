package se.fk.rimfrost.framework.regel.maskinell.base;

import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgiftsdatakopia;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgiftsdatakoppling;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdata;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdatakoppling;
import java.util.ArrayList;
import java.util.List;

public class RegelMaskinellTestData
{

   public static ArrayList<Uppgiftsdata> createUnderlagListForTest()
   {
      return new ArrayList<>(List.of(
            ImmutableUppgiftsdatakopia.builder()
                  .informationsobjekttyp("TEST_UNDERLAG_TYP_1")
                  .data("TEST_UNDERLAG_DATA_1")
                  .build(),
            ImmutableUppgiftsdatakoppling.builder()
                  .informationsobjektId("66666666-6666-6666-6666-666666665555")
                  .informationsobjektversion(2)
                  .build()));
   }

   public static ArrayList<Uppgiftsdatakoppling> createResultatForTest()
   {
      return new ArrayList<>(List.of(
            ImmutableUppgiftsdatakoppling.builder()
                  .informationsobjektId("66666666-6666-6666-6666-666666661234")
                  .informationsobjektversion(1)
                  .build(),
            ImmutableUppgiftsdatakoppling.builder()
                  .informationsobjektId("66666666-6666-6666-6666-666667771234")
                  .informationsobjektversion(1)
                  .build()));
   }
}
