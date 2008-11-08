package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.IllegalPointerException;
import org.codehaus.multiverse.IllegalVersionException;
import org.codehaus.multiverse.util.CheapLatch;
import org.codehaus.multiverse.util.Latch;
import static org.codehaus.multiverse.util.PtrUtils.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wakker maken thread via een latch.
 * <p/>
 * Opruimen van latches voor cellen waar je niet bent wakker gemaakt. Als een transactie 2 cellen heeft gelezen
 * en wil een retry doen, zal het op deze 2 cellen gaan slapen. Je zou dan 1 latch kunnen gebruiken die je op
 * beide cellen gaat zetten. Zo gauw er een change is, dan wordt de latch geopend en kan de transactie verder.
 * De vraag is wat te doen met de latch. Bij de cell waarop de change heeft plaatsgevonden, kan de latch direct
 * verwijderd worden. Bij de cell(len) waarop de change niet heeft plaatsgevonden, zit dus met een latch die
 * al wel is afgegaan. Dit is dus een potentieele memory leak. Welke oplossingen kunnen hiervoor bedacht worden?
 * -Je zou in een map alle cellen bij kunnen houden waarop een transactie wacht en daarmee de ongebruikte latches
 * in de heap traceren en verwijderen.
 * -Je zou een soort garbage collection in kunnen bouwen op een seperate thread(s)
 * -Je zou een soort garbage collection in kunnen bouwen op threads die gebruik maken van de heap.
 * -wrap the latch in een weak reference. Als de transactie er mee klaar is, dan is er niemand die nog naar de
 * latch refereerd. Je zit dan nog wel met de weak references zelf die ook weer opgeruimd moeten worden uit de list.
 * Dus eigelijk verplaats je het probleem alleen maar.
 * - bij het afgaan van de latch, de addressen, die zijn toegevoegd aan de latch, registeren bij een opschoon
 * thread. Eventueel dit laten verlopen via de aanroepende (transactie) thread?  Dan ruimte een thread zijn eigen
 * troep weer op.
 * <p/>
 * todo, aan de cellen de listeners koppelen? Wat betekend zo'n listener?
 * Je bent geinteresseerd om te luisteren vanaf een bepaald punt in de tijd. Alle versies voor dat tijdstip
 * zijn niet relevant. De versie versie van die transactie zelf is ook neit interessant, maar alle versies
 * die daarna komen zijn potentieel wel interessant. Op het moment dat er een change is geweest, moeten alle
 * luisterende transacties wakker gemaakt worden.
 * <p/>
 * Kan het voorkomen dat verschillende versies van dezelfde cell
 */
public final class MultiversionedHeap<E> {

    //statistics
    private final AtomicLong readCount = new AtomicLong();
    private final AtomicLong writeCount = new AtomicLong();

    private final ConcurrentMap<Long, MultiversionedCell<E>> cells = new ConcurrentHashMap<Long, MultiversionedCell<E>>();

    private long firstFreeAdress = 1;

    /**
     * Returns the number of writes that have been executed.
     *
     * @return the number of writes that have been executed.
     */
    public long getWriteCount() {
        return writeCount.longValue();
    }

    /**
     * Returns the number of reads that have happened.
     *
     * @return the number of reads that have happened.
     */
    public long getReadCount() {
        return readCount.longValue();
    }

    /**
     * Returns the number of cells allocated in this heap.
     * <p/>
     * Method can be called all the time.
     *
     * @return  the number of cells in this heap.
     */
    public int getCellCount() {
        return cells.size();
    }

    /**
     * Returns the number of versions in each cell and adds them. It gives a rough
     * indication of the size of the heap.
     *
     * @return
     */
    public int getVersionCount() {
        int count = 0;
        for (MultiversionedCell cell : cells.values())
            count += cell.getNumberOfVersions();

        return count;
    }

    /**
     * Returns the number of versions a cell has.
     * <p/>
     * Method can be called all the time.
     *
     * @param address
     * @return
     * @throws IllegalPointerException if cell doesn't exist.
     */
    public int getVersionCount(long address) {
        MultiversionedCell cell = cells.get(address);
        if (cell == null)
            throw new IllegalPointerException(address);
        return cell.getNumberOfVersions();
    }

    /**
     * Returns the current number of MultiversionedCells.
     *
     * @return the current number of MultiversionedCells.
     */
    public int getHeapSize() {
        int result = 0;
        for (MultiversionedCell cell : cells.values())
            result += cell.getTotalSize();
        return result;
    }

    /**
     * Method should not be called concurrently.
     * <p/>
     * todo: happens before.
     *
     * @return
     */
    public long createHandle() {
        long result = firstFreeAdress;
        firstFreeAdress++;
        return result;
    }

    /**
     * @param version
     * @param addresses
     * @return
     */
    public Latch listen(long[] addresses, long version) {
        checkVersion(version);

        Latch latch = new CheapLatch();
        for (long address : addresses) {
            MultiversionedCell<E> cell = cells.get(address);
            cell.listen(version, latch);
            //if the latch already is opened, we are done.
            if (latch.isOpen())
                return latch;
        }

        return latch;
    }

    /**
     * Reads the content of a specific memory cell.
     *
     * @param ptr     the pointer to the cell.
     * @param version the version of the cell. If a cell with the wanted version is not found, a cell with
     *                an older version are used (the least old version).
     * @return
     * @throws IllegalPointerException if pointer points to a non existing address.
     * @throws IllegalVersionException if the version is not valid, or if no cell with the desired version (or older)
     *                                 is found.
     */
    public E read(long ptr, long version) {
        checkPtrAndVersion(ptr, version);

        readCount.incrementAndGet();

        MultiversionedCell<E> cell = cells.get(ptr);
        if (cell == null)
            throw new IllegalPointerException(ptr);

        return cell.getValue(version);
    }


    /**
     * Returns the actual version of the cell.
     *
     * @param ptr the pointer to the cell.
     * @return the version of the last write to the specific cell. If no writes have been made, -1 is returned.
     * @throws IllegalPointerException if the ptr points to a non existing cell.
     */
    public long getActualVersion(long ptr) {
        checkPtr(ptr);

        MultiversionedCell cell = cells.get(ptr);
        return cell == null ? -1 : cell.getActiveVersion();
    }

    /**
     * This method should only be called by a single thread. It can be called parallel to
     * the other methods of this heap.
     *
     * @param ptr
     * @param version
     * @param content
     * @throws NullPointerException    if content is null
     * @throws IllegalPointerException if ptr is smaller than 1.
     * @throws IllegalVersionException
     */
    public void write(long ptr, long version, E content) {
        checkPtrAndVersion(ptr, version);
        if (content == null) throw new NullPointerException();

        MultiversionedCell<E> cell = cells.get(ptr);
        if (cell == null) {
            //the cell does not exist yet, so create it and store it.
            cell = new MultiversionedCell<E>(content, version);
            cells.put(ptr, cell);
        } else {
            //the cell exist, so we can continue and do the write.
            cell.write(version, content);
        }

        writeCount.incrementAndGet();
    }

    /**
     * Prunes each cell in the heap. It removes all old versions that are not needed anymore.
     *
     * @param minimalVersion
     */
    public void prune(long minimalVersion) {
        assert minimalVersion >= 0;

        for (MultiversionedCell cell : cells.values())
            cell.prune(minimalVersion);
    }
}
