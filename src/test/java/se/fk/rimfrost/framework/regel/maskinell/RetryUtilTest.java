package se.fk.rimfrost.framework.regel.maskinell;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.RetriesExhaustedException;
import se.fk.rimfrost.framework.regel.maskinell.logic.helpers.RetryUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class RetryUtilTest
{
   @Test
   void get_with_retries_should_return_supplier_value() throws RetriesExhaustedException
   {
      assertEquals(5, RetryUtil.getWithRetries(() -> Optional.of(5), List.of(5)));
   }

   @Test
   void get_with_retries_should_call_supplier_again_after_delay() throws RetriesExhaustedException
   {
      var counter = new Counter();
      var val = RetryUtil.getWithRetries(() -> {
         var count = counter.getCount();
         counter.increment();

         if (count < 1)
         {
            return Optional.empty();
         }
         else
         {
            return Optional.of(3);
         }
      }, List.of(1));
      assertEquals(3, val);
      assertEquals(2, counter.getCount());
   }

   @Test
   void get_with_retries_should_call_supplier_one_more_than_interval_list_length() throws RetriesExhaustedException
   {
      assertEquals(2, RetryUtil.getWithRetries(() -> Optional.of(2), List.of()));
   }

   @Test
   void get_with_retries_should_throw_exception_after_exhausting_retries()
   {
      assertThrowsExactly(RetriesExhaustedException.class, () -> RetryUtil.getWithRetries(Optional::empty, List.of(1)));
   }

   @Test
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
   void run_with_retries_should_call_supplier_again_after_delay() throws RetriesExhaustedException
   {
      var counter = new Counter();
      RetryUtil.runWithRetries(() -> {
         var count = counter.getCount();
         counter.increment();

         if (count < 1)
         {
            return false;
         }
         else
         {
            return true;
         }
      }, List.of(1));
      assertEquals(2, counter.getCount());
   }

   @Test
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
