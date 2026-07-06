package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensionCatalog;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NScored;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NExtensionCatalogManager {
    private List<NExtensionCatalog> catalogs;

    public List<NId> findExtensions(String type, String pattern) {
        List<NScored<NId>> ok = new ArrayList<>();
        int bestScore = -1;
        for (NExtensionCatalog catalog : catalogs()) {
            for (NScored<NId> e : catalog.findExtensions(type, pattern)) {
                if (e.score() > 0) {
                    if (e.score() > bestScore) {
                        ok.clear();
                        bestScore = e.score();
                    }
                    ok.add(e);
                } else if (e.score() == 0) {
                    ok.add(e);
                }
            }
        }
        return ok.stream().map(NScored::value).collect(Collectors.toList());
    }

    private List<NExtensionCatalog> catalogs() {
        if (catalogs == null) {
            catalogs = new ArrayList<>(NExtensions.of().createAll(NExtensionCatalog.class));
        }
        return catalogs;
    }

    public NOptional<NId> findExtension(String type, String pattern) {
        return NOptional.ofFirst(findExtensions(type, pattern));
    }

    public <T, V> NOptional<T> createSupported(Class<T> type, V supportCriteria, String extensionType, String extensionPattern) {
        T n = NExtensions.of().createSupported(type, supportCriteria).orNull();
        if (n == null) {
            if (loadExtensionFor(extensionType, extensionPattern)) {
                return NExtensions.of().createSupported(type, supportCriteria);
            }
        }
        if (n == null) {
            return NOptional.ofNamedEmpty(NMsg.ofC("missing %s", type));
        }
        return NOptional.of(n);
    }

    public boolean loadExtensionFor(String type, String pattern) {
        NId e = NWorkspaceExt.of().getModel().extensionCatalogManager
                .findExtension(type, pattern).orNull();
        if (e != null) {
            NExtensions.of().loadExtension(e);
            return true;
        }
        return false;
    }

    public static NOptional<NScoredIdMap> loadScoredIds(NPath path) {
        boolean firstLine = true;
        int score = 1;
        Map<String, NId> map = new LinkedHashMap<>();
        try {
            for (String s : path.lines().toList()) {
                s = NStringUtils.strip(s);
                if (!s.isEmpty()) {
                    if (s.startsWith("#")) {
                        if (firstLine) {
                            s = NStringUtils.strip(s.substring(1));
                            Map.Entry<String, String> e = parseEntry(s);
                            if (e != null && e.getKey().equals("score")) {
                                Integer uu = NLiteral.of(e.getValue()).asInt().orNull();
                                if (uu != null && uu >= 0) {
                                    score = uu;
                                }
                            }
                        }
                    } else {
                        Map.Entry<String, String> e = parseEntry(s);
                        if (e != null) {
                            try {
                                NId id = NId.of(e.getValue());
                                if (!id.groupId().isEmpty() && !id.artifactId().isEmpty()) {
                                    map.put(e.getKey(), id);
                                }
                            } catch (Exception ex) {
                                //just ignore
                            }
                        }
                    }
                    firstLine = false;
                }
            }
        } catch (Exception ex) {
            // just ignore
        }
        if (!map.isEmpty()) {
            return NOptional.of(new NScoredIdMap(map, score));
        }
        return NOptional.ofNamedEmpty("scored");
    }

    private static Map.Entry<String, String> parseEntry(String s) {
        int i = s.indexOf('=');
        if (i > 0) {
            String k = NStringUtils.strip(s.substring(0, i));
            String v = NStringUtils.strip(s.substring(i + 1));
            if (isValidKey(k)) {
                return new AbstractMap.SimpleEntry<>(k, v);
            }
        }
        return null;
    }

    private static boolean isValidKey(String k) {
        if (k == null || k.isEmpty()) {
            return false;
        }
        int len = k.length();
        char first = k.charAt(0);
        // first character: must be letter (a-z or A-Z) or maybe underscore? Original disallowed underscore at start, but we'll keep that rule. Actually original disallows start with '_', '-', '.', digit. So only letters allowed? Wait, original allowed letters? It only disallowed digit, ., _, -. So allowed first characters: letters only? Also maybe uppercase/lowercase. So first char must be a letter.
        if (!Character.isLetter(first)) {
            return false;
        }
        char last = k.charAt(len - 1);
        if (last == '.' || last == '_' || last == '-') {
            return false;
        }
        boolean lastWasDot = false;
        for (int i = 0; i < len; i++) {
            char c = k.charAt(i);
            // allowed chars: letter, digit, '.', '_', '-'
            if (!(Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-')) {
                return false;
            }
            if (c == ' ') { // space not allowed (already caught by above, but explicit)
                return false;
            }
            if (c == '.') {
                if (lastWasDot) {
                    return false; // consecutive dots
                }
                lastWasDot = true;
            } else {
                lastWasDot = false;
            }
        }
        return true;
    }
}
