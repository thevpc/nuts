package net.thevpc.nuts.runtime.standalone.cmdline;

import net.thevpc.nuts.cmdline.NArgCompleteCandidate;
import net.thevpc.nuts.cmdline.NArgCompletePosition;
import net.thevpc.nuts.cmdline.NArgCompleteResult;
import net.thevpc.nuts.cmdline.NArgValueComplete;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MyContext implements NArgValueComplete.Context {
    final String prefix;
    final String suffix;

    /**
     * My context.
     *
     * @param wordStr          word str
     * @param completePosition complete position
     * @return my context result
     */
    public MyContext(String wordStr, NArgCompletePosition completePosition) {
        if (completePosition == null) {
            prefix = "";
            suffix = "";
        } else {
            if (wordStr == null) {
                wordStr = "";
            }
            int offset = completePosition.wordOffset();
            if (offset < 0 || offset > wordStr.length()) {
                // offset unknown or out of range — treat whole word as prefix
                prefix = wordStr;
                suffix = "";
            } else {
                prefix = wordStr.substring(0, offset);
                suffix = wordStr.substring(offset);
            }
        }
    }

    /**
     * My context.
     *
     * @param wordStr word str
     * @param offset  offset
     * @return my context result
     */
    public MyContext(String wordStr, int offset) {
        if (wordStr == null) {
            wordStr = "";
        }
        if (offset < 0 || offset > wordStr.length()) {
            // offset unknown or out of range — treat whole word as prefix
            prefix = wordStr;
            suffix = "";
        } else {
            prefix = wordStr.substring(0, offset);
            suffix = wordStr.substring(offset);
        }
    }

    /**
     * My context.
     *
     * @param prefix prefix
     * @param suffix suffix
     * @return my context result
     */
    public MyContext(String prefix, String suffix) {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix;
    }

    @Override
    public String prefix() {
        return prefix;
    }

    @Override
    public String suffix() {
        return suffix;
    }

    @Override
    public boolean matches(String word) {
        if (word == null) {
            word = "";
        }
        if (!word.startsWith(prefix)) {
            return false;
        }
        if (!suffix.isEmpty()) {
            // Candidate must contain the suffix in the portion AFTER prefix
            String remainder = word.substring(prefix.length());
            return remainder.endsWith(suffix) || remainder.contains(suffix);
        }
        return true;
    }

    /**
     * Matches.
     *
     * @param word word
     * @return matches result
     */
    public boolean matches(NArgCompleteCandidate word) {
        /**
         * Matches.
         *
         * @param word.value() word.value()
         * @return matches result
         */
        return matches(word == null ? "" : word.value());
    }

    @Override
    public NArgCompleteResult filterValues(Stream<String> values) {
        if (values == null) {
            return NArgCompleteResult.ofBlank();
        }
        return NArgCompleteResult.ofSimpleCandidates(values.filter(this::matches).collect(Collectors.toList()));
    }

    @Override
    public NArgCompleteResult filterValues(Collection<String> values) {
        if (values == null) {
            return NArgCompleteResult.ofBlank();
        }
        return NArgCompleteResult.ofSimpleCandidates(values.stream().filter(this::matches).collect(Collectors.toList()));
    }

    @Override
    public NArgCompleteResult filterCandidates(Stream<NArgCompleteCandidate> values) {
        if (values == null) {
            return NArgCompleteResult.ofBlank();
        }
        return NArgCompleteResult.ofCandidates(values.filter(this::matches).collect(Collectors.toList()));
    }

    @Override
    public NArgCompleteResult filterCandidates(Collection<NArgCompleteCandidate> candidates) {
        if (candidates == null) {
            return NArgCompleteResult.ofBlank();
        }
        return NArgCompleteResult.ofCandidates(candidates.stream().filter(this::matches).collect(Collectors.toList()));
    }
}
