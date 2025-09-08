package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NStringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MavenNVersionComparator implements NVersionComparator {
    public static final NVersionComparator INSTANCE = new MavenNVersionComparator();

    @Override
    public int compare(NVersion version1, NVersion version2) {
        if (version1 == null) {
            version1 = NVersion.BLANK;
        }
        if (version2 == null) {
            version2 = NVersion.BLANK;
        }
        String v1 = NStringUtils.trim(version1.getValue());
        String v2 = NStringUtils.trim(version2.getValue());
        if (v1.equals(v2)) {
            return 0;
        }
        return compareVersionParts(
                version1.parts(),
                version2.parts()
        );
    }

    public int compareVersionParts(List<NVersionPart> v1, List<NVersionPart> v2) {
        int i = 0;
        int j = 0;
        List<NVersionPart> v11 = normalizeForComparison(v1);
        List<NVersionPart> v22 = normalizeForComparison(v2);
        while (i < v11.size() || j < v22.size()) {
            if (i < v11.size() && j < v22.size()) {
                NVersionPart a = v11.get(i);
                NVersionPart b = v22.get(i);
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
                NVersionPart b = v22.get(i);
                if (b.type() == NVersionPartType.QUALIFIER || b.type() == NVersionPartType.SUFFIX) {
                    if (b.value().equalsIgnoreCase("final") && i == v22.size() - 1) {
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

    private static List<NVersionPart> normalizeForComparison(List<NVersionPart> v1) {
        List<NVersionPart> ret = new ArrayList<>(v1);
        for (int i = ret.size() - 1; i >= 0; i--) {
            if (ret.get(i).type() == NVersionPartType.SEPARATOR) {
                ret.remove(i);
            } else if (ret.get(i).type() == NVersionPartType.QUALIFIER && ("final".equalsIgnoreCase(ret.get(i).value()) || "ga".equalsIgnoreCase(ret.get(i).value()))) {
                ret.remove(i);
            } else if (ret.get(i).type() == NVersionPartType.SUFFIX && ("final".equalsIgnoreCase(ret.get(i).value()) || "ga".equalsIgnoreCase(ret.get(i).value()))) {
                ret.remove(i);
            } else if (ret.get(i).type() == NVersionPartType.NUMBER) {
                if (new BigInteger(ret.get(i).value()).equals(BigInteger.ZERO)) {
                    if (i == ret.size() - 1 || (i < ret.size() - 1 && ret.get(i + 1).type() != NVersionPartType.NUMBER)) {
                        ret.remove(i);
                    }
                }
            } else if (ret.get(i).type() == NVersionPartType.SUFFIX || ret.get(i).type() == NVersionPartType.PREFIX) {
                //change them in qualifiers
                ret.set(i, new DefaultNVersionPart(ret.get(i).value(), NVersionPartType.QUALIFIER));
            }
        }
        return ret;
    }


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
            return new BigInteger(v1.value()).compareTo(new BigInteger(v2.value()));
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
