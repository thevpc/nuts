/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.bundles.strings;


import java.util.Objects;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class StringComparators {

    public static StringComparator ilike(final String pattern) {
        return like(pattern).apply(StringTransforms.LOWER);
    }

    public static StringComparator ilikepart(String pattern) {
        if(pattern==null){
            pattern="";
        }
        if(!pattern.startsWith("*")){
            pattern="*"+pattern;
        }
        if(!pattern.endsWith("*")){
            pattern=pattern+"*";
        }
        return like(pattern).apply(StringTransforms.LOWER);
    }

    public static StringComparator ieq(final String value) {
        return eq(value).apply(StringTransforms.LOWER);
    }

    public static StringComparator not(final StringComparator other) {
        return new NotStringComparator(other);
    }

    public static StringComparator any() {
        return new AnyStringComparator();
    }
    public static StringComparator like(final String pattern) {
        return new LikeStringComparator(pattern);
    }

    public static StringComparator eq(final String pattern) {
        return new EqStringComparator(pattern);
    }

    public static StringComparator iregexp(final String pattern) {
        return regexp(pattern).apply(StringTransforms.LOWER);
    }

    public static StringComparator regexp(final String pattern) {
        return new RegexpStringComparator(pattern);
    }

    private static class NotStringComparator extends AbstractStringComparator {

        private final StringComparator other;

        public NotStringComparator(StringComparator other) {
            this.other = other;
        }

        @Override
        AbstractStringComparator copy() {
            return new NotStringComparator(other);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 53 * hash + Objects.hashCode(this.other);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final NotStringComparator other = (NotStringComparator) obj;
            if (!Objects.equals(this.other, other.other)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean matches(String value) {
            if (getTransform() != null) {
                value = getTransform().transform(value);
            }
            return !other.matches(value);
        }

        @Override
        public String toString() {
            return "not(" + other + (getTransform() == null ? "" : ("," + getTransform().toString())) + ")";
        }
    }

    private static class LikeStringComparator extends AbstractStringComparator {

        private final String pattern;

        public LikeStringComparator(String pattern) {
            this.pattern = pattern;
        }

        @Override
        AbstractStringComparator copy() {
            return new LikeStringComparator(pattern);
        }

        private String compilePattern(String likePattern) {
            StringBuilder b = new StringBuilder();
            for (char c : likePattern.toCharArray()) {
                switch (c) {
                    case '\\': {
                        b.append(c).append(c);
                        break;
                    }
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case '^':
                    case '$':
                    case '.':
                    case '+': {
                        b.append('\\').append(c);
                        break;
                    }
                    case '*': {
                        b.append(".*");
                        break;
                    }
                    case '?': {
                        b.append(".");
                        break;
                    }
                    default: {
                        b.append(c);
                    }
                }
            }
            return b.toString();
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 29 * hash + Objects.hashCode(this.pattern);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final LikeStringComparator other = (LikeStringComparator) obj;
            if (!Objects.equals(this.pattern, other.pattern)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean matches(String value) {
            String pattern0 = pattern;
            if (getTransform() != null) {
                value = getTransform().transform(value);
                pattern0 = getTransform().transform(pattern0);
            }
            pattern0 = compilePattern(pattern0);
            return (value == null && pattern0.isEmpty()) || (value != null && value.matches(pattern0));
        }

        @Override
        public String toString() {
            return "like(" + pattern + (getTransform() == null ? "" : ("," + getTransform().toString())) + ")";
        }
    }

    private static class AnyStringComparator extends AbstractStringComparator {

        public AnyStringComparator() {
        }

        @Override
        AbstractStringComparator copy() {
            return new AnyStringComparator();
        }


        @Override
        public boolean matches(String value) {
            return true;
        }

        @Override
        public String toString() {
            return "any";
        }
    }

    private static class EqStringComparator extends AbstractStringComparator {

        private final String pattern;

        public EqStringComparator(String pattern) {
            this.pattern = pattern;
        }

        @Override
        AbstractStringComparator copy() {
            return new EqStringComparator(pattern);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 29 * hash + Objects.hashCode(this.pattern);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final EqStringComparator other = (EqStringComparator) obj;
            if (!Objects.equals(this.pattern, other.pattern)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean matches(String value) {
            String t_pattern = pattern;
            if (getTransform() != null) {
                value = getTransform().transform(value);
                t_pattern = getTransform().transform(t_pattern);
            }
            return (value == t_pattern) || (value != null && value.equals(t_pattern));
        }

        @Override
        public String toString() {
            return "eq(" + pattern + (getTransform() == null ? "" : ("," + getTransform().toString())) + ")";
        }
    }

    private static class RegexpStringComparator extends AbstractStringComparator {

        private final String pattern;

        public RegexpStringComparator(String pattern) {
            this.pattern = pattern;
        }

        @Override
        AbstractStringComparator copy() {
            return new RegexpStringComparator(pattern);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 29 * hash + Objects.hashCode(this.pattern);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final RegexpStringComparator other = (RegexpStringComparator) obj;
            if (!Objects.equals(this.pattern, other.pattern)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean matches(String value) {
            String pattern0 = pattern;
            if (getTransform() != null) {
                value = getTransform().transform(value);
                pattern0 = getTransform().transform(pattern0);
            }
            String v = value == null ? "" : value;
            String p = pattern0 == null ? "" : pattern0;
            return v.matches(p);
        }

        @Override
        public String toString() {
            return "regexp(" + pattern + (getTransform() == null ? "" : ("," + getTransform().toString())) + ")";
        }
    }

}
