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
            this.url = new URL(url.replaceFirst("/\\z", ""));
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CreepyURL)
            return false;
        return ((CreepyURL) obj).toString().equalsIgnoreCase(toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean isValid() {
        return url != null;
    }

    public boolean hasParams() throws Exception {
        if (url == null)
            throw new Exception("Invalid URL argument");
        return url.getQuery() != null;
    }

    public URL getURL() {
        return url;
    }

    public static boolean isRelative(String url) {
        return url.matches("(?s)\\A\\.{0,2}/.*");
    }

    public class Exception extends CreepyException {

        public Exception(String message) {
            super(message);
        }
    }

}
