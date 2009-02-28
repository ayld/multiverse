package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
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
        handle = HandleGenerator.createHandle();
    }

    public Person(int age, String name) {
        this.age = age;
        this.name = name;
        this.handle = HandleGenerator.createHandle();
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
        this.handle = dehydratedPerson.___getHandle();
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

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
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

    public boolean ___isImmutable() {
        return false;
    }

    public DehydratedPerson ___deflate(long commitVersion) {
        return new DehydratedPerson(this, commitVersion);
    }

    private StmObject next;

    public void setNext(StmObject next) {
        this.next = next;
    }

    public StmObject getNext() {
        return next;
    }

    public static class DehydratedPerson extends AbstractDeflated {
        private final int age;
        private final String name;
        private final long parentHandle;

        public DehydratedPerson(long handle, int age, String name) {
            super(handle, 0);
            this.age = age;
            this.name = name;
            this.parentHandle = 0;
        }

        public DehydratedPerson(long handle, int age, String name, long parentHandle) {
            super(handle, 0);
            this.age = age;
            this.name = name;
            this.parentHandle = parentHandle;
        }

        public DehydratedPerson(Person person, long commitVersion) {
            super(person.___getHandle(), commitVersion);
            this.age = person.age;
            this.name = person.name;
            this.parentHandle = StmObjectUtils.getHandle(person.getParent());
        }

        public Person ___inflate(Transaction transaction) {
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
            return that.___getHandle() == this.___getHandle() &&
                    that.name == this.name &&
                    that.age == this.age &&
                    that.parentHandle == this.parentHandle;
        }

        public String toString() {
            return format("DehydratedPerson(age=%s,name=%s,parentHandle=%s", age, name, parentHandle);
        }
    }
}
