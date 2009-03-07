package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestUtils;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedstm.*;
import org.codehaus.multiverse.util.iterators.EmptyIterator;
import org.codehaus.multiverse.util.iterators.InstanceIterator;

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

    public void incAge() {
        age++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getParent() {
        //GENERATED
        if (parendHolder != null) {
            parent = parendHolder.getAndLoadIfNeeded(transaction);
            parendHolder = null;
        }

        return parent;
    }

    public void setParent(Person parent) {
        //GENERATED
        parendHolder = null;
        this.parent = parent;
    }

    //todo: equals and hashcode

    //==================== GENERATED =====================

    private UnloadedHolder<Person> parendHolder;
    private DehydratedPerson initialDehydratedPerson;
    private MyTransaction transaction;
    private final long handle;

    public Person(DehydratedPerson dehydratedPerson, MyTransaction transaction) {
        //initialization of operational properties
        this.handle = dehydratedPerson.___getHandle();
        this.transaction = transaction;
        this.initialDehydratedPerson = dehydratedPerson;

        //reinitialization of the fields
        this.age = dehydratedPerson.age;
        this.name = dehydratedPerson.name;
        this.parendHolder = transaction.readHolder(dehydratedPerson.parentHandle);
    }

    public long ___getHandle() {
        return handle;
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        if (parendHolder != null || parent == null)
            return EmptyIterator.INSTANCE;

        return new InstanceIterator(parent);
    }

    public void ___onAttach(MyTransaction transaction) {
        this.transaction = transaction;
    }

    public MyTransaction ___getTransaction() {
        return transaction;
    }

    public boolean ___isDirtyIgnoringStmMembers() {
        if (initialDehydratedPerson == null)
            return true;

        if (initialDehydratedPerson.age != age)
            return true;

        if (initialDehydratedPerson.name != name)
            return true;

        if (parendHolder == null)
            return true;

        return false;
    }

    public boolean ___isImmutableObjectGraph() {
        return false;
    }

    public DehydratedPerson ___deflate(long commitVersion) {
        return new DehydratedPerson(this, commitVersion);
    }

    public static class DehydratedPerson extends AbstractDeflated {
        private final int age;
        private final String name;
        private final long parentHandle;

        public DehydratedPerson(Person person, long commitVersion) {
            super(person.___getHandle(), commitVersion);
            this.age = person.age;
            this.name = person.name;
            this.parentHandle = StmObjectUtils.getHandle(person.getParent());
        }

        public Person ___inflate(Transaction transaction) {
            return new Person(this, (MyTransaction) transaction);
        }

        //equals and hash only are needed for testing purposes
        public int hashCode() {
            return new Long(___getHandle()).hashCode();
        }

        //equals and hash for testing purposes.
        public boolean equals(Object thatObj) {
            if (thatObj == this)
                return true;

            if (!(thatObj instanceof DehydratedPerson))
                return false;

            DehydratedPerson that = (DehydratedPerson) thatObj;
            if (that.___getHandle() != this.___getHandle())
                return false;

            if (that.___getVersion() != this.___getVersion())
                return false;

            if (that.age != this.age)
                return false;

            if (that.parentHandle != this.parentHandle)
                return false;

            if (!TestUtils.equals(that.name, this.name))
                return false;

            return true;
        }

        public String toString() {
            return format("DehydratedPerson(handle=%s, version=%s, age=%s,name=%s,parentHandle=%s)",
                    ___getHandle(), ___getVersion(), age, name, parentHandle);
        }
    }
}
