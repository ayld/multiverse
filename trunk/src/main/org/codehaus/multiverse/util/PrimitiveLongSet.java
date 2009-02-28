package org.codehaus.multiverse.util;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveLongSet {
    private long value = 0;
    private Map map;

    public boolean add(long value) {
        if (map == null) {
            if (this.value == 0) {
                this.value = value;
                return true;
            }

            if (this.value == value)
                return false;

            map = new HashMap();
            map.put(value, this);
            map.put(this.value, this);
            return true;
        } else {
            return map.put(value, this) == null;
        }
    }
}
