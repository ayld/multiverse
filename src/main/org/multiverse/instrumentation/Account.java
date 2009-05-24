package org.multiverse.instrumentation;

import org.multiverse.api.Transaction;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.api.annotations.TmEntity;

@TmEntity
public class Account {
    private int balance;

    public Account() {

    }

    @Atomic
    public String transferTo() {
        System.out.println("transferTo is called");
        Transaction t = getTransaction();
        return null;
    }

    @Atomic
    public void method2() {
        System.out.println("method2 is called");
    }

    @Atomic
    public String method3(String s) {
        System.out.println("method3 called with argument: " + s);
        return null;
    }

    @Atomic
    public String method4(int s) {
        System.out.println("method4 called with argument: " + s);
        return null;
    }

    @Atomic
    public int method5() {
        System.out.println("method5 is called");
        return 4564;
    }
}
