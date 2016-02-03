## Performance impact ##
The STM uses instrumentation to make sure that a 'normal' POJO can participate in the STM. It is going to add some extra logic to some reads/writes of member fields of the STM object in some cases (scroll down for more info).

### Performance impact other stm references ###
There is a performance impact on STM objects. If the STM object is lazy loaded, an extra check is needed to see if the object already is loaded, and if it isn't it needs to be loaded. Once it is loaded, only the check needs to be done every time the reference is touched. It also means that getters and setters are not likely to be replaced by direct field access by the JIT-compiler.

todo: remark about truly immutable STM objects and optimizations.

### Performance impact non STM reference ###
There is no performance impact on references to other non STM objects. The reference itself will be stored inside the stm when the object is committed. It also means that non stm object is not under control of the STM and that it could be subject to all classic concurrency problems. This technique is however useful for message passing solutions where objects will be touched by only 1 thread at any given moment. The STM could be seen as an exchange mechanism between threads and you don't want the STM to have impact on each and every object.

### Performance impact primitive fields ###
Primitive fields on objects that participate in the stm are completely untouched, so there is no performance overhead. The compiler can do all optimizations (like removing getters and setters and replacing it by direct field access) as it always does.