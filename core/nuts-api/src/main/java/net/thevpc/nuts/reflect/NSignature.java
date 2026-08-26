package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NOptional;

/**
 * NSignature interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSignature<T,A extends NSignature<T,?>> {
    /**
     * Name.
     *
     * @return name result
     */
    NOptional<String> name();
    /**
     * Converts to unnamed.
     *
     * @return to unnamed result
     */
    A toUnnamed();
    /**
     * Converts to named.
     *
     * @param newName new name
     * @return to named result
     */
    A toNamed(String newName);
    /**
     * Checks if is named.
     *
     * @return is named result
     */
    boolean isNamed();
    /**
     * Returns the type.
     *
     * @param index index
     * @return get type result
     */
    T getType(int index);
    /**
     * Sets the vararg.
     *
     * @param vararg vararg
     * @return set vararg result
     */
    A setVararg(boolean vararg);
    /**
     * Sets the set.
     *
     * @param any any
     * @param pos pos
     * @return set result
     */
    A set(T any, int pos);
    /**
     * Size.
     *
     * @return size result
     */
    int size();
    /**
     * Types.
     *
     * @return types result
     */
    T[] types();
    /**
     * Checks if is var args.
     *
     * @return is var args result
     */
    boolean isVarArgs();
    /**
     * Matches.
     *
     * @param other other
     * @return matches result
     */
    boolean matches(A other);
    /**
     * Calculate score.
     *
     * @param other other
     * @return calculate score result
     */
    NSignatureScore calculateScore(A other);
    /**
     * Domain.
     *
     * @return domain result
     */
    NSignatureDomain<T> domain();
}
