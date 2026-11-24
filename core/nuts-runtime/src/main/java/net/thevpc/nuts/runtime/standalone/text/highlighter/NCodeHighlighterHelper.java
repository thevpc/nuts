package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class NCodeHighlighterHelper {
    /**
     * Shared implementation for the static {@code getScore} methods of small
     * highlighters. Returns default score when the criteria is null or matches
     * one of the given aliases, unsupported otherwise.
     */
    public static int score(NScorableContext context, String... aliases) {
        if (context == null) {
            return NScorable.DEFAULT_SCORE;
        }
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        for (String alias : aliases) {
            if (alias.equals(s)) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    public static Set<String> loadNames(String type, Class clz) {
        Set<String> reservedWords = new LinkedHashSet<>();
        String path = "/net/thevpc/nuts/runtime/highlighter/" + type;
        InputStream stream = clz.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("missing highlighter keyword resource: " + path);
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(stream))) {
            String line = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && line.charAt(0) != '#') {
                    reservedWords.add(line);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return reservedWords;
    }
}
