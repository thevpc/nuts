package net.thevpc.nuts.runtime.standalone.text.highlighter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class NCodeHighlighterHelper {
    public static Set<String> loadNames(String type, Class clz) {
        Set<String> reservedWords = new LinkedHashSet<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(clz.getResourceAsStream("/net/thevpc/nuts/runtime/highlighter/" + type)))) {
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
