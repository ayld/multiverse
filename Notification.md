

# Notification: retry #

Just as with traditional lock based approaches, notification between transactions can be very useful (e.g. to wake up when an item has been placed on a queue). But unlike the explicit condition variables in lock based approach, STM's can do this completely behind the screens. At the moment Multiverse only supports one construct: the retry (a static method from StmUtils).

### Example ###
A latch is a structure that can be seen as a door that is closed. As long as the latch is closed, a thread that wants to enter the door needs to wait until it is opened. Once the Latch is opened, it can never be closed and the waiting threads are woken up. If the latch is open, each thread can pass the latch.


Example in old school Java:

```
public class Latch{
   private volatile isOpen = false;

   public boolean isOpen(){
      return isOpen;
   }

   public void open(){
       if(isOpen)
          return;

       synchronized(this){
           isOpen = true;
           notifyAll();
       }
   }

   public void await()throws InterruptedException{
       if(isOpen){
           return;
       }

       synchronized(this){
           while(!isOpen)
              wait();
       }
   }
}
```

I won't provide the same example build the java.util.concurrent.locks.Lock, because that code is even more horrific.

The same example in Multiverse:
```
@AtomicObject
public class Latch{
   private isOpen = false;

   public boolean isOpen(){
      return isOpen;
   }

   public void open(){
      isOpen = true;       
   }

   public void await(){
       if(!isOpen){
           retry();
       }
   }
}
```

As you can see there is no concurrency logic here. The retry aborts the current transaction (all changes won't be committed) and registers itself as a listener to all (non immutable) AtomicObjects it has read.

Another difference is that the while loop in the await from the first version has been replaced by an if statement. If you want to use a condition variable, the Java says that you should be prepared to deal with spurious wakeups. This is why the condition needs to be checked in a loop. With Multiverse this is not needed (todo: explanation).

## Waiting on multiple condition ##

With traditional lock based approaches, it is very hard to wait on multiple waitsets. So if you have 2 stacks for example, and you want to wake up when an item is placed on one of those stacks, you have a hard problem to solve. This is caused by the fact that a  stack has its own lock and own waitset and a thread can also be placed in a single waitset. So the thread is either waiting in the waitset from stack1 (and ignoring notifications in the waitset of stack2) or the other way around.

With an STM this is not needed and you could say something like this:

```
E pop(){
    if(!stack1.isEmpty())
        stack1.pop();
    if(!stack2.isEmpty())
        stack2.pop();
    retry();
}
```

If no items is available on the stacks, the transaction is retried. And since the transaction knows which objects are able to change (the ones you have read), it registers itself as a listener to these structures. And as soon as a change is made on one of the two stacks, the transaction is retried.

# Planned: timeout #
At the moment there is no support for time-outs; so waiting for some event to happen or until a time-out occurs. But this is going to be added. And instead of placing it at the condition variable as can be seen with traditional lock based approaches, this time-out is going to be stored the transaction. This time-out value can be used over multiple condition variables, so no need to specify it again and again.

# Planned: orelse #

Support for the orelse is planned. The orelse is a 'construct' that makes it possible to try an alternative path if some path can't complete.

Example of an orelse:

```
E popFromSomeStack(){
   {
      return stack1.pop();
   }orelse{
      return stack2.pop();
   }
}
```

If the stack1.pop fails with an retry, the path containing stack2 can be tried. If you are lucky stack2 contains an item. And if you are unlucky stack2 doesn't contain an item and the transaction needs to be retried as soon as either stack1 or stack2 has change.

There are 2 problems that need to be solved with the orelse implementation:
  * The limitations of the Java language makes it hard to create a syntax friendly version of the orelse. Good idea's are appreciated.
  * Changes made in one path within a transaction are not rolled back. So for the orelse a new transaction is needed that knows to execute the second path. So there is quite some overhead.

A version that causes less overhead but doesn't roll back changes and doesn't have a very nice syntax could be created like this:

```
try{
    return stack1.pop();
}catch(RetryError e){
    return stack2.pop();
}
```

This works because a retry() only throws a RetryError and doesn't change any state. But isn't very pretty.