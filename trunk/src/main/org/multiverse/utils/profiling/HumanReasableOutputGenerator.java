package org.multiverse.utils.profiling;

public class HumanReasableOutputGenerator implements OutputGenerator {

    @Override
    public String toString(ProfileDataRepository profiler) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*
    @Override
    public String getProfileInfo() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<ComposedKey, AtomicLong> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    } */
}
