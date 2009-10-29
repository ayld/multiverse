package org.multiverse.datastructures.collections;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * Created by IntelliJ IDEA.
 * User: alarmnummer
 * Date: Oct 28, 2009
 * Time: 9:36:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class StrictLinkedBlockingDeque_equalsAndHashTest {

    @Test
      public void equalsNull() {
          BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
          boolean equals = deque.equals(null);
          assertFalse(equals);
      }

      @Test
      public void equalsWithNonCollection() {
          BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
          boolean equals = deque.equals("foo");
          assertFalse(equals);
      }

      @Test
      public void equalsWithNonEqualCollection() {
          List<String> other = Arrays.asList("1", "2");
          BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
          boolean equals = deque.equals(other);
          assertFalse(equals);
      }

      //@Test
      public void equalsWithEqualCollection() {
          /*
          List<String> other = Arrays.asList("1", "2");
          BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
          deque.add("1");
          deque.add("2");
          System.out.println(deque.toString());
          System.out.println(other.toString());

          boolean equals = deque.equals(other);
          assertTrue(equals);
          assertEquals(deque.hashCode(),other.hashCode());*/
          testIncomplete();
      }

      @Test
      public void hash() {
          testIncomplete();
      }
    
}
