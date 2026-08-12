package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.Result;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.RetriesExhaustedException;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry.RetryUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class RetryUtilTest
{
   @Test
   @DisplayName("FRMASK-NFR-01.1: Retry-mekanismen returnerar värdet direkt vid lyckat första anrop")
   void get_with_retries_should_return_supplier_value() throws RetriesExhaustedException
   {
      assertEquals(5, RetryUtil.getWithRetries(() -> Result.of(5), List.of(5)));
   }

   @Test
   @DisplayName("FRMASK-NFR-01.1, FRMASK-NFR-01.2: Supplier anropas igen efter konfigurerat retry-intervall vid misslyckat anrop")
   void get_with_retries_should_call_supplier_again_after_delay() throws RetriesExhaustedException
   {
      var counter = new Counter();
      var val = RetryUtil.getWithRetries(() -> {
         var count = counter.getCount();
         counter.increment();

         if (count < 1)
         {
            return Result.empty();
         }
         else
         {
            return Result.of(3);
         }
      }, List.of(1));
      assertEquals(3, val);
      assertEquals(2, counter.getCount());
   }

   @Test
   @DisplayName("FRMASK-NFR-01.1: Supplier anropas alltid minst en gång även utan konfigurerade retry-intervall")
   void get_with_retries_should_call_supplier_one_more_than_interval_list_length() throws RetriesExhaustedException
   {
      assertEquals(2, RetryUtil.getWithRetries(() -> Result.of(2), List.of()));
   }

   @Test
   @DisplayName("FRMASK-FR-02.3, FRMASK-NFR-01.1: RetriesExhaustedException kastas när alla retries är uttömda")
   void get_with_retries_should_throw_exception_after_exhausting_retries()
   {
      assertThrowsExactly(RetriesExhaustedException.class, () -> RetryUtil.getWithRetries(Result::empty, List.of(1)));
   }

   @Test
   @DisplayName("FRMASK-NFR-01.1: runWithRetries() avslutas utan exception vid lyckat första anrop")
   void run_with_retries_should_not_throw_on_success()
   {
      try
      {
         RetryUtil.runWithRetries(() -> true, List.of(1));
      }
      catch (RetriesExhaustedException e)
      {
         fail();
      }
   }

   @Test
   @DisplayName("FRMASK-NFR-01.1, FRMASK-NFR-01.2: Runnable anropas igen efter konfigurerat retry-intervall vid misslyckat anrop")
   void run_with_retries_should_call_supplier_again_after_delay() throws RetriesExhaustedException
   {
      var counter = new Counter();
      RetryUtil.runWithRetries(() -> {
         var count = counter.getCount();
         counter.increment();

         return count >= 1;
      }, List.of(1));
      assertEquals(2, counter.getCount());
   }

   @Test
   @DisplayName("FRMASK-NFR-01.1: Runnable anropas alltid minst en gång även utan konfigurerade retry-intervall")
   void run_with_retries_should_call_supplier_one_more_than_interval_list_length() throws RetriesExhaustedException
   {
      Counter counter = new Counter();
      RetryUtil.runWithRetries(() -> {
         counter.increment();
         return true;
      }, List.of());
      assertEquals(1, counter.getCount());
   }

   @Test
   @DisplayName("FRMASK-FR-05.3, FRMASK-NFR-01.1: RetriesExhaustedException kastas när alla retries är uttömda")
   void run_with_retries_should_throw_exception_after_exhausting_retries()
   {
      assertThrowsExactly(RetriesExhaustedException.class, () -> RetryUtil.runWithRetries(() -> false, List.of(1)));
   }

   private class Counter
   {
      private int count = 0;

      public int getCount()
      {
         return count;
      }

      public void increment()
      {
         count++;
      }
   }
}
