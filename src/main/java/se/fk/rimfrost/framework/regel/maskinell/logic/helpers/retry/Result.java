package se.fk.rimfrost.framework.regel.maskinell.logic.helpers.retry;

import java.util.NoSuchElementException;

public final class Result<T>
{
   private final boolean isEmpty;
   private final T value;

   private Result(T value, boolean isEmpty)
   {
      this.value = value;
      this.isEmpty = isEmpty;
   }

   public static <T> Result<T> of(T value)
   {
      return new Result<>(value, false);
   }

   public static <T> Result<T> empty()
   {
      return new Result<>(null, true);
   }

   public boolean isEmpty()
   {
      return this.isEmpty;
   }

   public T get()
   {
      if (isEmpty())
      {
         throw new NoSuchElementException("No value present");
      }

      return value;
   }
}
