package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NBootCompleteResult implements NBootCompleteRequestOrResult{
    public static NBootCompleteResult parse(String txt) {
        ArrayList<Candidate> candidates = new ArrayList<>();
        ArrayList<Flag> flags = new ArrayList<>();
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
                                Flag f = null;
                                try {
                                    f = Flag.valueOf(s.trim().toUpperCase());
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
                                candidates.add(new Candidate(line.substring(0, t), line.substring(t + 1).trim()));
                            } else {
                                candidates.add(new Candidate(line));
                            }
                        }
                    } else {
                        int t = line.indexOf('\t');
                        if (t >= 0) {
                            candidates.add(new Candidate(line.substring(0, t), line.substring(t + 1).trim()));
                        } else {
                            candidates.add(new Candidate(line));
                        }
                    }
                }
            }
        }
        return new NBootCompleteResult(candidates, flags);
    }

    public enum Flag {
        NOSPACE,
        PLUSDIRS,
        NOSORT,
        NOQUOTE,
        FILENAMES,
        DIRNAMES,
        NOFILE,
        ERROR,
    }

    public static class Candidate {
        private final String value;
        private String description;

        public Candidate(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public Candidate(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }


        public String description() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Candidate candidate = (Candidate) o;
            return Objects.equals(value, candidate.value) && Objects.equals(description, candidate.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, description);
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "value='" + value + '\'' +
                    (description == null ? "" : (", description='" + description + '\'')) +
                    '}';
        }

        public String format() {
            if (NBootUtils.isBlank(description)) {
                return escape(value, true);
            }
            return escape(value, true) + "\t" + escape(description.trim(), false);
        }
    }

    private final List<Candidate> candidates;
    private final List<Flag> flags;

    public NBootCompleteResult(List<Candidate> candidates, List<Flag> flags) {
        this.candidates = candidates == null ? new ArrayList<>() : candidates.stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.flags = flags == null ? new ArrayList<>() : flags.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Candidate> candidates() {
        return candidates;
    }

    public List<Flag> flags() {
        return flags;
    }

    public String format() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            Candidate candidate = candidates.get(i);
            String v = candidate.value();
            String s = candidate.description();
            sb.append(escape(v, true));
            if (!NBootUtils.isBlank(s) && !Objects.equals(s,v)) {
                sb.append("\t").append(escape(s, false));
            }
        }
        if (!flags.isEmpty()) {
            if (!candidates.isEmpty()) {
                sb.append("\n");
            }
            sb.append("::");
            for (int i = 0; i < flags.size(); i++) {
                Flag flag = flags.get(i);
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(flag.name().toLowerCase());
            }
        }
        return sb.toString();
    }

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
    public String toString() {
        return "NCompleteResult{" +
                "candidates=" + candidates +
                ", flags=" + flags +
                '}';
    }
}
