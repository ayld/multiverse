package org.multiverse.instrumentation.exclusion;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestUtils;
import static org.multiverse.TestUtils.commitAndRead;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class VolatileFieldsAreExcludedTest {
    private File errorOutputFile;
    private MultiversionedStm stm;

    @Before
    public void setUp() throws IOException {
        stm = new MultiversionedStm();

        errorOutputFile = File.createTempFile("foo", "bar");
        errorOutputFile.deleteOnExit();

        System.setErr(new PrintStream(errorOutputFile));
    }

    private void assertVolatileWarningIsFound() {
        String output = TestUtils.readText(errorOutputFile);
        assertTrue(output.startsWith("Warning: "));
        assertTrue(output.toLowerCase().indexOf("volatile") != -1);
    }

    @Test
    public void volatileFieldIsDetected() throws IOException {
        new VolatileField();
        assertVolatileWarningIsFound();
    }

    @TmEntity
    public static class VolatileField {
        volatile int field;
    }

    @Test
    public void volatileFieldAreIgnored() {
        VolatileField field = new VolatileField();
        field.field = 123;

        VolatileField found = commitAndRead(stm, field);
        assertEquals(found.field, 0);
    }
}
