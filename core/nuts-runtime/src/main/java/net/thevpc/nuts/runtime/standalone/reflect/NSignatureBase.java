package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NSignature;
import net.thevpc.nuts.reflect.NSignatureDomain;
import net.thevpc.nuts.reflect.NSignatureScore;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

public abstract class NSignatureBase<T, A extends NSignature<T,?>> implements NSignature<T,A>{
    private final String name;
    private final T[] types;
    private final boolean vararg;
    private final NSignatureDomain<T> domain;

    protected static <T> void checkVararg(T[] types, NSignatureDomain<T> domain) {
        if (types.length == 0) {
            throw new IllegalArgumentException("invalid vararg with zero length arguments");
        }
        T lastType = types[types.length - 1];
        if (!domain.isArray(lastType)) {
            throw new IllegalArgumentException("last argument must be a valid array for vararg methods");
        }
    }

    public NSignatureDomain<T> domain() {
        return domain;
    }

    protected NSignatureBase(String name, T[] types, boolean vararg, NSignatureDomain<T> domain) {
        this.name = NStringUtils.trimToNull(name);
        this.types = types;
        this.vararg = vararg;
        this.domain = domain;
        if (vararg) {
            checkVararg(types, domain);
        }
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name, "name");
    }

    public A toUnnamed() {
        if (name == null) {
            return (A) this;
        }
        return _create(null, types, vararg);
    }


    protected abstract A _create(String name, T[] types, boolean vararg);

    public A toNamed(String newName) {
        String n = NStringUtils.trimToNull(newName);
        if (Objects.equals(n, name)) {
            return (A) this;
        }
        return _create(n, types, vararg);
    }


    public boolean isNamed() {
        return name != null;
    }

    public T getType(int index) {
        return types[index];
    }

    public A setVararg(boolean vararg) {
        if (this.vararg == vararg) {
            return (A) this;
        }
        if (vararg) {
            checkVararg(types, domain);
        }
        T[] types2 = Arrays.copyOfRange(types, 0, types.length);
        return _create(name, types2, vararg);
    }

    public A set(T any, int pos) {
        T[] types2 = Arrays.copyOfRange(types, 0, types.length);
        types2[pos] = any;
        if (vararg) {
            checkVararg(types2, domain());
        }
        return _create(name, types2, vararg);
    }

    public int size() {
        return types.length;
    }

    public T[] types() {
        return Arrays.copyOf(types, types.length);
    }

    public boolean isVarArgs() {
        return vararg;
    }

    @Override
    public String toString() {
        if (types.length == 0) {
            if (name == null) {
                return "()";
            }
            return name + "()";
        } else {
            StringBuilder b = new StringBuilder();
            if (name != null) {
                b.append(name);
            }
            b.append('(');
            if (vararg) {
                if (types.length == 1) {
                    b.append(safeStrOfType(types[0]));
                    b.append("...");
                } else {
                    b.append(safeStrOfType(types[0]));
                    for (int i = 1; i < types.length - 1; i++) {
                        b.append(",");
                        b.append(safeStrOfType(types[i]));
                    }
                    b.append(safeStrOfType(types[types.length - 1]));
                    b.append("...");
                }
            } else {
                b.append(types[0]);
                for (int i = 1; i < types.length; i++) {
                    b.append(",");
                    b.append(safeStrOfType(types[i]));
                }
            }
            return b.append(')').toString();
        }
    }

    private String safeStrOfType(T t) {
        if (t == null) {
            return "null";
        }
        if (domain().isArray(t)) {
            return safeStrOfType(domain().getComponentType(t)) + "[]";
        }
        return domain().toSignatureString(t);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSignatureBase nSig = (NSignatureBase) o;
        return vararg == nSig.vararg
                && Objects.equals(domain, nSig.domain)
                && Objects.equals(name, nSig.name)
                && Objects.deepEquals(types, nSig.types)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, name, Arrays.hashCode(types), vararg);
    }


    public boolean matches(A other) {
        if (!nameMatches(name, other.name().orNull())) {
            return false;
        }
        int mySize = this.size();
        boolean vararg1 = isVarArgs();
        if (vararg1 && other.size() == mySize - 1) {
            for (int i = 0; i < mySize - 1; i++) {
                T a = types[i];
                T b = (T) other.getType(i);
                if (!typeMatches(a, b, false)) {
                    return false;
                }
            }
            return true;
        }
        if (mySize != other.size()) {
            return false;
        }
        for (int i = 0; i < mySize; i++) {
            T a = types[i];
            T b = (T) other.getType(i);
            if (!typeMatches(a, b, vararg1 && i == mySize - 1)) {
                return false;
            }
        }
        return true;
    }

    private int calculateNameScore(String actualName, String pattern) {
        if (NBlankable.isBlank(pattern) && NBlankable.isBlank(actualName)) {
            return 0;
        }
        if (NBlankable.isBlank(pattern) || NBlankable.isBlank(actualName)) {
            return 1; // a small penalty if "unnamed" is less desirable
        }
        if (actualName.equals(pattern)) {
            return 0; // Perfect
        }
        if(!isRegex(actualName) &&  !isRegex(pattern)){
            if(NNameFormat.equalsIgnoreFormat(actualName, pattern)){
                return 2;
            }
        }

        if (actualName.equalsIgnoreCase(pattern)) {
            return 3;
        }

        if (pattern.equals("*")) {
            return 4;
        }
        try {
            if(actualName.matches(pattern)){
                return 4;
            }
        } catch (Exception e) {
            // just ignore
        }
        return Integer.MAX_VALUE;
    }

    private boolean isRegex(String a){
        for (char c : a.toCharArray()) {
            switch (c){
                case '*':
                case '?':
                case '[':
                case ']':
                case '(':
                case ')':
                case '<':
                case '>':
                    return true;
            }
        }
        return false;
    }

    public NSignatureScore calculateScore(A other) {
        int nm = calculateNameScore(name, other.name().orNull());
        if (nm == Integer.MAX_VALUE) {
            return NSignatureScore.NO_MATCH;
        }

        int totalDistance = 0;
        int mySize = this.size();
        int otherSize = other.size();
        boolean vararg1 = isVarArgs();

        // 1. Handle size mismatch for non-varargs
        if (!vararg1 && mySize != otherSize) {
            return NSignatureScore.NO_MATCH;
        }

        // 2. Handle Vararg "Empty" case: foo(String... args) called as foo()
        if (vararg1 && otherSize == mySize - 1) {
            // Penalty for using vararg instead of exact match
            totalDistance += 100;
        } else if (mySize == otherSize) {
            for (int i = 0; i < mySize; i++) {
                int d = getDistance(types[i], (T) other.getType(i));
                if (d < 0) return NSignatureScore.NO_MATCH;

                // Apply vararg penalty to the last element if it's a vararg match
                if (vararg1 && i == mySize - 1) {
                    totalDistance += (d + 100);
                } else {
                    totalDistance += d;
                }
            }
        } else if (vararg1 && otherSize >= mySize) {
            // Handle foo(String... args) called as foo("a", "b", "c")
            for (int i = 0; i < mySize - 1; i++) {
                int d = getDistance(types[i], (T) other.getType(i));
                if (d < 0) return NSignatureScore.NO_MATCH;
                totalDistance += d;
            }
            T varargComponent = domain().getComponentType(types[mySize - 1]);
            for (int i = mySize - 1; i < otherSize; i++) {
                int d = getDistance(varargComponent, (T) other.getType(i));
                if (d < 0) return NSignatureScore.NO_MATCH;
                totalDistance += (d + 100); // Vararg penalty per element
            }
        } else {
            return NSignatureScore.NO_MATCH;
        }

        return NSignatureScore.of(nm, totalDistance);
    }

    private boolean typeMatches(T a, T b, boolean varArg) {
        if (a == null) {
            return true;
        }
        if (b == null) {
            return true;
        }
        NSignatureDomain<T> _domain = domain();
        if (_domain.isPrimitive(a)) {
            a = _domain.toBoxedType(a);
        }
        if (_domain.isPrimitive(b)) {
            b = _domain.toBoxedType(b);
        }
        if (_domain.isAssignableFrom(a, b)) {
            return true;
        }
        if (varArg && _domain.isArray(a)) {
            return typeMatches(_domain.getComponentType(a), b, false);
        }
        return false;
    }

    private boolean nameMatches(String name, String namePattern) {
        if (NBlankable.isBlank(namePattern) || NBlankable.isBlank(name)) {
            return true;
        }
        if (NStringUtils.trim(namePattern).equals("*")) {
            return true;
        }
        return name.matches(namePattern);
    }


    protected int getDistance(T expected, T actual) {
        NSignatureDomain<T> d = domain();

        // 1. Ask the domain first (it might have a fast-path)
        int dist = d.getDistance(expected, actual);

        // 2. If the domain says "I don't know" (-1), we compute it ourselves
        if (dist < 0) {
            dist = computeDefaultDistance(expected, actual);
        }

        return dist;
    }

    private int computeDefaultDistance(T expected, T actual) {
        if (Objects.equals(expected, actual)) return 0;

        NSignatureDomain<T> d = domain();

        T bE = d.isPrimitive(expected) ? d.toBoxedType(expected) : expected;
        T bA = d.isPrimitive(actual) ? d.toBoxedType(actual) : actual;
        if (Objects.equals(bE, bA)) return 1;

        // 2. BFS Hierarchy Traversal
        Queue<TypeNode<T>> queue = new LinkedList<>();
        Set<T> visited = new HashSet<>();

        queue.add(new TypeNode<>(bA, 0));
        visited.add(bA);

        while (!queue.isEmpty()) {
            TypeNode<T> current = queue.poll();

            // If we found the target, return the accumulated distance
            if (Objects.equals(current.type, bE)) {
                return current.distance;
            }

            // Add Superclass (Weight: +2 per level to favor classes over interfaces)
            T sc = d.getSuperType(current.type);
            if (sc != null && visited.add(sc)) {
                queue.add(new TypeNode<>(sc, current.distance + 2));
            }

            // Add Interfaces (Weight: +10 per level to deprioritize generic interfaces)
            T[] interfaces = d.getInterfaces(current.type);
            if (interfaces != null) {
                for (T itf : interfaces) {
                    if (itf != null && visited.add(itf)) {
                        queue.add(new TypeNode<>(itf, current.distance + 10));
                    }
                }
            }
        }

        return Integer.MAX_VALUE; // No path found
    }

    // Simple internal helper to track distance during traversal
    private static class TypeNode<T> {
        final T type;
        final int distance;
        TypeNode(T type, int distance) { this.type = type; this.distance = distance; }
    }
}
