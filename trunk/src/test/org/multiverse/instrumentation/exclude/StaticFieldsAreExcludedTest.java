package org.multiverse.instrumentation.exclude;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestUtils;
import org.multiverse.api.annotations.TmEntity;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class StaticFieldsAreExcludedTest {
    private File errorOutputFile;

    @Before
    public void setUp() throws IOException {
        errorOutputFile = File.createTempFile("foo", "bar");
        errorOutputFile.deleteOnExit();
        System.setErr(new PrintStream(errorOutputFile));
    }

    private void assertStaticWarningIsFound() {
        String output = TestUtils.readText(errorOutputFile);
        assertTrue(output.startsWith("Warning: "));
        assertTrue(output.toLowerCase().indexOf("static") != -1);
    }

    @Test
    public void booleanArrayIsDetected() throws IOException {
        new StaticField();
        assertStaticWarningIsFound();
    }

    @TmEntity
    public static class StaticField {
        static int field;
    }
}
