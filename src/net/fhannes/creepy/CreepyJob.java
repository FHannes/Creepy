package net.fhannes.creepy;

import org.apache.commons.validator.routines.UrlValidator;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreepyJob {

    private final long id;
    private final String url;
    private boolean finished = false;
    private boolean failed = false;

    private final Set<String> urls = new HashSet<String>();

    private UrlValidator validator = new UrlValidator(new String[] {"http", "https"});

    public CreepyJob(long id, String url) {
        this.id = id;
        this.url = url;
    }

    public long getID() {
        return id;
    }

    public String getURL() {
        return url;
    }

    public void addURL(String url) {
        if (validator.isValid(url)) {
            if (url.contains("#"))
                url = url.substring(0, url.indexOf('#'));
            urls.add(URLDecoder.decode(url));
        }
    }

    public void finish() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void fail() {
        failed = true;
    }

    public boolean hasFailed() {
        return failed;
    }

    public Iterator<String> urlIterator() {
        return urls.iterator();
    }

}
