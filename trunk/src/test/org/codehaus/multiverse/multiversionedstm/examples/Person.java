package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.util.PtrUtils;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import static java.lang.String.format;
import java.util.Iterator;

public class Person implements StmObject {

    private int age;
    private String name;
    private Person parent;

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
            long parentPtr = initialDehydratedPerson.parentPtr;
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

    //GENERATED
    private boolean parent_localized = true;

    private DehydratedPerson initialDehydratedPerson;
    //GENERATED
    private Transaction transaction;

    //GENERATED
    private long handle;

    public long ___getHandle() {
        return handle;
    }

    public DehydratedStmObject ___getInitialDehydratedStmObject() {
        return initialDehydratedPerson;
    }

    public void ___setHandle(long ptr) {
        this.handle = ptr;
    }

    public Iterator<StmObject> ___directReferencedIterator() {
        if (parent != null && parent != this)
            return new ArrayIterator(parent);

        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(Transaction transaction) {
        if (transaction == null)
            throw new NullPointerException();

        if (this.transaction == transaction)
            return;

        if (this.transaction != null)
            throw new IllegalArgumentException("Object already bound to another transaction");

        this.transaction = transaction;
    }

    public Transaction ___getTransaction() {
        return transaction;
    }

    public boolean ___isDirty() {
        if (initialDehydratedPerson == null)
            return true;

        return initialDehydratedPerson.age != age ||
                initialDehydratedPerson.name != null ||
                (parent_localized && (initialDehydratedPerson.parentPtr != (parent == null ? 0 : parent.___getHandle())));
    }

    public DehydratedStmObject ___dehydrate() {
        return new DehydratedPerson(this);
    }

    public static class DehydratedPerson extends DehydratedStmObject {
        private final int age;
        private final String name;
        private final long parentPtr;

        public DehydratedPerson(long handle, int age, String name) {
            super(handle);
            this.age = age;
            this.name = name;
            this.parentPtr = 0;
        }

        public DehydratedPerson(Person person) {
            super(person.___getHandle());
            this.age = person.age;
            this.name = person.name;
            this.parentPtr = PtrUtils.getHandle(person.getParent());
        }

        public Iterator<Long> getDirect() {
            throw new RuntimeException();
        }

        public StmObject hydrate(Transaction transaction) {
            try {
                Person person = (Person) Person.class.newInstance();
                //initialization of operational properties
                person.handle = getHandle();
                person.transaction = transaction;
                person.initialDehydratedPerson = this;

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
            return that.getHandle() == this.getHandle() &&
                    that.name == this.name &&
                    that.age == this.age &&
                    that.parentPtr == this.parentPtr;
        }

        public String toString() {
            return format("DehydratedPerson(age=%s,name=%s,parentPtr=%s", age, name, parentPtr);
        }
    }
}
