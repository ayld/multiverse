package org.multiverse.instrumentation;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import static org.multiverse.TestUtils.read;
import org.multiverse.api.Handle;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class TmEntityClassTransformer_AccessModifiersTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    public <E> E commitAndRead(E item) {
        Handle<E> handle = commit(stm, item);
        return read(stm, handle);
    }

    @Test
    public void privateField() {
        PrivateField original = new PrivateField();
        original.s = "foo";

        PrivateField found = commitAndRead(original);
        assertEquals("foo", found.s);
    }

    @TmEntity
    public static class PrivateField {
        private String s;
    }

    @Test
    public void protectedField() {
        ProtectedField original = new ProtectedField();
        original.s = "foo";

        ProtectedField found = commitAndRead(original);
        assertEquals("foo", found.s);
    }

    @TmEntity
    public static class ProtectedField {
        protected String s;
    }

    @Test
    public void publicField() {
        PublicField original = new PublicField();
        original.s = "foo";

        PublicField found = commitAndRead(original);
        assertEquals("foo", found.s);
    }

    @TmEntity
    public static class PublicField {
        public String s;
    }

    @Test
    public void packageFriendlyField() {
        PackageFriendlyField original = new PackageFriendlyField();
        original.s = "foo";

        PackageFriendlyField found = commitAndRead(original);
        assertEquals("foo", found.s);
    }

    @TmEntity
    public static class PackageFriendlyField {
        String s;
    }

    @Test
    public void finalField() {
        FinalField original = new FinalField("foo");

        FinalField found = commitAndRead(original);
        assertEquals("foo", found.s);
    }

    @TmEntity
    public static class FinalField {
        public final String s;

        public FinalField(String s) {
            this.s = s;
        }
    }
}
