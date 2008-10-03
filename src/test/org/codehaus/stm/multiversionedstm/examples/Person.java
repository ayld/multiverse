package org.codehaus.stm.multiversionedstm.examples;

import org.codehaus.stm.multiversionedstm.HydratedCitizen;
import org.codehaus.stm.multiversionedstm.MultiversionedStm;
import org.codehaus.stm.multiversionedstm.Citizen;

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

    private HydratedPerson initialHydratedPerson;

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
            parent = parentPtr == 0 ? null : (Person) transaction.read(parentPtr);
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

    public Iterator<Citizen> ___findNewlyborns() {
        if(parent_localized && parent!=null)
            return asList((Citizen)parent).iterator();
        else
            return Collections.EMPTY_LIST.iterator();
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

    private void checkTransaction(MultiversionedStm.MultiversionedTransaction transaction) {
        if (transaction == null)
            throw new NullPointerException();
        if (this.transaction != null && this.transaction != transaction)
            throw new IllegalStateException();
    }

    public MultiversionedStm.MultiversionedTransaction ___getTransaction() {
        return transaction;
    }

    public boolean ___isDirty() {
        return initialHydratedPerson.age != age ||
                initialHydratedPerson.name != null ||
                (parent_localized && (initialHydratedPerson.parentPtr != (parent == null ? 0 : parent.___getPointer())));
    }

    public HydratedCitizen ___hydrate() {
        HydratedPerson hydratedPerson = new HydratedPerson();
        hydratedPerson.age = age;
        hydratedPerson.name = name;
        hydratedPerson.parentPtr = parent == null ? 0L : parent.___getPointer();
        return hydratedPerson;
    }

    public static class HydratedPerson implements HydratedCitizen {
        private int age;
        private String name;
        private long parentPtr;

        public HydratedPerson(){}

        public HydratedPerson(int age, String name, long parentPtr) {
            this.age = age;
            this.name = name;
            this.parentPtr = parentPtr;
        }

        public Citizen dehydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction) {
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

            if (!(thatObj instanceof HydratedPerson))
                return false;

            HydratedPerson that = (HydratedPerson) thatObj;
            return that.name == this.name && that.age == this.age && that.parentPtr == this.parentPtr;
        }

        public String toString(){
            return format("HydratedPerson(age=%s,name=%s,parentPtr=%s",age,name,parentPtr);
        }
    }
}
