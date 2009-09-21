package org.multiverse.datastructures.collections;


import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

public class ConcurrentHashMapTest {

    private AlphaStm stm;
    private ConcurrentHashMap<Integer, String> map;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        map = new ConcurrentHashMap<Integer, String>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        map.put(4, "four");
        map.put(5, null);
        // 17 should map to the same bucket as 1, with the default settings
        map.put(17, "seventeen");
    }
    
    @Test
    public void clearList() {
    	assertEquals(map.size(), 6);
    	map.clear();
    	
    	assertEquals(map.size(), 0);
    	for (Integer key : map.keySet()) {
    		fail("No keys should be present");
    	}
    	for (String value : map.values()) {
    		fail("No values should be present");
    	}
    	for (Map.Entry<Integer, String> entry : map.entrySet()) {
    		fail("No entries should be present");
    	}	
    }
    
    @Test
    public void containsKey() {
    	assertTrue(map.containsKey(1));
    	assertTrue(map.containsKey(5));
    	assertFalse(map.containsKey(10));
    	assertTrue(map.containsKey(17));
    }
    
    @Test
    public void containsValue()	{
    	assertTrue(map.containsValue("one"));
    	assertTrue(map.containsValue(null));
    	assertFalse(map.containsValue("ten"));
    	assertTrue(map.containsValue("seventeen"));   	
    }
    
    @Test
    public void modifyEntrySet() {
    	for (Map.Entry<Integer, String> entry : map.entrySet()) {
    		entry.setValue("fourtytwo");
    	}

    	for (String value : map.values()) {
    		assertEquals(value, "fourtytwo");
    	}
    }
    
    @Test
    public void get() {
    	assertEquals(map.get(1), "one");
    	assertEquals(map.get(17), "seventeen");
    	assertEquals(map.get(5), null);
    	assertEquals(map.get(10), null);
    }
}
