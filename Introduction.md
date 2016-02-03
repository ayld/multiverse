# Introduction #

Concurrency control is one of the most complex programming tasks and with the increasing number of cores, the need to create additional threads so all resources are used, is going to increase. Lock based concurrency is the oldest form of concurrency control, but still is complex to use. Everybody has heard of the classic problems like race problems and deadlocks, but there are also less known problems like actions not being composable. Writing correct and efficient datastructures using lock based concurrency control remains a very hard task (next to the general concurrency theory, one also needs to know the Memory Model).

Transactional Memory takes a different approach. Instead requiring imperative concurrency control (e.g. explicitly locking resources), declarative concurrency control can be applied through transactions. Essentially you could see Transactional Memory as a very fast in memory database where one is not limited by the data structures (e.g. a b-tree for the index) provided by the database.

## Different forms of Transactional Memory ##

There are different forms of Transactional Memory:
  * **Software Transactional Memory:** all the concurrency control is done in the software. The advantage is there is no limit on the functionality that needs to be added. The disadvantage is that is not as fast as it could be.
  * **Hardware Transactional Memory:** all the concurrency control is done on hardware level. Already a lot of very smart stuff is done using caches, and ones this functionality is extended, the performance of Transactional Memory could improve quite dramatically. The disadvantage is that there is no support for Transactional Memory on modern Intel and AMD CPU's right now. The Sun Niagara III processor has some support, but is not a very main stream CPU.
  * **Hybrids:** combination of Software and Hardware Transactional Memory. The hardware could provide good performing basic primitives and the software could start where the hardware ends.

Multiverse is a Software Transactional Memory implementation, but as soon as the hardware starts to appear, we are going to investigate how to create a better performing hybrid.