package se.fk.rimfrost.framework.regel.maskinell.logic.helpers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RetryUtil
{
   /***
    * Automatically retries the provided supplier if an empty result is returned.
    * The supplier is called at most retryIntervals.size() + 1 times.
    *
    * @param supplier Supplies an optional containing the value returned from the method. If an empty optional is returned the supplier will be called again after a delay.
    * @param retryIntervals The list of retry intervals used for waiting between each attempt. The list is assumed to contain positive integer values representing the intervals in seconds.
    * @return The value contained in the first non-empty optional returned from the supplier.
    * @param <T> The type of the value returned from the supplier.
    * @throws RetriesExhaustedException Thrown if no non-empty result was returned within retryIntervals.size() + 1 attempts of calling the supplier.
    */
   public static <T> T getWithRetries(Supplier<Optional<T>> supplier, List<Integer> retryIntervals)
         throws RetriesExhaustedException
   {
      int i = 0;
      do
      {
         var result = supplier.get();

         if (result.isPresent())
         {
            return result.get();
         }
         else if (i < retryIntervals.size())
         {
            delay(retryIntervals.get(i));
         }

         i++;
      }
      while (i <= retryIntervals.size());

      throw new RetriesExhaustedException();
   }

   /***
    * Automatically retries the provided supplier if a false result is returned.
    * The supplier is called at most retryIntervals.size() + 1 times.
    *
    * @param supplier Supplies a boolean value indicating if the operation was a success. If a false value is returned the supplied will be called again after a delay.
    * @param retryIntervals The list of retry intervals used for waiting between each attempt. The list is assumed to contain positive integer values representing the intervals in seconds.
    * @throws RetriesExhaustedException Thrown if the supplier did not indicate success within retryIntervals.size() + 1 attempts of calling the supplier.
    */
   public static void runWithRetries(Supplier<Boolean> supplier, List<Integer> retryIntervals) throws RetriesExhaustedException
   {
      int i = 0;
      do
      {
         var success = supplier.get();

         if (success)
         {
            return;
         }
         else if (i < retryIntervals.size())
         {
            delay(retryIntervals.get(i));
         }

         i++;
      }
      while (i <= retryIntervals.size());

      throw new RetriesExhaustedException();
   }

   private static void delay(int delay)
   {
      try
      {
         TimeUnit.SECONDS.sleep(delay);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Interrupted while waiting for retry", e);
      }
   }
}
