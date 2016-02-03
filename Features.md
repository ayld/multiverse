# Features #
  * 2 programming models:
    * POJO programming model in combination with annotations/instrumentation.
    * Ref based approach that doesn't rely on instrumentation. It is similar to Clojure its ref based approach.
  * Readonly transactions
  * Multi version concurrency control (the same mechanism Oracle, Postgresql and other databases use). So readers don't block writers, and writers don't block readers. And transactions with non conflicting writes can be committed in parallel.
  * Non managed stm objects can flow through stm space (for example for handing over an object from one thread to another).
  * Transaction level read consistency (the same isolation Oracle provides, so not 100% serialized)
  * No zombie threads; so a thread can't get in an inconsistent state caused by it observing isolation problems and therefore there is no need to 'kill' zombie threads.
  * Multiverse doesn't touch classes from the Java API (unlike Terracotta).
  * Supports the retry  (a thread notification construct).
  * Support for orelse (a thread notification construct).
  * Simple nested transactions

# Planned #
  * Pessimistic locking
  * Deadlock detection for the pessimistic locking
  * Complex nested transactions with partial rollbacks etc
  * Detachment and reattachment of stm objects
  * A distributed version
  * Durable state (state that is persisted to disk for example).
  * Control history length of previous committed data (needed for MVCC).
  * Statistics to design better performing data structures.
  * Complete serialized behaviour instead of the Oracle version
  * Configurable history of old data
  * Transaction level statistics containing information about which transactions are causing problems
  * Class level statistics containing information about which classes are cause problems
  * Compile-time instrumentation
  * Striping: technique to reduce write-conflicts and therefore create better scaling components.