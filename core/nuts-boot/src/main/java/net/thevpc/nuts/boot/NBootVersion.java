/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootConstants;
import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 */
public class NBootVersion {
    private static Pattern VERSION_PART_PATTERN = Pattern.compile("([0-9a-zA-Z_.-])+");
    public static Pattern PATTERN = Pattern.compile("[A-Za-z0-9._*,()\\[\\] ${}+-]+");
    public static NBootVersion BLANK = new NBootVersion("");

    private static final long serialVersionUID = 1L;
    protected String expression;
    private VersionParts parts;

    public NBootVersion(String expression) {
        this.expression = (NBootUtils.trim(expression));
    }

    public static String incVersion(String oldVersion, int level, long count) {
        return incVersion(oldVersion, level, BigInteger.valueOf(count));
    }

    public static String incVersion(String oldVersion, int level, BigInteger count) {
        if (count == null) {
            count = BigInteger.ZERO;
        }
        VersionParts parts = splitVersionParts2(oldVersion);
        int digitCount = parts.getDigitCount();
        if (digitCount == 0) {
            parts.addDigit(BigInteger.ZERO, ".");
            digitCount = parts.getDigitCount();
        }
        if (level < 0) {
            level = digitCount + level;
            while (level < 0) {
                parts.addDigit(BigInteger.ZERO, ".");
                level++;
            }
            VersionPart digit = parts.getDigit(level);
            digit.string = String.valueOf(new BigInteger(digit.string).add(count));
            return parts.toString();
        } else {
            for (int i = digitCount; i < level; i++) {
                parts.addDigit(BigInteger.ZERO, ".");
            }
            VersionPart digit = parts.getDigit(level);
            digit.string = String.valueOf(new BigInteger(digit.string).add(count));
            return parts.toString();
        }
    }


    public static int compareVersions(String v1, String v2) {
        v1 = NBootUtils.trim(v1);
        v2 = NBootUtils.trim(v2);
        if (v1.equals(v2)) {
            return 0;
        }
        if (NBootConstants.Versions.LATEST.equals(v1)) {
            return 1;
        }
        if (NBootConstants.Versions.LATEST.equals(v2)) {
            return -1;
        }
        if (NBootConstants.Versions.RELEASE.equals(v1)) {
            return 1;
        }
        if (NBootConstants.Versions.RELEASE.equals(v2)) {
            return -1;
        }
        VersionParts v1arr = splitVersionParts2(v1);
        VersionParts v2arr = splitVersionParts2(v2);
        return v1arr.compareTo(v2arr);
    }


    private static VersionParts splitVersionParts2(String v1) {
        v1 = NBootUtils.trim(v1);
        List<VersionPart> parts = new ArrayList<>();
        StringBuilder last = null;
        VersionPartType partType = null;
        for (char c : v1.toCharArray()) {
            if (Character.isDigit(c)) {
                if (last == null) {
                    last = new StringBuilder();
                    last.append(c);
                    partType = VersionPartType.NUMBER;
                } else if (partType == VersionPartType.NUMBER) {
                    last.append(c);
                } else {
                    parts.add(new VersionPart(last.toString(), partType));
                    last.delete(0, last.length());
                    partType = VersionPartType.NUMBER;
                    last.append(c);
                }
            } else if (c == '.' || c == '-') {
                if (last != null) {
                    parts.add(new VersionPart(last.toString(), partType));
                    last = null;
                }
                parts.add(new VersionPart(String.valueOf(c), VersionPartType.SEPARATOR));
                partType = VersionPartType.SEPARATOR;
            } else {
                if (last == null) {
                    partType = VersionPartType.QAL;
                    last = new StringBuilder();
                    last.append(c);
                } else if (partType == VersionPartType.QAL) {
                    last.append(c);
                } else {
                    parts.add(new VersionPart(last.toString(), partType));
                    partType = VersionPartType.QAL;
                    last.delete(0, last.length());
                    last.append(c);
                }
            }
        }
        if (last != null && last.length() > 0) {
            parts.add(new VersionPart(last.toString(), partType));
        }
        return new VersionParts(parts);
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
        }
        return null;
    }

    public static NBootVersion of(String value) {
        return new NBootVersion(value);
    }


    public boolean isLatestVersion() {
        String s = asSingleValue();
        return NBootConstants.Versions.LATEST.equalsIgnoreCase(s);
    }

    public boolean isReleaseVersion() {
        String s = asSingleValue();
        return NBootConstants.Versions.RELEASE.equalsIgnoreCase(s);
    }

    
    public boolean isSnapshotVersion() {
        String s = asSingleValue();
        return s != null && s.toUpperCase().endsWith("-SNAPSHOT");
    }

    
    public boolean isNull() {
        return expression == null;
    }

    
    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    
    public String getValue() {
        return expression;
    }

    
    public int compareTo(String other) {
        return compareVersions(expression, other);
    }

    
    public int compareTo(NBootVersion other) {
        return compareTo(other == null ? null : other.getValue());
    }

    
//    public NVersionFilter filter() {
//        return NVersionFilters.of().byValue(expression).get();
//    }

    
    public NBootVersion compatNewer() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new NBootVersion("[" + expression + ",[");
    }

    
    public NBootVersion compatOlder() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new NBootVersion("]," + v + "]");
    }

    
    public List<NVersionIntervalBoot> intervals() {
        return NVersionIntervalBoot.ofList(expression);
    }

    public String asSingleValue() {
        if (VERSION_PART_PATTERN.matcher(expression).matches()) {
            return expression;
        }
        int commas = 0;
        int seps = 0;
        String s = expression.trim();
        if (s.isEmpty()) {
            return null;
        }
        for (char c : s.toCharArray()) {
            switch (c) {
                case '*':
                    return null;
                case ',': {
                    commas++;
                    if (commas > 1) {
                        return null;
                    }
                    if (seps >= 2) {
                        return null;
                    }
                    break;
                }
                case '(':
                case ')':
                case '[':
                case ']': {
                    seps++;
                    if (seps > 2) {
                        return null;
                    }
                    break;
                }
                default: {
                    if (!Character.isWhitespace(c)) {
                        if (seps >= 2) {
                            return null;
                        }
                    }
                }
            }
        }
        if (seps == 0) {
            if (commas == 0) {
                if (VERSION_PART_PATTERN.matcher(expression).matches()) {
                    return (expression.trim());
                }
            } else {
                Set<String> all = new HashSet<>(NBootUtils.split(s, ",", true, true));
                if (all.size() == 1) {
                    String one = all.stream().findAny().get();
                    if (VERSION_PART_PATTERN.matcher(one).matches()) {
                        return (one);
                    }
                }
            }
        } else if (seps == 2) {
            // this is now a simple version
            char o = s.charAt(0);
            char c = s.charAt(s.length() - 1);
            if (o == '(') {
                o = ']';
            }
            if (c == ')') {
                c = '[';
            }
            s = s.substring(1, s.length() - 1).trim();
            if (o == '[' && c == ']') {
                if (commas == 0) {
                    if (VERSION_PART_PATTERN.matcher(s).matches()) {
                        return (s);
                    }
                } else {
                    //commas==1
                    Set<String> two = new HashSet<>(NBootUtils.split(s, ",", true, false));
                    if (two.size() == 1) {
                        String one = two.stream().findAny().get();
                        if (VERSION_PART_PATTERN.matcher(one).matches()) {
                            return (one);
                        }
                    }
                }
            }
        }
        return null;
    }

    
    public boolean isSingleValue() {
        return asSingleValue()!=null;
    }

    
    public boolean isFilter() {
        for (char c : expression.toCharArray()) {
            switch (c) {
                case '*':
                case ',':
                case '(':
                case ')':
                case '[':
                case ']': {
                    return true;
                }
            }
        }
        return false;
    }

    
    public NBootVersion inc() {
        return inc(-1);
    }

    
    public NBootVersion inc(int index) {
        return inc(index, 1);
    }

    
    public NBootVersion inc(int index, long amount) {
        return new NBootVersion(incVersion(getValue(), index, amount));
    }

    
    public NBootVersion inc(int index, BigInteger amount) {
        return new NBootVersion(incVersion(getValue(), index, amount));
    }

    public int size() {
        VersionParts parts = getParts();
        return parts.size();
    }

    
    public int numberSize() {
        return getParts().getDigitCount();
    }

    public String[] split() {
        VersionParts parts = getParts();
        int size = parts.size();
        String[] all = new String[size];
        for (int i = 0; i < size; i++) {
            all[i] = (parts.get(i).string);
        }
        return all;
    }

    public String get(int index) {
        VersionParts parts = getParts();
        int size = parts.size();
        if (index >= 0) {
            if (index < parts.size()) {
                return ((parts.get(index).string));
            }
        } else {
            int x = size + index;
            if (x >= 0 && x < parts.size()) {
                return ((parts.get(x).string));
            }
        }
        return null;
    }

    public String getNumber(int level) {
        VersionParts parts = getParts();
        int size = parts.getDigitCount();
        if (level >= 0) {
            VersionPart digit = parts.getDigit(level);
            return (
                    digit == null ? null : (digit.string)
            );
        } else {
            int x = size + level;
            VersionPart digit = x >= 0 ? parts.getDigit(x) : null;
            return (
                    digit == null ? null : (digit.string)
            );
        }
    }

    private String toExplicitSingleValueOrNullString() {
        if (!isBlank() && !isFilter()) {
            return expression;
        }
        return null;
    }

    private VersionParts getParts() {
        if (parts == null) {
            parts = splitVersionParts2(getValue());
        }
        return parts;
    }

    
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }

    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NBootVersion version = (NBootVersion) o;

        return expression != null ? expression.equals(version.expression) : version.expression == null;

    }

    
    public String toString() {
        return expression == null ? "" : expression;
    }

    enum VersionPartType {
        NUMBER,
        QAL,
        SEPARATOR,
    }

    private static class VersionPart {

        String string;
        VersionPartType type;

        public VersionPart(String string, VersionPartType type) {
            this.string = string;
            this.type = type;
        }

        
        public int hashCode() {
            return Objects.hash(string, type);
        }

        
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionPart that = (VersionPart) o;
            return string.equalsIgnoreCase(that.string) && type == that.type;
        }

        
        public String toString() {
            String name = type.name().toLowerCase();
            return name + "(" + string + ")";
        }

        public int compareTo(VersionPart v2) {
            VersionPart v1 = this;
            if (v1.equals(v2)) {
                return 0;
            }
            if (v1.type == VersionPartType.SEPARATOR && v2.type == VersionPartType.SEPARATOR) {
                //a dash usually precedes a qualifier, and is always less important than something preceded with a dot.
                if (v1.string.equals("-")) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (v1.type == VersionPartType.SEPARATOR) {
                return -1;
            } else if (v2.type == VersionPartType.SEPARATOR) {
                return 1;
            }

            if (v1.type == VersionPartType.NUMBER && v2.type == VersionPartType.NUMBER) {
                return new BigInteger(v1.string).compareTo(new BigInteger(v2.string));
            } else if (v1.type == VersionPartType.NUMBER) {
                return 1;
            } else if (v2.type == VersionPartType.NUMBER) {
                return -1;
            } else {
                //both are string...
                Integer q1 = getKnownQualifierIndex(v1.string);
                Integer q2 = getKnownQualifierIndex(v2.string);
                if (q1 != null && q2 != null) {
                    return q1.compareTo(q2);
                } else if (q1 != null) {
                    return -1;
                } else if (q2 != null) {
                    return 1;
                } else {
                    return v1.string.compareToIgnoreCase(v2.string);
                }
            }
        }
    }

    private static class VersionParts {

        List<VersionPart> all;

        public VersionParts(List<VersionPart> all) {
            this.all = all;
        }

        public VersionPart get(int index) {
            return all.get(index);
        }

        public int size() {
            return all.size();
        }

        public int getDigitCount() {
            int c = 0;
            for (VersionPart s : all) {
                if (s.type == VersionPartType.NUMBER) {
                    c++;
                }
            }
            return c;
        }

        public VersionPart getDigit(int index) {
            int c = 0;
            for (VersionPart s : all) {
                if (s.type == VersionPartType.NUMBER) {
                    if (c == index) {
                        return s;
                    }
                    c++;
                }
            }
            return null;
        }

        public void insertDigit(long val, String sep) {
            if (all.size() == 0) {
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else if (all.get(0).type == VersionPartType.NUMBER) {
                if (sep == null) {
                    sep = ".";
                }
                if (!sep.equals(".") && !sep.equals("-")) {
                    throw new IllegalArgumentException("illegal separator");
                }
                all.add(0, new VersionPart(sep, VersionPartType.SEPARATOR));
                all.add(0, new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else {
                all.add(0, new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            }
        }

        public void addDigit(BigInteger val, String sep) {
            if (all.size() == 0) {
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else if (all.get(all.size() - 1).type == VersionPartType.NUMBER) {
                if (sep == null) {
                    sep = ".";
                }
                if (!sep.equals(".") && !sep.equals("-")) {
                    throw new IllegalArgumentException("illegal separator");
                }
                all.add(new VersionPart(sep, VersionPartType.SEPARATOR));
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else {
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (VersionPart versionPart : all) {
                sb.append(versionPart.string);
            }
            return sb.toString();
        }

        /**
         * https://maven.apache.org/ref/3.3.3/maven-artifact/apidocs/org/apache/maven/artifact/versioning/ComparableVersion.html
         *
         * @param v2 v2
         * @return compare int
         */
        public int compareTo(VersionParts v2) {
            VersionParts v1 = this;
            int i = 0;
            int j = 0;
            while (i < v1.size() || j < v2.size()) {
                if (i < v1.size() && j < v2.size()) {
                    VersionPart a = v1.get(i);
                    VersionPart b = v2.get(i);
                    int r = a.compareTo(b);
                    if (r != 0) {
                        return r;
                    }
                    i++;
                    j++;
                } else if (i < v1.size()) {
                    if (isQualifierFrom(i, v1)) {
                        return -1;
                    }
                    return 1;
                } else {
                    if (isQualifierFrom(i, v2)) {
                        return 1;
                    }
                    return -1;
                }
            }
            return 0;
        }
    }

    private static boolean isQualifierFrom(int i, VersionParts v1) {
        VersionPart a = v1.get(i);
        if (a.type == VersionPartType.SEPARATOR && a.string.equals("-")) {
            if (i + 1 < v1.size()) {
                for (int j = i + 1; j < v1.size(); j++) {
                    a = v1.get(j);
                    switch (a.type) {
                        case SEPARATOR:
                            if (a.string.equals(".")) {
                                return false;
                            }
                            break;
                        case QAL:
                            return true;
                        default:
                            return false;
                    }
                }
                return true;
            } else {
                return true;
            }
        }
        return false;
    }

    public static class NVersionIntervalBoot implements Serializable {

        private static final long serialVersionUID = 1L;

        private final boolean includeLowerBound;
        private final boolean includeUpperBound;
        private final String lowerBound;
        private final String upperBound;

        static NVersionIntervalBoot of(String s) {
            List<NVersionIntervalBoot> x = ofList(s);
            if (x.isEmpty()) {
                return null;
            }
            if (x.size() > 1) {
                throw new NBootException(NBootMsg.ofC("too many intervals"));
            }
            return x.get(0);
        }

        static List<NVersionIntervalBoot> ofList(String s) {
            return new NReservedVersionIntervalParserBoot().parse(s);
        }

        public NVersionIntervalBoot(boolean inclusiveLowerBoundary, boolean inclusiveUpperBoundary, String min, String max) {
            this.includeLowerBound = inclusiveLowerBoundary;
            this.includeUpperBound = inclusiveUpperBoundary;
            this.lowerBound = NBootUtils.trimToNull(min);
            this.upperBound = NBootUtils.trimToNull(max);
        }

        public boolean acceptVersion(NBootVersion version) {
            if (!NBootUtils.isBlank(lowerBound) && !lowerBound.equals(NBootConstants.Versions.LATEST) && !lowerBound.equals(NBootConstants.Versions.RELEASE)) {
                int t = version.compareTo(lowerBound);
                if ((includeLowerBound && t < 0) || (!includeLowerBound && t <= 0)) {
                    return false;
                }
            }
            if (!NBootUtils.isBlank(upperBound) && !upperBound.equals(NBootConstants.Versions.LATEST) && !upperBound.equals(NBootConstants.Versions.RELEASE)) {
                int t = version.compareTo(upperBound);
                return (!includeUpperBound || t <= 0) && (includeUpperBound || t < 0);
            }
            return true;
        }

        public boolean isFixedValue() {
            return includeLowerBound && includeUpperBound && NBootUtils.trim(lowerBound).equals(NBootUtils.trim(upperBound))
                    && !NBootConstants.Versions.LATEST.equals(lowerBound) && !NBootConstants.Versions.RELEASE.equals(lowerBound);
        }

        public boolean isIncludeLowerBound() {
            return includeLowerBound;
        }


        public boolean isIncludeUpperBound() {
            return includeUpperBound;
        }


        public String getLowerBound() {
            return lowerBound;
        }


        public String getUpperBound() {
            return upperBound;
        }

        public String toString() {
            String lb = lowerBound == null ? "" : lowerBound;
            String ub = upperBound == null ? "" : upperBound;
            boolean sameBound = ub.equals(lb);

            if (sameBound && !lb.isEmpty()) {
                return (includeLowerBound ? "[" : "]")
                        + lb
                        + (includeUpperBound ? "]" : "[");
            }
            return (includeLowerBound ? "[" : "]")
                    + lb
                    + ","
                    + ub
                    + (includeUpperBound ? "]" : "[");
        }

    }



    public static class NReservedVersionIntervalParserBoot {

        final int NEXT = 1;
        final int NEXT_COMMA = 2;
        final int EXPECT_V1 = 3;
        final int EXPECT_V_COMMA = 4;
        final int EXPECT_V2 = 5;
        final int EXPECT_CLOSE = 6;
        int t;
        int state = NEXT;
        int open = -1;
        int close = -1;
        String v1 = null;
        String v2 = null;
        List<NVersionIntervalBoot> dd = new ArrayList<>();

        public NReservedVersionIntervalParserBoot() {
        }

        void reset() {
            open = -1;
            close = -1;
            v1 = null;
            v2 = null;
        }

        void addNextValue(String sval) {
            if (sval.endsWith("*")) {
                String min = sval.substring(0, sval.length() - 1);
                if (min.equals("")) {
                    dd.add(new NVersionIntervalBoot(false, false, min, null));
                } else {
                    String max = NBootVersion.of(min).inc(-1).getValue();
                    dd.add(new NVersionIntervalBoot(true, false, min, max));
                }
            } else {
                dd.add(new NVersionIntervalBoot(true, true, sval, sval));
            }
        }

        void addNextInterval() {
            boolean inclusiveLowerBoundary = open == '[' && (v1 != null);
            boolean inclusiveUpperBoundary = close == ']' && (v2 != null);
            dd.add(new NVersionIntervalBoot(inclusiveLowerBoundary, inclusiveUpperBoundary, v1, v2));
            reset();
        }

        public List<NVersionIntervalBoot> parse(String version) {
            StreamTokenizer st = new StreamTokenizer(new StringReader(version));
            st.resetSyntax();
            st.whitespaceChars(0, 32);
            for (int i = 33; i < 256; i++) {
                switch ((char) i) {
                    case '(':
                    case ')':
                    case ',':
                    case '[':
                    case ']': {
                        break;
                    }
                    default: {
                        st.wordChars(i, i);
                    }
                }
            }
            try {
                while ((t = st.nextToken()) != StreamTokenizer.TT_EOF) {
                    switch (state) {
                        case NEXT: {
                            switch (t) {
                                case StreamTokenizer.TT_WORD: {
                                    addNextValue(st.sval);
                                    state = NEXT_COMMA;
                                    break;
                                }
                                case '[':
                                case ']':
                                case '(': {
                                    open = t;
                                    state = EXPECT_V1;
                                    break;
                                }
                                case ',': {
                                    //just ignore
                                    break;
                                }
                                default: {
                                    throw new NBootException(NBootMsg.ofC("unexpected  %s", ((char) t)));
                                }
                            }
                            break;
                        }
                        case NEXT_COMMA: {
                            switch (t) {
                                case ',': {
                                    state = NEXT;
                                    break;
                                }
                                default: {
                                    throw new NBootException(NBootMsg.ofC("unexpected ',' found %s", ((char) t)));
                                }
                            }
                            break;
                        }
                        case EXPECT_V1: {
                            switch (t) {
                                case StreamTokenizer.TT_WORD: {
                                    v1 = st.sval;
                                    state = EXPECT_V_COMMA;
                                    break;
                                }
                                case ',': {
                                    state = EXPECT_V2;
                                    break;
                                }
                                default: {
                                    throw new NBootException(NBootMsg.ofC("unexpected  %s", ((char) t)));
                                }
                            }
                            break;

                        }
                        case EXPECT_V_COMMA: {
                            switch (t) {
                                case ',': {
                                    state = EXPECT_V2;
                                    break;
                                }
                                case ']': {
                                    close = t;
                                    v2 = v1;
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                case '[':
                                case ')': {
                                    close = t;
                                    v2 = v1; //the same?
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                default: {
                                    throw new NBootException(NBootMsg.ofC("unexpected  %s", ((char) t)));
                                }
                            }
                            break;
                        }
                        case EXPECT_V2: {
                            switch (t) {
                                case StreamTokenizer.TT_WORD: {
                                    v2 = st.sval;
                                    state = EXPECT_CLOSE;
                                    break;
                                }
                                case '[':
                                case ']':
                                case ')': {
                                    close = t;
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                default: {
                                    throw new NBootException(NBootMsg.ofC("unexpected  %s", ((char) t)));
                                }
                            }
                            break;
                        }
                        case EXPECT_CLOSE: {
                            switch (t) {
                                case '[':
                                case ']':
                                case ')': {
                                    close = t;
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                default: {
                                    throw new NBootException(NBootMsg.ofC("unexpected  %s", ((char) t)));
                                }
                            }
                            break;
                        }
                        default: {
                            throw new NBootException(NBootMsg.ofC("unsupported state %s", state));
                        }
                    }
                }
                if (state != NEXT_COMMA && state != NEXT) {
                    throw new NBootException(NBootMsg.ofC("invalid state %s", state));
                }
            } catch (IOException ex) {
                throw new NBootException(NBootMsg.ofC("parse version failed: %s : ", version, ex));
            }
            return dd;
        }
    }

}
