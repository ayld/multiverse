package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;

/**
 * Create a number if listen latches and register them by multiple threads.
 * Create a number of version increasing threads.
 * Let the test run from beginVersion to endVersion
 * check that all the latches that are registered including endVersion have been openen 
 */
public class VersionedLatchGroupStressTest extends TestCase {

}
