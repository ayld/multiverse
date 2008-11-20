package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.StmException;

/**
 * An {@link StmException} that indicates that an action is done (write, delete, read) on an already deleted
 * cell.
 *
 * @author Peter Veentjer
 */
public class CellDeletedException extends StmException{

}
