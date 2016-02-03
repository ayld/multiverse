# FAQ #

## Which Java version is required ##
Java 6 is needed. In the future it also depends on Java 7 to make use of all features.

## Which libraries does Multverse depend on ##
Atm it only depends on ASM. But if you are not using bytecode instrumentation, even this dependency can be dropped.

## Where can Maven download the Multiverse JARs from ##
See MavenRepository for information on how to add the Multiverse dependencies to your Maven project.

## Can AtomicObjects be accessed without a transaction ##
This is not possible. When a field or method on a AtomicObject is called, it will always be done under a transaction, so if a transaction doesn't exist, it is created.

## What about extensibility ##
The Multiverse architecture makes it possible to extend existing engines through all kinds of policies, or it is even possible to create additional engines. Experimental engines (engines that contain features that will be moved to the main engines if useful) also make use of the Multiverse infrastructure.

## What about the retry/orelse ##
The Alpha Stm implementation (the main implementation containing the most features) supports the retry/orelse. The beta enigine doesn't.

## What about scalability ##
The design of main stm implementation (Alpha) is to remove contention points. The only contention points are the clock and of course the shared atomicobjects.

## Is Multiverse lockfree? ##
The current stm implementations need to acquire the locks on AtomicObjects before they do a commit. So the problems that can happen with non lock free algorithms still can happen with multiverse. This is something that needs research in the future.

## What about contention management ##
Contention management has at least 2 different scopes:
**Commit time: this can be influenced through CommitLockPolicy** Encounter time: this is not supported yet since Multiverse doesn't support pessimistic locking when an object is encountered. This reduces the need for contention management because locks already are hold for a short amount of time (only while committing). But in the future the contentionmanagers described in the work of Maurice Herlihy.

## What is the default STM implementation? ##
The default STM implementation is the AlphaStm. This STM implementation is a general purpose implementation that supports the most features.

## How can the STM be configured? ##
For standard usage this can only be done by setting the GlobalStmInstance. This needs to be done before any transactions are executed. In the future a more advanced mechanism needs to be used (OSGI, system properties etc).

## Does Multiverse depend on instrumentation? ##
No. Using instrumentation can be very useful because the Java code remains very readable. But if you don't want to use it, there is no reason you should. In the org.multiverse.datastructures.ref.manual package you will find references that are instrumented manually. So if you use these implementation in combination with the AtomicTemplate, you don't need to rely on instrumentation.

## Is Multiverse concurrent itself? ##
No. Each transaction is run on completely on a single transaction (however a transaction can be passed from one to another transaction as long as there are no concurrent calls on the transaction). In the future improving the performance of the commit will be done by parallelizing the commit (so acquiring the locks and checking for conflicts.. And another task that can be executed parallel is storing the changes and releasing the locks).

## What about immutable fields of an AtomicObject ##
The AlphaStm completely ignores immutable fields of an AtomicObject so there is no overhead. If an AtomicObject only contains immutable fields, it is completely ignored by the stm. The only thing that does happen is that the methods are made atomic.

## What about static fields? ##
Multiverse completely ignores static fields, so you are on your own.

## What about volatile fields? ##
For multiverse it doesn't matter if a field is or isn't volatile.

## What about transient fields? ##
For multiverse it doesn't matter if a field is or isn't transient.

## What about integration with other JVM languages ##
Although Multiverse is written in Java, it can be combined with other languages. So even if the instrumentation process is not suitable for that specific language, it always is possible to make use of a <a href='http://www.multiversestm.org/~mvn-site/apidocs/org/multiverse/datastructures/refs/manual/Ref.html'>Managed Reference</a> that doesn't rely on instrumentation. Changes on managed references will be atomic and isolated and you can make use of all the features of Multiverse.

## What about integration with Scala? ##
I'm working on integrating Multiverse with Scala, so you can say stuff like:

```
atomic{
   intRef.inc();
}
```

or

```
atomic{
   {  
       stack1.pop();
   }orelse{
       stack2.pop();
   }   
}
```

It would be nice if someone with Scala experience could pick this part up since I only have one day worth of Scala experience.

## What about deadlocks ##
Deadlocks are not possible because waiting for a lock is bounded. So eventually one transaction is going to release its resources so others can complete. If in the future pessimistic locking is added, a deadlock detection mechanism will be added as well.

## What about livelocks ##
Livelocks certainly are possible. The AlphaStm tracks livelocking transactions, but this is not done on a transaction level. This will be added in the future.

## What about starvation ##
No special precautions have been made to protect against starvation.