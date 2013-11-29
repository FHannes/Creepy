package net.fhannes.creepy;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CreepyWorker extends CreepyDBAgent implements Runnable {

    private static final Pattern pHtmlParse = Pattern.compile("<\\s*a.*?href\\s*=\\s*\"([^\"#?]*).*?\">",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final CreepyURL url;

    public CreepyWorker(File dbFile, CreepyURL url) throws SQLException, ClassNotFoundException {
        super(dbFile);
        this.url = url;
    }

    @Override
    public void run() {
        try {
            URLConnection conn = url.getURL().openConnection();
            if (conn.getContentType().toLowerCase().startsWith("text/html")) {
                // TODO: Implement html-aware link parser?
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                try {
                    StringBuilder buffer = new StringBuilder(1048576);
                    char[] data = new char[4096];
                    while (br.read(data) != -1)
                        buffer.append(data);
                    Matcher matchLinks = pHtmlParse.matcher(buffer.toString());
                    List<CreepyURL> links = new ArrayList<CreepyURL>();
                    while (matchLinks.find()) {
                        String link = matchLinks.group(1);
                        CreepyURL newURL = null;
                        if (CreepyURL.isRelative(link))
                            newURL = url.makeRelative(link);
                        else
                            newURL = new CreepyURL(link);
                        links.add(newURL);
                    }
                    addURL(links);
                    // TODO: Add links between urls
                    updateLastCheck(url);
                } finally {
                    br.close();
                }
            } else
                deleteURL(url);
        } catch (Exception e) { }
    }

}
