package com.drinker.alock.util.process;

/**
 * Created by zhuolin on 15-8-3.
 */
public class ProcessData implements Comparable<ProcessData> {
    public final static int OOM_DEFAULT = 1000000;
    public final static int OOM_INIT = 0;
    public final static int OOM_UP = OOM_INIT + 1; //OOM 变大
    public final static int OOM_DOWN = OOM_UP + 1; //OOM 变小

    public final static int FORE = 0;

    public final static int BACKGROUND = FORE + 1;

    public final static int UNKNOWN = BACKGROUND + 1;

    public int pid = 0;
    public int ppid = 1;
    public String packageName = "";
    public int oomScore = OOM_DEFAULT;

    public int oomOrientation = OOM_INIT;

    public int oomChangeValue = 0;

    public int status = UNKNOWN;

    @Override
    public int compareTo(ProcessData another) {
        if (oomScore > another.oomScore) {
            return 1;
        } else if (oomScore < another.oomScore) {
            return -1;
        }
        return 0;
    }
}
