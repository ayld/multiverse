package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.multiversionedstm.DehydratedCitizen;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.Citizen;
import org.codehaus.multiverse.util.ArrayIterator;
import org.codehaus.multiverse.util.EmptyIterator;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.Collections;
import static java.util.Arrays.asList;

public class Person implements Citizen {

    //GENERATED
    public final static int OFFSET_AGE = 1;
    public final static int OFFSET_NAME = 2;
    public final static int OFFSET_PARENT = 3;

    //GENERATED
    private MultiversionedStm.MultiversionedTransaction transaction;

    private int age;

    private String name;

    private Person parent;
    //GENERATED
    private boolean parent_localized = true;

    private DehydratedPerson initialHydratedPerson;

    //GENERATED
    private long ptr;

    public Person() {
    }

    public Person(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getParent() {
        //GENERATED
        if (!parent_localized) {
            long parentPtr = initialHydratedPerson.parentPtr;
            parent = parentPtr == 0 ? null : (Person) transaction.readRoot(parentPtr);
            parent_localized = true;
        }

        return parent;
    }

    public void setParent(Person parent) {
        //GENERATED
        parent_localized = true;
        this.parent = parent;
    }

    //==================== GENERATED =====================

    //todo: dehydrate?

    public long ___getPointer() {
        return ptr;
    }

    public void ___setPointer(long ptr) {
        this.ptr = ptr;
    }

     public Iterator<Citizen> ___directReachableIterator() {
        if (parent != null)
            return new ArrayIterator(parent);

         return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(MultiversionedStm.MultiversionedTransaction transaction) {
        if (transaction == null)
            throw new NullPointerException();

        if (this.transaction == transaction)
            return;

        if (this.transaction != null)
            throw new IllegalArgumentException("Object already bound to another transaction");

        this.transaction = transaction;
    }

   public MultiversionedStm.MultiversionedTransaction ___getTransaction() {
        return transaction;
    }

    public boolean ___isDirty() {
        if(initialHydratedPerson==null)
            return true;

        return initialHydratedPerson.age != age ||
                initialHydratedPerson.name != null ||
                (parent_localized && (initialHydratedPerson.parentPtr != (parent == null ? 0 : parent.___getPointer())));
    }

    public DehydratedCitizen ___dehydrate() {
        DehydratedPerson hydratedPerson = new DehydratedPerson();
        hydratedPerson.age = age;
        hydratedPerson.name = name;
        hydratedPerson.parentPtr = parent == null ? 0L : parent.___getPointer();
        return hydratedPerson;
    }

    public static class DehydratedPerson implements DehydratedCitizen {
        private int age;
        private String name;
        private long parentPtr;

        public DehydratedPerson() {
        }

        public DehydratedPerson(int age, String name, long parentPtr) {
            this.age = age;
            this.name = name;
            this.parentPtr = parentPtr;
        }

        public Citizen hydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction) {
            try {
                Person person = (Person) Person.class.newInstance();
                //initialization of operational properties
                person.ptr = ptr;
                person.transaction = transaction;
                person.initialHydratedPerson = this;

                //reinitialization of the fields
                person.age = age;
                person.name = name;
                person.parent_localized = false;

                return person;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //equals and hash only are needed for testing purposes
        public int hashCode() {
            return 0;
        }

        public boolean equals(Object thatObj) {
            if (thatObj == this)
                return true;

            if (!(thatObj instanceof DehydratedPerson))
                return false;

            DehydratedPerson that = (DehydratedPerson) thatObj;
            return that.name == this.name && that.age == this.age && that.parentPtr == this.parentPtr;
        }

        public String toString() {
            return format("DehydratedPerson(age=%s,name=%s,parentPtr=%s", age, name, parentPtr);
        }
    }
}
