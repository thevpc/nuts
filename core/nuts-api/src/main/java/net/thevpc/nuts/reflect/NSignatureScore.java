package net.thevpc.nuts.reflect;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;

public final class NSignatureScore implements Comparable<NSignatureScore> {
    private final int nameDistance;
    private final int typeDistance;

    public static final NSignatureScore EXACT = new NSignatureScore(0, 0);
    public static final NSignatureScore NO_MATCH = new NSignatureScore(Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static NSignatureScore of(int nameDistance, int typeDistance) {
        if (nameDistance < 0) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid score %s", nameDistance));
        }
        if (typeDistance < 0) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid score %s", typeDistance));
        }
        if (nameDistance == Integer.MAX_VALUE && typeDistance == Integer.MAX_VALUE) {
            return NO_MATCH;
        }
        if (nameDistance == 0 && typeDistance == 0) {
            return EXACT;
        }
        return new NSignatureScore(nameDistance, typeDistance);
    }

    private NSignatureScore(int nameDistance, int typeDistance) {
        this.nameDistance = nameDistance;
        this.typeDistance = typeDistance;
    }

    public boolean isMatch() {
        return nameDistance != Integer.MAX_VALUE && typeDistance != Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(NSignatureScore o) {
        int cmp = Integer.compare(this.nameDistance, o.nameDistance);
        if (cmp != 0) return cmp;
        return Integer.compare(this.typeDistance, o.typeDistance);
    }

    @Override
    public String toString() {
        if (nameDistance == Integer.MAX_VALUE || typeDistance == Integer.MAX_VALUE) {
            return "no-match";
        }
        if (nameDistance == 0 && typeDistance == 0) {
            return "exact";
        }
        return "match(name=" + nameDistance + ", type=" + typeDistance + ")";
    }
}