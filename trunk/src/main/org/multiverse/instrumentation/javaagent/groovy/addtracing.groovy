import org.multiverse.api.Transaction

/**
 * The add tracing is responsible for including detailed tracing information about
 * transactions. So that information can be traced like reads/writes/starts/commits/aborts etc.
 * This is not something you normally want to have in your code, but it is very useful if
 * you can inject it temporary for debugging purposes.
 *
 * todo:
 * look for calls to transaction metods: aborts, commits, reads/writes etc
 */
/**
 * Phase 1 is responsible for modifying all access to materialized objects.
 *
 */
TransformClass {


  on_INVOKE_SPECIAL(Transaction.class) {
    // the logic needs to be placed
  }


}