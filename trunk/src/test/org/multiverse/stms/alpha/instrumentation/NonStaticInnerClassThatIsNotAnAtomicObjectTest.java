package org.multiverse.stms.alpha.instrumentation;

import org.junit.Test;
import org.multiverse.api.annotations.AtomicObject;

/**
 * @author Peter Veentjer
 */
public class NonStaticInnerClassThatIsNotAnAtomicObjectTest {

    @Test
    public void testAnonymousInnerClass() {
        AnonymousInnerClass o = new AnonymousInnerClass();
    }

    @AtomicObject
    public static class AnonymousInnerClass {

        private int i;

        public AnonymousInnerClass() {
           new Runnable(){
               @Override
               public void run() {
                   //todo
               }
           };
        }

    }

    @Test
    public void testNamedInnerClass() {
        NamedInnerClass executor = new NamedInnerClass();
    }

     @AtomicObject
    public static class NamedInnerClass {

        private int i;

        public NamedInnerClass() {
            new SomeRunnable();           
        }

         class SomeRunnable implements Runnable{
             @Override
             public void run() {
                 //todo
             }
         }
     }
}