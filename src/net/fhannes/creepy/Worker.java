package net.fhannes.creepy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
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
                List<CreepyURL> links = new ArrayList<CreepyURL>();
                while (matchLinks.matches()) {
                    String link = matchLinks.group(1);
                    CreepyURL newURL = null;
                    if (CreepyURL.isRelative(link))
                        newURL = url.makeRelative(link);
                    else
                        newURL = new CreepyURL(link);
                    links.add(newURL);
                }
                // TODO: Implement html-aware link parser?
            } else
                evtDeleteURL.run(url);
        } catch (IOException e) { }
    }

}
