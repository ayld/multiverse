package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.multiversionedstm.StmObjectUtils;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import static java.lang.String.format;
import java.util.Iterator;

public class Person implements StmObject {

    private int age;
    private String name;
    private Person parent;

    public Person() {
        handle = HandleGenerator.create();
    }

    public Person(int age, String name) {
        this.age = age;
        this.name = name;
        this.handle = HandleGenerator.create();
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
            parent = (Person) transaction.read(initialDehydratedPerson.parentHandle);
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

    private boolean parent_localized = true;
    private DehydratedPerson initialDehydratedPerson;
    private Transaction transaction;
    private final long handle;

    public Person(DehydratedPerson dehydratedPerson, Transaction transaction) {
        //initialization of operational properties
        this.handle = dehydratedPerson.getHandle();
        this.transaction = transaction;
        this.initialDehydratedPerson = dehydratedPerson;

        //reinitialization of the fields
        this.age = initialDehydratedPerson.age;
        this.name = initialDehydratedPerson.name;
        this.parent_localized = false;
    }

    public long ___getHandle() {
        return handle;
    }

    public DehydratedStmObject ___getInitialDehydratedStmObject() {
        return initialDehydratedPerson;
    }

    public Iterator<StmObject> ___loadedMembers() {
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
                (parent_localized && (initialDehydratedPerson.parentHandle != (parent == null ? 0 : parent.___getHandle())));
    }

    public DehydratedStmObject ___dehydrate() {
        return new DehydratedPerson(this);
    }

    public static class DehydratedPerson extends DehydratedStmObject {
        private final int age;
        private final String name;
        private final long parentHandle;

        public DehydratedPerson(long handle, int age, String name) {
            super(handle);
            this.age = age;
            this.name = name;
            this.parentHandle = 0;
        }

        public DehydratedPerson(long handle, int age, String name, long parentHandle) {
            super(handle);
            this.age = age;
            this.name = name;
            this.parentHandle = parentHandle;
        }

        public DehydratedPerson(Person person) {
            super(person.___getHandle());
            this.age = person.age;
            this.name = person.name;
            this.parentHandle = StmObjectUtils.getHandle(person.getParent());
        }

        public Iterator<Long> members() {
            throw new RuntimeException();
        }

        public Person hydrate(Transaction transaction) {
            return new Person(this, transaction);
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
                    that.parentHandle == this.parentHandle;
        }

        public String toString() {
            return format("DehydratedPerson(age=%s,name=%s,parentHandle=%s", age, name, parentHandle);
        }
    }
}
