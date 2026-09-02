package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionPart;
import net.thevpc.nuts.artifact.NVersionPartType;
import net.thevpc.nuts.io.NPathExtensionType;
import net.thevpc.nuts.io.NPathNameParts;

import java.util.*;

public class NPathNamePartsUtils {
    private static final Set<String> SMART_COMPRESSION_EXTENSIONS = new HashSet<>(Arrays.asList(
            "gz", "bz2", "xz", "lz", "lzma", "zst", "z", "br"
    ));

    public static NPathNameParts getSmartFileNameParts(String name) {
        String n = name;
        int li = n.indexOf('.');
        if (li < 0) {
            return new NPathNameParts(n, "", "", NPathExtensionType.SMART);
        }
        List<NVersionPart> vals = NVersion.get(n).get().parts();
        int lastDot = -1;
        for (int i = vals.size() - 1; i >= 0; i--) {
            NVersionPart v = vals.get(i);
            String u = v.value();
            if (u.equals(".")) {
                if (i == vals.size() - 1) {
                    return rebuildSmartParts(vals, i);
                }
                NVersionPart v2 = vals.get(i + 1);
                if (v2.type() == NVersionPartType.NUMBER) {
                    if (i > 0 && vals.get(i - 1).type() == NVersionPartType.NUMBER) {
                        if (i + 1 == vals.size() - 1) {
                            return rebuildSmartParts(vals, i + 2);
                        }
                    }
                }
                if (lastDot == -1) {
                    lastDot = i;
                } else {
                    break;
                }
            }
        }
        NPathNameParts result = (lastDot < 0)
                ? new NPathNameParts(n, "", ".", NPathExtensionType.SMART)
                : rebuildSmartParts(vals, lastDot);
        return mergeCompressionWrapper(result);
    }

    // merges a leading extension into the outer one when the outer extension
// is a known compressor and the preceding segment looks like a real
// (non-numeric) extension, e.g. "archive.tar.gz" -> ext="tar.gz",
// "data.csv.gz" -> ext="csv.gz", but "release-1.2.gz" stays ext="gz".
    private static NPathNameParts mergeCompressionWrapper(NPathNameParts result) {
        String ext = result.extension();
        if (ext.isEmpty() || !SMART_COMPRESSION_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            return result;
        }
        String base = result.baseName();
        int i = base.lastIndexOf('.');
        if (i <= 0) {
            return result;
        }
        String inner = base.substring(i + 1);
        if (inner.isEmpty() || !isAlpha(inner)) {
            return result;
        }
        return NPathNameParts.ofSmart(base.substring(0, i), inner + "." + ext);
    }

    public static NPathNameParts getLongFileNameParts(String name) {
        String n = name;
        int i = n.indexOf('.');
        if (i < 0) {
            return new NPathNameParts(n, "", "", NPathExtensionType.LONG);
        }
        return new NPathNameParts(n.substring(0, i), n.substring(i + 1), n.substring(i), NPathExtensionType.LONG);
    }

    public static NPathNameParts getShortFileNameParts(String name) {
        String n = name;
        int i = n.lastIndexOf('.');
        if (i < 0) {
            return new NPathNameParts(n, "", "", NPathExtensionType.SHORT);
        }
        return new NPathNameParts(n.substring(0, i), n.substring(i + 1), n.substring(i), NPathExtensionType.SHORT);
    }

    private static boolean isAlpha(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isLetter(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static NPathNameParts rebuildSmartParts(List<NVersionPart> vals, int split) {
        String fe = concatSmartParts(vals, split, vals.size());
        String e = fe.startsWith(".") ? fe.substring(1) : fe;

        return new NPathNameParts(
                concatSmartParts(vals, 0, split),
                e,
                fe,
                NPathExtensionType.SMART
        );
    }

    private static String concatSmartParts(List<NVersionPart> vals, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(vals.get(i).value());
        }
        return sb.toString();
    }

}
