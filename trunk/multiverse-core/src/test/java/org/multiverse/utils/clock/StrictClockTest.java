package org.multiverse.utils.clock;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Peter Veentjer
 */
public class StrictClockTest {

    @Test
    public void testNoArgConstructor(){
        StrictClock clock  = new StrictClock();
        assertEquals(0, clock.getDawn());
        assertEquals(0, clock.getTime());
    }

    @Test
    public void testConstructorWithDawn(){
        StrictClock clock  = new StrictClock(10);
        assertEquals(10, clock.getDawn());
        assertEquals(10, clock.getTime());
    }

    @Test
    public void testTick(){
        StrictClock clock = new StrictClock();
        long old = clock.getTime();
        long returned = clock.tick();
        assertEquals(old+1, clock.getTime());
        assertEquals(returned, clock.getTime());
    }

    @Test
    public void testToString(){
        StrictClock clock = new StrictClock(1000);
        //make sure that the toString function makes use of the time and not of the dawn.
        clock.tick();

        assertEquals("StrictClock(time=1001)", clock.toString());
    }
}
