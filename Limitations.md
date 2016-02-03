# List of limitations #

A list of limitations that exist in the current instrumentation:
  * The superclass of an AtomicObject is completely ignored.
  * Constructors in AtomicObjects are not allowed to call this(...)
  * No direct field initialization in @AtomicObject (do everything in constructor)
  * Annotations are not inherited