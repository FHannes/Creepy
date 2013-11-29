package net.fhannes.creepy;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public final class CreepyURL {

    private URL url;

    public CreepyURL(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            this.url = null;
        }
    }

    @Override
    public String toString() {
        return (!isValid() ? "" : new StringBuilder(url.getProtocol().toLowerCase()).
                append("://").
                append(url.getHost().toLowerCase()).
                append(url.getPath()).
                toString());
    }

    public boolean isValid() {
        return url != null;
    }

    public boolean hasParams() throws Exception {
        if (url == null)
            throw new Exception();
        return url.getQuery() != null;
    }

    // TODO: Handle invalid relPath data
    public CreepyURL makeRelative(String relPath) {
        relPath = relPath.replaceFirst("\\A\\.?/", "");
        String path = url.getPath();
        int index;
        if ((index = path.lastIndexOf('/')) != -1)
            path = path.substring(0, index);
        while (relPath.startsWith("../")) {
            if ((index = path.lastIndexOf('/')) != -1)
                path = path.substring(0, index);
            else
                return null;
            relPath = relPath.substring(3);
        }
        path = new StringBuilder(path).append('/').append(relPath).toString();
        return new CreepyURL(new StringBuilder(url.getProtocol().toLowerCase()).
                append("://").
                append(url.getHost()).
                append(path).
                toString());
    }

    public URL getURL() {
        return url;
    }

    public static boolean isRelative(String url) {
        return url.matches("(?s)\\A\\.{0,2}/.*");
    }

    /**
     * Exception thrown when the url is invalid.
     */
    public class Exception extends CreepyException {

        public Exception() {
            super("Invalid URL");
        }

    }

}
