package net.fhannes.creepy;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreepyJob {

    private final long id;
    private final CreepyURL url;
    private boolean finished = false;

    private final Set<CreepyURL> urls = new HashSet<CreepyURL>();

    public CreepyJob(long id, CreepyURL url) {
        this.id = id;
        this.url = url;
    }

    public long getID() {
        return id;
    }

    public CreepyURL getURL() {
        return url;
    }

    public void addURL(CreepyURL url) {
        urls.add(url);
    }

    public void finish() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public Iterator<CreepyURL> urlIterator() {
        return urls.iterator();
    }

}
