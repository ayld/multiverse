package org.multiverse.instrumentation;

import org.junit.Test;
import org.multiverse.api.annotations.TmEntity;

/**
 * At the moment the instrumentation doesn't support arrays. So when an array is found in
 * a TmEntity, the instrumentation should fail.
 */
public class ArraysAreRejectedTest {

    @Test
    public void booleanArrayIsDetected() {
        new BooleanArray();
    }

    @TmEntity
    public static class BooleanArray {
        boolean[] array;
    }

    @Test
    public void charArrayIsDetected() {
        new CharArray();
    }

    @TmEntity
    public static class CharArray {
        char[] array;
    }

    @Test
    public void shortArray() {
        new ShortArray();
    }

    @TmEntity
    public static class ShortArray {
        short[] array;
    }

    @Test
    public void intArrayIsDetected() {
        new IntArray();
    }

    @TmEntity
    public static class IntArray {
        int[] array;
    }

    @Test
    public void floatArrayIsDetected() {
        new FloatArray();
    }

    @TmEntity
    public static class FloatArray {
        float[] array;
    }

    @Test
    public void longArrayIsDetected() {
        new LongArray();
    }

    @TmEntity
    public static class LongArray {
        long[] array;
    }

    @Test
    public void doubleArrayIsDetected() {
        new DoubleArray();
    }

    @TmEntity
    public static class DoubleArray {
        double[] array;
    }

    @Test
    public void objectArrayIsDetected() {
        new ObjectArray();
    }

    @TmEntity
    public static class ObjectArray {
        Object[] array;
    }
}
