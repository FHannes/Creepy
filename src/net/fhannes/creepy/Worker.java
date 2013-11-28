package net.fhannes.creepy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Worker implements Runnable {

    private static final Pattern pHtmlParse = Pattern.compile("<\\s*a.*?href\\s*=\\s*\"([^\"#?]*).*?\">",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final CreepyURL url;

    private final ICreepyURLEvent evtDeleteURL;

    public Worker(CreepyURL url, ICreepyURLEvent deleteURL) throws MalformedURLException {
        this.url = url;
        this.evtDeleteURL = deleteURL;
    }

    @Override
    public void run() {
        try {
            URLConnection conn = url.getURL().openConnection();
            if (conn.getContentType().toLowerCase().startsWith("html/text")) {
                Matcher matchLinks = pHtmlParse.matcher(conn.getContent().toString());
                // TODO: Implement html-aware link parser?
            } else
                evtDeleteURL.run(url);
        } catch (IOException e) { }
    }

}
