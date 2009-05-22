package org.multiverse.instrumentation.exclusion;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestUtils;
import static org.multiverse.TestUtils.commitAndRead;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * At the moment the instrumentation doesn't support arrays. So when an array is found in
 * a TmEntity, the instrumentation should fail.
 */
public class ArraysFieldsAreExcludedTest {
    private File errorOutputFile;
    private Stm stm;

    @Before
    public void setUp() throws IOException {
        stm = new MultiversionedStm();
        errorOutputFile = File.createTempFile("foo", "bar");
        errorOutputFile.deleteOnExit();

        System.setErr(new PrintStream(errorOutputFile));
    }

    private void assertArrayWarningIsFound() {
        String output = TestUtils.readText(errorOutputFile);
        assertTrue(output.startsWith("Warning: "));
        assertTrue(output.toLowerCase().indexOf("array") != -1);
    }

    @Test
    public void booleanArrayIsDetected() throws IOException {
        new BooleanArray();
        assertArrayWarningIsFound();
    }


    @TmEntity
    public static class BooleanArray {
        boolean[] array;
    }

    @Test
    public void charArrayIsDetected() {
        new CharArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class CharArray {
        char[] array;
    }

    @Test
    public void shortArray() {
        new ShortArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class ShortArray {
        short[] array;
    }

    @Test
    public void intArrayIsDetected() {
        new IntArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class IntArray {
        int[] array;
    }

    @Test
    public void floatArrayIsDetected() {
        new FloatArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class FloatArray {
        float[] array;
    }

    @Test
    public void longArrayIsDetected() {
        new LongArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class LongArray {
        long[] array;
    }

    @Test
    public void doubleArrayIsDetected() {
        new DoubleArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class DoubleArray {
        double[] array;
    }

    @Test
    public void objectArrayIsDetected() {
        new ObjectArray();
        assertArrayWarningIsFound();
    }

    @TmEntity
    public static class ObjectArray {
        Object[] array;

        public Object[] getArray() {
            return array;
        }
    }

    @Test
    public void arraysAreIgnored() {
        ObjectArray array = new ObjectArray();
        array.array = new String[]{"foo", "bar"};

        ObjectArray found = commitAndRead(stm, array);
        assertNull(found.getArray());
    }
}
