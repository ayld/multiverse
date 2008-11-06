package org.codehaus.multiverse.multiversionedstm2;

/**
 * De Snapshot moet eenvoudig over een andere snapshot heengelegd kunnen worden.
 * Je wilt alleen de delta's tov de vorige snapshot in de snapshot opnemen.
 *
 *
 */
public class HeapSnapshot {

    private final long version;

    public HeapSnapshot(){
        version = 0;
    }
    
    public HeapSnapshot(long version) {
        this.version = version;
    }

    public long getVersion(){
        return version;
    }

    public DehydratedCitizen read(long ptr) {
        throw new RuntimeException();
    }
}
