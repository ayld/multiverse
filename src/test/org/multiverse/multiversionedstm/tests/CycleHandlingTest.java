package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertSameHandle;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.util.*;

/**
 * A Test to see how well the MultiversionedStm deals with cycles
 *
 * @author Peter Veentjer.
 */
public class CycleHandlingTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @TmEntity
    public static class SingleLinkedNode {
        private SingleLinkedNode next;

        public void setNext(SingleLinkedNode next) {
            this.next = next;
        }

        public SingleLinkedNode getNext() {
            return next;
        }
    }


    @Test
    public void directCycle() {
        SingleLinkedNode original = new SingleLinkedNode();
        original.setNext(original);

        Handle<SingleLinkedNode> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        SingleLinkedNode found = t.read(handle);
        assertSameHandle(original, found);
        assertSameHandle(original.getNext(), found.getNext());
        t.commit();
    }

    @Test
    public void shortIndirectCycle() {
        SingleLinkedNode original1 = new SingleLinkedNode();
        SingleLinkedNode original2 = new SingleLinkedNode();
        original1.setNext(original2);
        original2.setNext(original1);

        Handle<SingleLinkedNode> handle = commit(stm, original1);
        Transaction t = stm.startTransaction();
        SingleLinkedNode found = t.read(handle);
        assertSameHandle(original1, found);
        assertSameHandle(original2, found.getNext());
        assertSameHandle(original1, found.getNext().getNext());
        t.commit();
    }

    /**
     * A large number is chosen to see if the system doesn't run out of call stack. If there
     * is some recursion going on, we would notice it here.
     */
    @Test
    public void longIndirectCycle() {
        SingleLinkedNode original = createLongChain(100000);

        Handle<SingleLinkedNode> handle = commit(stm, original);
        Transaction t = stm.startTransaction();

        SingleLinkedNode found = t.read(handle);
        SingleLinkedNode currentOriginal = original;
        SingleLinkedNode currentFound = found;
        do {
            assertSameHandle(currentFound, currentOriginal);
            currentOriginal = currentOriginal.getNext();
            currentFound = currentFound.getNext();
        } while (currentOriginal != original);
    }

    private SingleLinkedNode createLongChain(int depth) {
        SingleLinkedNode first = new SingleLinkedNode();
        SingleLinkedNode current = first;
        for (int k = 0; k < depth; k++) {
            SingleLinkedNode newHolder = new SingleLinkedNode();
            current.setNext(newHolder);
            current = newHolder;
        }

        current.setNext(first);
        return first;
    }

    /**
     * A large number is chosen to make sure that there is no hidden recursion in the system.
     */
    @Test
    public void complexObjectGraphWithLoadsOfCycles() {
        int nodeCount = 100000;
        ComplexNode original = createComplexGraphWithLoadsOfCycles(nodeCount);
        complexObjectGraphWithLoadsOfCycles(nodeCount, original);
    }

    /**
     * A large number is chosen to make sure that there is no hidden recursion in the system.
     */
    @Test
    public void anoterComplexObjectGraphWithLoadsOfCycles() {
        int nodeCount = 100000;
        ComplexNode original = createAnotherComplexGraphWithLoadsOfCycles(nodeCount);
        complexObjectGraphWithLoadsOfCycles(nodeCount, original);
    }


    public void complexObjectGraphWithLoadsOfCycles(int nodeCount, ComplexNode original) {

        assertFalse(original.getEdge1() == null && original.getEdge2() == null && original.getEdge3() == null);

        long oldWriteCount = stm.getStatistics().getWriteCount();
        Handle<ComplexNode> handle = commit(stm, original);
        long newWriteCount = stm.getStatistics().getWriteCount();

        assertEquals(oldWriteCount + nodeCount, newWriteCount);
        assertStructuralContentIsEquals(original, handle);
    }

    private void assertStructuralContentIsEquals(ComplexNode original, Handle<ComplexNode> handle) {
        Transaction t = stm.startTransaction();
        ComplexNode found = t.read(handle);

        assertSameHandle(original, found);
        Map<ComplexNode, Object> doneMap = new IdentityHashMap<ComplexNode, Object>();
        Stack<Pair> traverseStack = new Stack<Pair>();
        traverseStack.push(new Pair(original, found));
        do {
            Pair pair = traverseStack.pop();

            assertSameHandle(pair.original, pair.found);

            if (pair.original == null) {
                //since they have the same handle and original is null, found will be null as well.
                //so no need to traverse any further.
                break;
            } else {
                if (!doneMap.containsKey(pair.original)) {
                    doneMap.put(pair.original, "");
                    traverseStack.push(new Pair(pair.original.getEdge1(), pair.found.getEdge1()));
                    traverseStack.push(new Pair(pair.original.getEdge2(), pair.found.getEdge2()));
                    traverseStack.push(new Pair(pair.original.getEdge3(), pair.found.getEdge3()));
                }
            }
        } while (!traverseStack.isEmpty());
    }

    class Pair {
        ComplexNode original;
        ComplexNode found;

        Pair(ComplexNode original, ComplexNode found) {
            assert original != null && found != null;
            this.original = original;
            this.found = found;
        }
    }

    /**
     * Create a complex graph that can contain cycles. The amount of nodes
     * allocated are controlled by the nodeCount parameter.
     *
     * @param nodeCount The amount of ComplexNode objects allocated.
     * @return A graph of ComplexNodes that can contain cycles.
     */
    private ComplexNode createComplexGraphWithLoadsOfCycles(int nodeCount) {
        if (nodeCount <= 0) {
            throw new IllegalArgumentException("nodeCount parameter should be > 0.");
        }

        final Random rng = new Random();

        // The chance for the generation of a reference to a previous node.
        final double referenceProbability = 0.1;
        // The chance for the generation of a terminator node.
        final double nullProbability = 0.2;
        // Maximum amount of previously generated nodes stored for back referencing.
        final int maxReferenceListSize = 12;

        // The workList contains all generated ComplexNodes that have no child nodes yet
        // and are waiting for processing.
        LinkedList<ComplexNode> workList = new LinkedList<ComplexNode>();
        // The backreferences contains the list of ComplexNodes that can get referenced by
        // a ComplexNode that is processed.
        LinkedList<ComplexNode> backreferences = new LinkedList<ComplexNode>();

        // Initialize the lists with the root node.
        ComplexNode root = new ComplexNode();
        --nodeCount;
        workList.addFirst(root);
        backreferences.addFirst(root);

        while (!workList.isEmpty() && nodeCount > 0) {
            ComplexNode current = workList.removeLast();
            ComplexNode[] children = new ComplexNode[3];

            for (int i = 0; i < 3; ++i) {
                double randomVal = rng.nextDouble();
                if (randomVal <= nullProbability) {
                    children[i] = null;
                } else if (randomVal <= (nullProbability + referenceProbability) || nodeCount <= 0) {
                    // We choose to generate a back reference when we are not allowed to allocate new ComplexNodes
                    // in order to increase the complexity.
                    children[i] = backreferences.peekLast();
                } else {
                    children[i] = new ComplexNode();
                    workList.push(children[i]);
                    backreferences.addFirst(children[i]);
                    --nodeCount;
                    if (backreferences.size() > maxReferenceListSize)
                        backreferences.removeLast();
                }
            }

            current.setEdge1(children[0]);
            current.setEdge2(children[1]);
            current.setEdge3(children[2]);
        }

        // After the loop, it could be that the workList is not empty and still many ComplexNodes would
        // like to get processed. We can safely ignore these nodes, as their children are initialized to null
        // (terminator node) by default.
        // Another option is that the workList is empty, but not enough nodes are generated. This could happen
        // if the null probability is set high and a lot of terminators are generated. In that case, we choose to
        // pad the tree's rightmost child that has a null child right child with a tree that has only 
        // right children. We continue this process until we end up with the correct amount of nodes.
        if (nodeCount > 0) {
        	ComplexNode rightMost = root;
        	while (rightMost.getEdge3() != null) {
        		rightMost = rightMost.getEdge3();
        	}
        	
        	ComplexNode parent = rightMost;
        	while (nodeCount > 0) {
        		ComplexNode child = new ComplexNode();
        		--nodeCount;
        		parent.setEdge3(child);
        		parent = child;
        	}
        }
        
        return root;
    }

    /**
     * Create a complex graph that can contain cycles. The maximum amount of nodes
     * allocated are controlled by the nodeCount parameter, but less could be allocated,
     * depending on the random generation of the graph.
     *
     * @param nodeCount The maximum amount of ComplexNode objects allocated.
     * @return A graph of ComplexNodes that can contain cycles.
     */
    private ComplexNode createAnotherComplexGraphWithLoadsOfCycles(int nodeCount) {
        if (nodeCount <= 0) {
            throw new IllegalArgumentException("nodeCount parameter should be > 0.");
        }

        final Random rng = new Random();

        ComplexNode[] nodes = new ComplexNode[nodeCount];
        ComplexNode root = new ComplexNode();
        nodes[0] = root;

        ComplexNode current = root;

        //create an initial set all linked on edge2.
        for (int k = 1; k < nodeCount; k++) {
            ComplexNode newNode = new ComplexNode();
            nodes[k] = newNode;

            current.setEdge2(newNode);
            current = newNode;
        }

        //fill edge1 and 3.
        for (int k = 0; k < nodeCount; k++) {
            ComplexNode node = nodes[k];

            int edge1Index = (int) Math.round(Math.floor(rng.nextFloat() * nodeCount));
            node.setEdge1(nodes[edge1Index]);

            int edge3Index = (int) Math.round(Math.floor(rng.nextFloat() * nodeCount));
            node.setEdge3(nodes[edge3Index]);
        }

        //introducing self cycles and null references
        for (int k = 0; k < nodeCount; k += 10) {
            ComplexNode node = nodes[k];
            node.edge1 = node;
            node.edge3 = null;
        }

        return root;
    }


    @TmEntity
    static class ComplexNode {
        private ComplexNode edge1;
        private ComplexNode edge2;
        private ComplexNode edge3;

        public ComplexNode getEdge1() {
            return edge1;
        }

        public void setEdge1(ComplexNode edge1) {
            this.edge1 = edge1;
        }

        public ComplexNode getEdge2() {
            return edge2;
        }

        public void setEdge2(ComplexNode edge2) {
            this.edge2 = edge2;
        }

        public ComplexNode getEdge3() {
            return edge3;
        }

        public void setEdge3(ComplexNode edge3) {
            this.edge3 = edge3;
        }
    }
}
