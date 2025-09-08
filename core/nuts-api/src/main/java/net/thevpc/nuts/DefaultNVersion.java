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
package net.thevpc.nuts;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNVersion implements NVersion {
    private static Pattern VERSION_PART_PATTERN = Pattern.compile("([0-9a-zA-Z_.-])+");

    private static final long serialVersionUID = 1L;
    protected String expression;
    private VersionParts parts;

    public DefaultNVersion(String expression) {
        this.expression = (NStringUtils.trim(expression));
    }

    public static String incVersion(String oldVersion, int level, long count) {
        return incVersion(oldVersion, level, BigInteger.valueOf(count));
    }

    public static String incVersion(String oldVersion, int level, BigInteger count) {
        if (count == null) {
            count = BigInteger.ZERO;
        }
        VersionParts parts = splitVersionParts2(oldVersion);
        int digitCount = parts.getNumbersCount();
        if (digitCount == 0) {
            parts.addNumber(BigInteger.ZERO, ".");
            digitCount = parts.getNumbersCount();
        }
        if (level < 0) {
            level = digitCount + level;
            while (level < 0) {
                parts.addNumber(BigInteger.ZERO, ".");
                level++;
            }
            NVersionPart digit = parts.getNumberAt(level);
            parts.setNumberAt(level, new BigInteger(digit.value()).add(count));
            return parts.toString();
        } else {
            for (int i = digitCount; i < level; i++) {
                parts.addNumber(BigInteger.ZERO, ".");
            }
            NVersionPart digit = parts.getNumberAt(level);
            parts.setNumberAt(level, new BigInteger(digit.value()).add(count));
            return parts.toString();
        }
    }


    public List<NVersionPart> parts() {
        return new ArrayList<>(splitVersionParts2(NStringUtils.trim(expression)).all);
    }

    private static VersionParts splitVersionParts2(String v1) {
        v1 = NStringUtils.trim(v1);
        List<NVersionPart> parts = new ArrayList<>();
        StringBuilder last = null;
        NVersionPartType partType = null;
        boolean qualVisited = false;
        boolean numberVisited = false;
        for (char c : v1.toCharArray()) {
            if (Character.isDigit(c)) {
                numberVisited = true;
                if (last == null) {
                    last = new StringBuilder();
                    last.append(c);
                    partType = NVersionPartType.NUMBER;
                } else if (partType == NVersionPartType.NUMBER) {
                    last.append(c);
                } else {
                    parts.add(new DefaultNVersionPart(last.toString(), partType));
                    last.delete(0, last.length());
                    partType = NVersionPartType.NUMBER;
                    last.append(c);
                }
            } else if (c == '.' || c == '-') {
                if (last != null) {
                    parts.add(new DefaultNVersionPart(last.toString(), partType));
                    last = null;
                }
                parts.add(new DefaultNVersionPart(String.valueOf(c), NVersionPartType.SEPARATOR));
                partType = NVersionPartType.SEPARATOR;
            } else {
                if (last == null) {
                    if (numberVisited) {
                        if (qualVisited) {
                            partType = NVersionPartType.SUFFIX;
                        } else {
                            partType = NVersionPartType.QUALIFIER;
                            qualVisited = true;
                        }
                    } else {
                        partType = NVersionPartType.PREFIX;
                    }
                    last = new StringBuilder();
                    last.append(c);
                } else if (partType == NVersionPartType.QUALIFIER || partType == NVersionPartType.PREFIX || partType == NVersionPartType.SUFFIX) {
                    last.append(c);
                } else {
                    parts.add(new DefaultNVersionPart(last.toString(), partType));
                    if (numberVisited) {
                        if (qualVisited) {
                            partType = NVersionPartType.SUFFIX;
                        } else {
                            partType = NVersionPartType.QUALIFIER;
                            qualVisited = true;
                        }
                    } else {
                        partType = NVersionPartType.PREFIX;
                    }
                    last.delete(0, last.length());
                    last.append(c);
                }
            }
        }
        if (last != null && last.length() > 0) {
            parts.add(new DefaultNVersionPart(last.toString(), partType));
        }
        return new VersionParts(parts);
    }


    public boolean isLatestVersion() {
        String s = asSingleValue().orNull();
        return NConstants.Versions.LATEST.equalsIgnoreCase(s);
    }

    public boolean isReleaseVersion() {
        String s = asSingleValue().orNull();
        return NConstants.Versions.RELEASE.equalsIgnoreCase(s);
    }

    @Override
    public boolean isSnapshotVersion() {
        String s = asSingleValue().orNull();
        return s != null && s.toUpperCase().endsWith("-SNAPSHOT");
    }

    @Override
    public boolean isNull() {
        return expression == null;
    }

    @Override
    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    @Override
    public String getValue() {
        return expression;
    }

    @Override
    public int compareTo(String other) {
        return compareTo(NVersion.of(other), null);
    }

    @Override
    public int compareTo(NVersion other) {
        return compareTo(other, null);
    }

    @Override
    public int compareTo(String other, NVersionComparator comparator) {
        return compareTo(other == null ? BLANK : NVersion.of(other), comparator);
    }

    @Override
    public int compareTo(NVersion other, NVersionComparator comparator) {
        return (comparator == null ? NVersionComparator.of() : comparator).compare(this, other);
    }

    public NVersion toCanonical() {
        VersionParts parts = getParts();
        List<NVersionPart> can = new ArrayList<>();

        for (NVersionPart p : parts.all) {
            if (p.type() == NVersionPartType.NUMBER || p.type() == NVersionPartType.PREFIX || p.type() == NVersionPartType.SEPARATOR) {
                can.add(p);
            } else if (p.type() == NVersionPartType.QUALIFIER) {
                break;
            }
        }
        while (!can.isEmpty() && can.get(can.size() - 1).type() == NVersionPartType.SEPARATOR) {
            can.remove(can.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (NVersionPart v : can) {
            sb.append(v.value());
        }
        return NVersion.of(sb.toString());
    }

    public NVersion toNormalized() {
        List<NVersionPart> parts = new ArrayList<>(getParts().all);
        while (!parts.isEmpty()) {
            NVersionPart last = parts.get(parts.size() - 1);
            if (last.type() == NVersionPartType.SEPARATOR) {
                parts.remove(parts.size() - 1);
            } else if (last.type() == NVersionPartType.NUMBER && new BigInteger(last.value()).equals(BigInteger.ZERO)) {
                parts.remove(parts.size() - 1);
            } else {
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (NVersionPart v : parts) {
            sb.append(v.value());
        }
        return NVersion.of(sb.toString());
    }

    @Override
    public NVersionFilter filter() {
        return filter(null);
    }

    @Override
    public NVersionFilter filter(NVersionComparator comparator) {
        return NVersionFilters.of().byValue(expression,comparator).get();
    }

    @Override
    public NVersion compatNewer() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new DefaultNVersion("[" + expression + ",[");
    }

    @Override
    public NVersion compatOlder() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new DefaultNVersion("]," + v + "]");
    }

    @Override
    public NOptional<List<NVersionInterval>> intervals() {
        return intervals(null);
    }

    @Override
    public NOptional<List<NVersionInterval>> intervals(NVersionComparator comparator) {
        return NVersionInterval.ofList(expression,comparator);
    }

    public NOptional<String> asSingleValue() {
        if (VERSION_PART_PATTERN.matcher(expression).matches()) {
            return NOptional.of(expression);
        }
        int commas = 0;
        int seps = 0;
        String s = expression.trim();
        NOptional<String> emptyVersion = NOptional.ofEmpty(() -> NMsg.ofC("not a single value : %s", expression));
        if (s.isEmpty()) {
            return emptyVersion;
        }
        for (char c : s.toCharArray()) {
            switch (c) {
                case '*':
                    return emptyVersion;
                case ',': {
                    commas++;
                    if (commas > 1) {
                        return emptyVersion;
                    }
                    if (seps >= 2) {
                        return emptyVersion;
                    }
                    break;
                }
                case '(':
                case ')':
                case '[':
                case ']': {
                    seps++;
                    if (seps > 2) {
                        return emptyVersion;
                    }
                    break;
                }
                default: {
                    if (!Character.isWhitespace(c)) {
                        if (seps >= 2) {
                            return emptyVersion;
                        }
                    }
                }
            }
        }
        if (seps == 0) {
            if (commas == 0) {
                if (VERSION_PART_PATTERN.matcher(expression).matches()) {
                    return NOptional.of(expression.trim());
                }
            } else {
                Set<String> all = new HashSet<>(NStringUtils.split(s, ",", true, true));
                if (all.size() == 1) {
                    String one = all.stream().findAny().get();
                    if (VERSION_PART_PATTERN.matcher(one).matches()) {
                        return NOptional.of(one);
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
                        return NOptional.of(s);
                    }
                } else {
                    //commas==1
                    Set<String> two = new HashSet<>(NStringUtils.split(s, ",", true, false));
                    if (two.size() == 1) {
                        String one = two.stream().findAny().get();
                        if (VERSION_PART_PATTERN.matcher(one).matches()) {
                            return NOptional.of(one);
                        }
                    }
                }
            }
        }
        return emptyVersion;
    }

    @Override
    public boolean isSingleValue() {
        return asSingleValue().isPresent();
    }

    @Override
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

    @Override
    public NVersion inc() {
        return inc(-1);
    }

    @Override
    public NVersion inc(int index) {
        return inc(index, 1);
    }

    @Override
    public NVersion inc(int index, long amount) {
        return new DefaultNVersion(incVersion(getValue(), index, amount));
    }

    @Override
    public NVersion inc(int index, BigInteger amount) {
        return new DefaultNVersion(incVersion(getValue(), index, amount));
    }

    public int size() {
        VersionParts parts = getParts();
        return parts.size();
    }

    @Override
    public int numberSize() {
        return getParts().getNumbersCount();
    }

    public NLiteral[] split() {
        VersionParts parts = getParts();
        int size = parts.size();
        NLiteral[] all = new NLiteral[size];
        for (int i = 0; i < size; i++) {
            all[i] = NLiteral.of(parts.get(i).value());
        }
        return all;
    }

    public NOptional<NLiteral> get(int index) {
        VersionParts parts = getParts();
        int size = parts.size();
        if (index >= 0) {
            if (index < parts.size()) {
                return NOptional.of(NLiteral.of(parts.get(index).value()));
            }
        } else {
            int x = size + index;
            if (x >= 0 && x < parts.size()) {
                return NOptional.of(NLiteral.of(parts.get(x).value()));
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("version part not found : %s", index));
    }

    public NOptional<NLiteral> getNumberLiteralAt(int level) {
        VersionParts parts = getParts();
        int size = parts.getNumbersCount();
        if (level >= 0) {
            NVersionPart digit = parts.getNumberAt(level);
            return NOptional.of(
                    digit == null ? null : NLiteral.of(digit.value()),
                    () -> NMsg.ofC("missing number at %s", level)
            );
        } else {
            int x = size + level;
            NVersionPart digit = x >= 0 ? parts.getNumberAt(x) : null;
            return NOptional.of(
                    digit == null ? null : NLiteral.of(digit.value()),
                    () -> NMsg.ofC("missing number at %s", level)
            );
        }
    }

    @Override
    public NOptional<Integer> getIntegerAt(int index) {
        return getNumberLiteralAt(index).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> getLongAt(int index) {
        return getNumberLiteralAt(index).flatMap(NLiteral::asLong);
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

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNVersion version = (DefaultNVersion) o;

        return expression != null ? expression.equals(version.expression) : version.expression == null;

    }

    @Override
    public String toString() {
        return expression == null ? "" : expression;
    }

    private static class VersionParts {

        List<NVersionPart> all;

        public VersionParts(List<NVersionPart> all) {
            this.all = all;
        }

        public NVersionPart get(int index) {
            return all.get(index);
        }

        public int size() {
            return all.size();
        }

        public int getNumbersCount() {
            int c = 0;
            for (NVersionPart s : all) {
                if (s.type() == NVersionPartType.NUMBER) {
                    c++;
                }
            }
            return c;
        }

        public void setNumberAt(int index, BigInteger value) {
            int c = 0;
            for (int i = 0; i < all.size(); i++) {
                NVersionPart s = all.get(i);
                if (s.type() == NVersionPartType.NUMBER) {
                    if (c == index) {
                        all.set(i, new DefaultNVersionPart(value.toString(), NVersionPartType.NUMBER));
                        return;
                    }
                    c++;
                }
            }
        }

        public void setNumberAt(int index, long value) {
            int c = 0;
            for (int i = 0; i < all.size(); i++) {
                NVersionPart s = all.get(i);
                if (s.type() == NVersionPartType.NUMBER) {
                    if (c == index) {
                        all.set(i, new DefaultNVersionPart(String.valueOf(value), NVersionPartType.NUMBER));
                        return;
                    }
                    c++;
                }
            }
        }

        public NVersionPart getNumberAt(int index) {
            int c = 0;
            for (NVersionPart s : all) {
                if (s.type() == NVersionPartType.NUMBER) {
                    if (c == index) {
                        return s;
                    }
                    c++;
                }
            }
            return null;
        }

        public void insertNumber(long val, String sep) {
            if (all.size() == 0) {
                all.add(new DefaultNVersionPart(String.valueOf(val), NVersionPartType.NUMBER));
            } else if (all.get(0).type() == NVersionPartType.NUMBER) {
                if (sep == null) {
                    sep = ".";
                }
                if (!sep.equals(".") && !sep.equals("-")) {
                    throw new IllegalArgumentException("illegal separator");
                }
                all.add(0, new DefaultNVersionPart(sep, NVersionPartType.SEPARATOR));
                all.add(0, new DefaultNVersionPart(String.valueOf(val), NVersionPartType.NUMBER));
            } else {
                all.add(0, new DefaultNVersionPart(String.valueOf(val), NVersionPartType.NUMBER));
            }
        }

        public void addNumber(BigInteger val, String sep) {
            if (all.size() == 0) {
                all.add(new DefaultNVersionPart(String.valueOf(val), NVersionPartType.NUMBER));
            } else if (all.get(all.size() - 1).type() == NVersionPartType.NUMBER) {
                if (sep == null) {
                    sep = ".";
                }
                if (!sep.equals(".") && !sep.equals("-")) {
                    throw new IllegalArgumentException(NMsg.ofC(NI18n.of("illegal version number separator %s"), sep).toString());
                }
                all.add(new DefaultNVersionPart(sep, NVersionPartType.SEPARATOR));
                all.add(new DefaultNVersionPart(String.valueOf(val), NVersionPartType.NUMBER));
            } else {
                all.add(new DefaultNVersionPart(String.valueOf(val), NVersionPartType.NUMBER));
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (NVersionPart versionPart : all) {
                sb.append(versionPart.value());
            }
            return sb.toString();
        }


    }


}
