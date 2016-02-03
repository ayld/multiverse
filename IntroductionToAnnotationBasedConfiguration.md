

# Transaction #

A transactional method that can be used for updates, can be created with the AtomicMethod annotation, example:

```
@AtomicMethod
public void transfer(Account from, Account to, int amount){
   from.dec(amount);
   to.inc(amount);
}
```

(where Account is an AtomicObject)

This annotation will make sure that an update transaction is started and committed, or retried when needed.

It is also important to realize that not every method needs to be an AtomicMethod; only the 'outer' methods of your system. Exactly the same as with classic database transactions where, in most cases, you only make the methods of the service layer transactional.

All instance methods of an AtomicObject are by default update-AtomicMethods. If you know that a method is readonly, mark it as readonly. In the future some kind of infer mechanism might be added.

## Readonly transactions ##

A readonly transaction can be created like this

```
@AtomicMethod(readonly = true)
public int sum(Account account1, Account account2){
    return account1.getBalance() + account2.getBalance();
}
```

The advantage of readonly transactions is that a readonly transaction doesn't need to do readtracking, so the size of a transaction will not grow, and the time it takes to commit is constant instead of linear. Also the amount of object creation is a lot smaller, so a readonly transaction is a lot faster.

## Retry Count ##

The number of times a transaction is retried can be set like this (default is Integer.MAX\_VALUE)
```
@AtomicMethod(retryCount = 10)
public int execute(int newAge){
    return person.setAge(newAge)
}
```

The problem of setting the retryCount too low is that you get a TooManyRetriesException, so you need to deal with this. And the problem of setting the retryCount to high is that the system could suffer from livelocking and starvation. For the time being the default works, but in the future this is something needs further inspection.

## Family Name ##
Each transaction can have a family name that is sort of an identifier. This identifier can be used to classify the transaction (and this can be used for various techniques like profiling/determining the optimal transaction size/logging etc). Default the complete classname.methodDescriptor is used as the familyName.

```
@AtomicMethod(familyName = "setAge")
public int execute(int newAge){
    return person.setAge(newAge)
}
```


# Mapping an AtomicObject #

An AtomicObject (an object that lives in STM space) can be configured by placing an AtomicObject annotation, example:

```
@AtomicObject
public class Account{

   private int amount;
   ...
}
```

All reads/writes made in this Account will be handled by the STM. AtomicObjects are allowed to have references to other AtomicObjects, eg:

```
@AtomicObject
public class Person{
   
    private Account account;
    ...
}
```

All changes made on AtomicObject within the context of a transaction, will automatically be persisted when the transaction commits if there are no conflicts. So just as with a database, a lot of responsibilities have shifted from the developer to the environment. And only in corner cases you need to influence the implementation.

If a field is not an atomic object and not a primitive, the reference itself is managed, by the STM, but the object is not. So if you are using a standard java.util.LinkedList for example, you still could have concurrency problems. But this doesn't have to be a bad thing if you can make sure that the object is touched by at most 1 thread at a time. This technique can be used for example to pass a normal pojo through the stm from one thread to another.

# Final fields and AtomicObjects #

Final fields in AtomicObjects are completely ignored by the STM. If an AtomicObject only contains final/excluded fields, the object becomes invisible to the STM (although the methods still are atomic). So immutable datastructures can be perfectly combined with STM techniques. So don't throw immutable datastructures out of the window.

# Excluding fields #

Normally all fields of an object will be stored in STM space. But in some cases it is better to exclude a field. This can be done with the Exclude annotation, example:

```
@AtomicObject
public class Person{

   @Exclude
   private String firstname;

   private String lastname;
 
   ...
}
```

In this case the firstname is ignored and the lastname is not ignored.

The Exclude annotation can be compared to the Transient annotation from JPA.

# Annotations planned #

Annotations planned for future releases:
  1. Lock(mode=LockMode.exclusive), Lock(mode=LockMode.shared)
  1. Eager for eager loading of references when lazy loading is not needed