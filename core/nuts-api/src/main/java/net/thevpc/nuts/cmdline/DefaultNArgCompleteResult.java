package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.boot.NBootCompleteResult;
import net.thevpc.nuts.boot.internal.util.NBootUtils;
import net.thevpc.nuts.collections.NCollections;

import java.util.*;

/**
 * DefaultNArgCompleteResult class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class DefaultNArgCompleteResult implements NArgCompleteResult {
    public static final NArgCompleteResult BLANK=new DefaultNArgCompleteResult(null,null);
    private final List<NArgCompleteCandidate> candidates;
    private final Set<NArgCompleteFlag> flags;

    /**
     * Default n arg complete result.
     *
     * @param candidates candidates
     * @param flags flags
     * @return default n arg complete result result
     */
    public DefaultNArgCompleteResult(Collection<NArgCompleteCandidate> candidates, Collection<NArgCompleteFlag> flags) {
        this.candidates = NCollections.unmodifiableNonNullList(candidates);
        this.flags = NCollections.unmodifiableNonNullSet(flags);
    }

    /**
     * Parse.
     *
     * @param txt txt
     * @return parse result
     */
    public static NArgCompleteResult parse(String txt) {
        ArrayList<NArgCompleteCandidate> candidates = new ArrayList<>();
        ArrayList<NArgCompleteFlag> flags = new ArrayList<>();
        if (txt != null) {
            //remove last newline
            if (txt.endsWith("\r\n")) {
                txt = txt.substring(0, txt.length() - 2);
            } else if (txt.endsWith("\n") || txt.endsWith("\r")) {
                txt = txt.substring(0, txt.length() - 1);
            }
            if (!txt.isEmpty()) {
                List<String> split = NBootUtils.split(txt, "\n\r", false, false);
                int count = split.size();
                for (int i = 0; i < count; i++) {
                    String line = split.get(i);
                    if (i == count - 1) {
                        if (line.startsWith("::")) {
                            for (String s : NBootUtils.split(line.substring(2), ",", true, true)) {
                                NArgCompleteFlag f = null;
                                try {
                                    f = NArgCompleteFlag.valueOf(s.trim().toUpperCase());
                                } catch (Exception e) {
                                    // ignore
                                }
                                if (f != null) {
                                    flags.add(f);
                                }
                            }
                        } else {
                            int t = line.indexOf('\t');
                            if (t >= 0) {
                                candidates.add(NArgCompleteCandidate.of(line.substring(0, t), line.substring(t + 1).trim()));
                            } else {
                                candidates.add(NArgCompleteCandidate.of(line));
                            }
                        }
                    } else {
                        int t = line.indexOf('\t');
                        if (t >= 0) {
                            candidates.add(NArgCompleteCandidate.of(line.substring(0, t), line.substring(t + 1).trim()));
                        } else {
                            candidates.add(NArgCompleteCandidate.of(line));
                        }
                    }
                }
            }
        }
        return NArgCompleteResult.of(candidates, flags);
    }

    @Override
    public List<NArgCompleteCandidate> candidates() {
        return candidates;
    }

    @Override
    public Set<NArgCompleteFlag> flags() {
        return flags;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        net.thevpc.nuts.cmdline.DefaultNArgCompleteResult aDefault = (net.thevpc.nuts.cmdline.DefaultNArgCompleteResult) o;
        return Objects.equals(candidates, aDefault.candidates) && Objects.equals(flags, aDefault.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidates, flags);
    }


    /**
     * Format.
     *
     * @return format result
     */
    public String format() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            NArgCompleteCandidate candidate = candidates.get(i);
            String v = candidate.value();
            String s = candidate.display();
            sb.append(escape(v, true));
            if (!NBootUtils.isBlank(s) && !Objects.equals(s, v)) {
                sb.append("\t").append(escape(s, false));
            }
        }
        if (!flags.isEmpty()) {
            if (!candidates.isEmpty()) {
                sb.append("\n");
            }
            sb.append("::");
            boolean first = true;
            for (NArgCompleteFlag flag : flags) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(flag.name().toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Escape.
     *
     * @param any any
     * @param escapeTab escape tab
     * @return escape result
     */
    private static String escape(String any, boolean escapeTab) {
        StringBuilder sb = new StringBuilder();
        for (char c : any.toCharArray()) {
            switch (c) {
                case '\t': {
                    if (escapeTab) {
                        sb.append("\\t");
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\r': {
                    sb.append("\\r");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isBlank() {
        return candidates.isEmpty() && flags.isEmpty();
    }
}
