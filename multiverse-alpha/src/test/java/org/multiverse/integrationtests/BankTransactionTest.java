package org.multiverse.integrationtests;

import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;

/**
 * The BankTransactionTest is a test where money is transfered from one account to another. In the beginning there is
 * one account with money. Each account has a thread. If no money is on an account, the thread is dorment. If money is
 * on the account, money is transfered from one to another.
 *
 * @author Peter Veentjer.
 */
public class BankTransactionTest {

    @Test
    public void test() {
        testIncomplete();
    }
}
