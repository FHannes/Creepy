package net.fhannes.creepy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class URLUtils {

    private static final Pattern URL_PATTERN = Pattern.compile("\\b(https?)://([-A-Z0-9.]+)(/[-A-Z0-9+&@#/%=~_|!:,.;]*)?(\\?[A-Z0-9+&@#/%=~_|!:,.;]*)?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public static String normalizeURL(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if (!matcher.matches() || matcher.group(4) != null) // TODO: Filter params rather than ignore page with params
            return "";
        else {
            return new StringBuilder().
                    append(matcher.group(1).toLowerCase()). // Protocol
                    append("://").
                    append(matcher.group(2).toLowerCase()). // Domain
                    append(matcher.group(3)).               // Path
                    toString();
        }
    }

}
