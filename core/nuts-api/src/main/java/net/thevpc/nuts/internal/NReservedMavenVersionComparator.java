package net.thevpc.nuts.internal;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.internal.artifact.NVersionImpl;
import net.thevpc.nuts.internal.artifact.NVersionPartImpl;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * NReservedMavenVersionComparator class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NReservedMavenVersionComparator implements NVersionComparator {
    public static final NVersionComparator INSTANCE = new NReservedMavenVersionComparator();

    @Override
    public int compare(NVersion version1, NVersion version2) {
        if (version1 == null) {
            version1 = NVersion.BLANK;
        }
        if (version2 == null) {
            version2 = NVersion.BLANK;
        }
        String v1 = NStringUtils.strip(version1.value());
        String v2 = NStringUtils.strip(version2.value());
        if (v1.equals(v2)) {
            return 0;
        }
        List<NVersionPart> p1 = version1 instanceof NVersionImpl
                ? ((NVersionImpl) version1).comparisonParts()
                : normalizeForComparison(version1.parts());
        List<NVersionPart> p2 = version2 instanceof NVersionImpl
                ? ((NVersionImpl) version2).comparisonParts()
                : normalizeForComparison(version2.parts());
        return compareNormalizedVersionParts(p1, p2);
    }

    /**
     * Compare version parts.
     *
     * @param v1 v1
     * @param v2 v2
     * @return compare version parts result
     */
    public int compareVersionParts(List<NVersionPart> v1, List<NVersionPart> v2) {
        return compareNormalizedVersionParts(
                normalizeForComparison(v1),
                normalizeForComparison(v2));
    }

    private int compareNormalizedVersionParts(List<NVersionPart> v11, List<NVersionPart> v22) {
        int i = 0;
        int j = 0;
        while (i < v11.size() || j < v22.size()) {
            if (i < v11.size() && j < v22.size()) {
                NVersionPart a = v11.get(i);
                NVersionPart b = v22.get(j);
                int r = compareTo(a, b);
                if (r != 0) {
                    return r;
                }
                i++;
                j++;
            } else if (i < v11.size()) {
                NVersionPart a = v11.get(i);
                if (a.type() == NVersionPartType.QUALIFIER || a.type() == NVersionPartType.SUFFIX) {
                    if (a.value().equalsIgnoreCase("final") && i == v11.size() - 1) {
                        return 0;
                    } else {
                        switch (a.value().toLowerCase()) {
                            case "a":
                            case "alpha":
                            case "b":
                            case "beta":
                            case "m":
                            case "milestone":
                            case "rc":
                            case "cr":
                            case "snapshot":
                                return -1;
                            default:
                                return 1;
                        }
                    }
                }
                return 1;
            } else {
                NVersionPart b = v22.get(j);
                if (b.type() == NVersionPartType.QUALIFIER || b.type() == NVersionPartType.SUFFIX) {
                    if (b.value().equalsIgnoreCase("final") && j == v22.size() - 1) {
                        return 0;
                    } else {
                        switch (b.value().toLowerCase()) {
                            case "a":
                            case "alpha":
                            case "b":
                            case "beta":
                            case "m":
                            case "milestone":
                            case "rc":
                            case "cr":
                            case "snapshot":
                                return 1;
//                            case "":
//                            case "ga":
//                            case "final":
//                            case "sp":
//                            case "latest":
//                            case "release":
//                                return -1;
                            default: {
                                return -1;
                            }
                        }
                    }
                }
                return -1;
            }
        }
        return 0;
    }

    /**
     * Normalize for comparison.
     *
     * @param v1 v1
     * @return normalize for comparison result
     */
    public static List<NVersionPart> normalizeForComparison(List<NVersionPart> v1) {
        List<NVersionPart> ret = new ArrayList<>(v1);
        for (int i = ret.size() - 1; i >= 0; i--) {
            NVersionPart p = ret.get(i);
            if (p.type() == NVersionPartType.SEPARATOR) {
                ret.remove(i);
            } else if ((p.type() == NVersionPartType.QUALIFIER || p.type() == NVersionPartType.SUFFIX)
                    && ("final".equalsIgnoreCase(p.value()) || "ga".equalsIgnoreCase(p.value()))) {
                ret.remove(i);
            } else if (p.type() == NVersionPartType.NUMBER) {
                if (isZeroDigits(p.value())) {
                    if (i == ret.size() - 1 || (i < ret.size() - 1 && ret.get(i + 1).type() != NVersionPartType.NUMBER)) {
                        ret.remove(i);
                    }
                }
            } else if (p.type() == NVersionPartType.SUFFIX || p.type() == NVersionPartType.PREFIX) {
                //change them in qualifiers
                ret.set(i, new NVersionPartImpl(p.value(), NVersionPartType.QUALIFIER));
            }
        }
        return ret;
    }

    private static boolean isZeroDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    private static int compareNumber(String a, String b) {
        int ai = 0;
        while (ai < a.length() - 1 && a.charAt(ai) == '0') {
            ai++;
        }
        int bi = 0;
        while (bi < b.length() - 1 && b.charAt(bi) == '0') {
            bi++;
        }
        int al = a.length() - ai;
        int bl = b.length() - bi;
        if (al != bl) {
            return al < bl ? -1 : 1;
        }
        for (int i = 0; i < al; i++) {
            char ca = a.charAt(ai + i);
            char cb = b.charAt(bi + i);
            if (ca != cb) {
                return ca < cb ? -1 : 1;
            }
        }
        return 0;
    }


    /**
     * Compare to.
     *
     * @param v1 v1
     * @param v2 v2
     * @return compare to result
     */
    public int compareTo(NVersionPart v1, NVersionPart v2) {
        if (v1.equals(v2)) {
            return 0;
        }
        if (v1.type() == NVersionPartType.SEPARATOR && v2.type() == NVersionPartType.SEPARATOR) {
            //a dash usually precedes a qualifier, and is always less important than something preceded with a dot.
            if (v1.value().equals("-")) {
                return -1;
            } else {
                return 1;
            }
        } else if (v1.type() == NVersionPartType.SEPARATOR) {
            return -1;
        } else if (v2.type() == NVersionPartType.SEPARATOR) {
            return 1;
        }

        if (v1.type() == NVersionPartType.NUMBER && v2.type() == NVersionPartType.NUMBER) {
            return compareNumber(v1.value(), v2.value());
        } else if (v1.type() == NVersionPartType.NUMBER) {
            return 1;
        } else if (v2.type() == NVersionPartType.NUMBER) {
            return -1;
        } else {
            if (v1.type() == NVersionPartType.QUALIFIER && v2.type() == NVersionPartType.QUALIFIER) {
                //both are string...
                Integer q1 = getKnownQualifierIndex(v1.value());
                Integer q2 = getKnownQualifierIndex(v2.value());
                if (q1 != null && q2 != null) {
                    return q1.compareTo(q2);
                } else if (q1 != null) {
                    return -1;
                } else if (q2 != null) {
                    return 1;
                } else {
                    return v1.value().compareToIgnoreCase(v2.value());
                }
            } else {
                return v1.value().compareToIgnoreCase(v2.value());
            }
        }
    }

    /**
     * Returns the known qualifier index.
     *
     * @param v1 v1
     * @return get known qualifier index result
     */
    private static Integer getKnownQualifierIndex(String v1) {
        switch (v1.toLowerCase()) {
            case "a":
            case "alpha":
                return 1;
            case "b":
            case "beta":
                return 2;
            case "m":
            case "milestone":
                return 3;
            case "rc":
            case "cr":
                return 4;
            case "snapshot":
                return 5;
            case "":
            case "ga":
            case "final":
                return 6;
            case "sp":
                return 7;
            case "release":
                return Integer.MAX_VALUE - 1;
            case "latest":
                return Integer.MAX_VALUE;
        }
        return null;
    }
}
