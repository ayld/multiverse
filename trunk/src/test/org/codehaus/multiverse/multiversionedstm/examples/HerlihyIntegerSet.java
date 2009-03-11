package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestUtils;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.MultiversionedTransaction;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.multiversionedstm.StmObjectUtils;
import org.codehaus.multiverse.util.iterators.EmptyIterator;
import org.codehaus.multiverse.util.iterators.InstanceIterator;

import java.util.Iterator;

/**
 * This set doesn't load lazy (not needed), so the complete object graph.
 *
 * @author Peter Veentjer
 */
public final class HerlihyIntegerSet implements StmObject {

    private final long handle;
    private Node head;

    public HerlihyIntegerSet(int... values) {
        handle = HandleGenerator.createHandle();

        for (int value : values)
            add(value);
    }

    public boolean add(int value) {
        if (head == null) {
            //first item
            head = new Node(value, null);
            return true;
        } else {
            //there are items.
            Node prev = null;
            Node node = head;
            while (node != null) {
                //there already is a node with the value
                if (node.value == value)
                    return false;

                if (node.value > value) {
                    if (prev == null) {
                        head = new Node(value, node);
                    } else {
                        prev.next = new Node(value, node);
                    }

                    return true;
                }

                prev = node;
                node = node.next;
            }

            //adds in the end
            prev.next = new Node(value, prev.next);
            return true;
        }
    }

    public int size() {
        Node node = head;

        int size = 0;
        while (node != null) {
            size++;
            node = node.next;
        }
        return size;
    }

    public boolean contains(int value) {
        Node node = head;

        while (node != null) {
            if (node.value == value)
                return true;

            if (node.value < value)
                return false;

            node = node.next;
        }

        return false;
    }

    public String toString() {
        if (head == null)
            return "[]";

        if (head.next == null)
            return "[" + head.value + "]";

        StringBuffer sb = new StringBuffer("[" + head.value);
        Node node = head.next;
        do {
            sb.append("," + node.value);
            node = node.next;
        } while (node != null);
        sb.append("]");

        return sb.toString();
    }

    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof HerlihyIntegerSet))
            return false;

        HerlihyIntegerSet that = (HerlihyIntegerSet) thatObj;
        return TestUtils.equals(that.head, this.head);
    }

    public int hashCode() {
        return head == null ? 0 : head.hashCode();
    }

    static class Node implements StmObject {

        private final long handle;
        private Node next;
        private int value;

        public Node(int value, Node next) {
            this.handle = HandleGenerator.createHandle();
            this.value = value;
            this.next = next;
        }

        public Node(DehydratedNode dehydratedNode, MultiversionedTransaction transaction) {
            this.dehydratedNode = dehydratedNode;
            this.handle = dehydratedNode.___getHandle();
            this.value = dehydratedNode.value;
            this.next = (Node) transaction.read(dehydratedNode.nextHandle);
        }

        @Override
        public int hashCode() {
            return next == null ? value : value + next.hashCode();
        }

        @Override
        public boolean equals(Object thatObject) {
            if (thatObject == this)
                return true;

            if (!(thatObject instanceof Node))
                return false;

            Node that = (Node) thatObject;
            if (that.value != this.value)
                return false;

            return TestUtils.equals(that.next, this.next);
        }

        // ============== generated ==================

        private DehydratedNode dehydratedNode;

        public long ___getHandle() {
            return handle;
        }

        public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
            if (next == null)
                return EmptyIterator.INSTANCE;

            return new InstanceIterator(next);
        }

        public boolean ___isDirtyIgnoringStmMembers() {
            if (dehydratedNode == null)
                return true;


            if (dehydratedNode.nextHandle != StmObjectUtils.getHandle(next))
                return true;

            return false;
        }

        public Deflated ___deflate(long version) {
            return dehydratedNode = new DehydratedNode(this, version);
        }
    }

    //======================

    private DehydratedIntegerSet dehydrated;

    private HerlihyIntegerSet(DehydratedIntegerSet dehydratedIntegerSet, MultiversionedTransaction transaction) {
        this.handle = dehydratedIntegerSet.___getHandle();
        this.head = (Node) transaction.read(dehydratedIntegerSet.headHandle);
        this.dehydrated = dehydratedIntegerSet;
    }

    public long ___getHandle() {
        return handle;
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        if (head == null)
            return EmptyIterator.INSTANCE;

        return new InstanceIterator(head);
    }

    public boolean ___isDirtyIgnoringStmMembers() {
        if (dehydrated == null)
            return true;

        if (dehydrated.headHandle != StmObjectUtils.getHandle(head))
            return true;

        return false;
    }

    public Deflated ___deflate(long version) {
        return dehydrated = new DehydratedIntegerSet(this, version);
    }

    private static class DehydratedIntegerSet extends AbstractDeflated {
        final long headHandle;

        public DehydratedIntegerSet(HerlihyIntegerSet integerSet, long version) {
            super(integerSet.___getHandle(), version);
            headHandle = StmObjectUtils.getHandle(integerSet.head);
        }

        @Override
        public Deflatable ___inflate(Transaction transaction) {
            return new HerlihyIntegerSet(this, (MultiversionedTransaction) transaction);
        }
    }

    private static class DehydratedNode extends AbstractDeflated {
        final long nextHandle;
        final int value;

        private DehydratedNode(Node node, long version) {
            super(node.handle, version);
            this.nextHandle = StmObjectUtils.getHandle(node.next);
            this.value = node.value;
        }

        @Override
        public Deflatable ___inflate(Transaction transaction) {
            return new Node(this, (MultiversionedTransaction) transaction);
        }
    }
}
