package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * NAssert class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public final class NAssert {
    /**
     * N assert.
     *
     * @return n assert result
     */
    private NAssert() {
    }

    /**
     * Creates a new instance of create message.
     *
     * @param msg msg
     * @return create message result
     */
    private static NMsg createMessage(Supplier<NMsg> msg) {
      /**
       * Require named non null.
       *
       * @param msg msg
       * @param supplier" supplier"
       */
        requireNamedNonNull(msg, "message supplier");
        NMsg m = msg.get();
      /**
       * Require named non null.
       *
       * @param m m
       * @param "message" "message"
       */
        requireNamedNonNull(m, "message");
        return m;
    }

    /**
     * Creates a new instance of create message.
     *
     * @param name name
     * @return create message result
     */
    private static String createMessage(String name) {
        return NBlankable.isBlank(name) ? "value" : name;
    }

    /**
     * Creates a new instance of create message.
     *
     * @param name name
     * @return create message result
     */
    private static NMsg createMessage(NMsg name) {
        return NBlankable.isBlank(name) ? NMsg.ofC("value") : name;
    }

    /**
     * Require non null.
     *
     * @param object object
     * @param msg msg
     * @return require non null result
     */
    public static <T> T requireNonNull(T object, Supplier<NMsg> msg) {
        if (object == null) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }


    /**
     * Require named non null.
     *
     * @param object object
     * @param name name
     * @return require named non null result
     */
    public static <T> T requireNamedNonNull(T object, String name) {
        /**
         * Require non null.
         *
         * @param object object
         * @param null" null"
         * @param createMessage(name)) create message(name))
         * @return require non null result
         */
        return requireNonNull(object, () -> NMsg.ofC("%s should not be null", createMessage(name)));
    }

    /**
     * Require named non null.
     *
     * @param object object
     * @param name name
     * @return require named non null result
     */
    public static <T> T requireNamedNonNull(T object, NMsg name) {
        /**
         * Require non null.
         *
         * @param object object
         * @param null" null"
         * @param createMessage(name)) create message(name))
         * @return require non null result
         */
        return requireNonNull(object, () -> NMsg.ofC("%s should not be null", createMessage(name)));
    }

    /**
     * Require named non null.
     *
     * @param object object
     * @param name name
     * @return require named non null result
     */
    public static <T> T requireNamedNonNull(T object, Supplier<NMsg> name) {
        /**
         * Require non null.
         *
         * @param object object
         * @param null" null"
         * @param createMessage(name)) create message(name))
         * @return require non null result
         */
        return requireNonNull(object, () -> NMsg.ofC("%s should not be null", createMessage(name)));
    }

    /**
     * Require named non null.
     *
     * @param object object
     * @return require named non null result
     */
    public static <T> T requireNamedNonNull(T object) {
        /**
         * Require named non null.
         *
         * @param object object
         * @param "value" "value"
         * @return require named non null result
         */
        return requireNamedNonNull(object, "value");
    }


    /**
     * Require named null.
     *
     * @param object object
     * @param name name
     */
    public static void requireNamedNull(Object object, String name) {
        if (object != null) {
            throw NException.ofSafeAssertException(NMsg.ofC("%s must be null", createMessage(name)));
        }
    }

    /**
     * Require named null.
     *
     * @param object object
     * @param name name
     */
    public static void requireNamedNull(Object object, NMsg name) {
        if (object != null) {
            throw NException.ofSafeAssertException(NMsg.ofC("%s must be null", createMessage(name)));
        }
    }

    /**
     * Require named null.
     *
     * @param object object
     * @param name name
     */
    public static void requireNamedNull(Object object, Supplier<NMsg> name) {
        if (object != null) {
            throw NException.ofSafeAssertException(NMsg.ofC("%s must be null", createMessage(name)));
        }
    }

    /**
     * Require null.
     *
     * @param object object
     * @param message message
     */
    public static void requireNull(Object object, Supplier<NMsg> message) {
        if (object != null) {
            throw NException.ofSafeAssertException(createMessage(message));
        }
    }


    /**
     * Require named non blank.
     *
     * @param object object
     * @param name name
     * @return require named non blank result
     */
    public static <T> T requireNamedNonBlank(T object, String name) {
        if (NBlankable.isBlank(object)) {
            throw NException.ofSafeAssertException(NMsg.ofC("%s should not be blank", createMessage(name)));
        }
        return object;
    }

    /**
     * Require named non blank.
     *
     * @param object object
     * @param name name
     * @return require named non blank result
     */
    public static <T> T requireNamedNonBlank(T object, NMsg name) {
        if (NBlankable.isBlank(object)) {
            throw NException.ofSafeAssertException(NMsg.ofC("%s should not be blank", NUtils.firstNonNull(name, "value")));
        }
        return object;
    }

    /**
     * Require named non blank.
     *
     * @param object object
     * @param name name
     * @return require named non blank result
     */
    public static <T> T requireNamedNonBlank(T object, Supplier<NMsg> name) {
        if (NBlankable.isBlank(object)) {
            throw NException.ofSafeAssertException(NMsg.ofC("%s should not be blank", createMessage(name)));
        }
        return object;
    }


    /**
     * Require non blank.
     *
     * @param object object
     * @param msg msg
     * @return require non blank result
     */
    public static <T> T requireNonBlank(T object, Supplier<NMsg> msg) {
        if (NBlankable.isBlank(object)) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }


    // NO SESSION


    /**
     * Require named null.
     *
     * @param object object
     */
    public static void requireNamedNull(Object object) {
      /**
       * Require named null.
       *
       * @param object object
       * @param null null
       */
        requireNamedNull(object, (String) null);
    }

    /**
     * Require named true.
     *
     * @param value value
     * @param name name
     * @return require named true result
     */
    public static boolean requireNamedTrue(boolean value, String name) {
        /**
         * Require true.
         *
         * @param value value
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value, () -> NMsg.ofC("should be %s", createMessage(name)));
    }

    /**
     * Require named true.
     *
     * @param value value
     * @param name name
     * @return require named true result
     */
    public static boolean requireNamedTrue(boolean value, NMsg name) {
        /**
         * Require true.
         *
         * @param value value
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value, () -> NMsg.ofC("should be %s", createMessage(name)));
    }


    /**
     * Require true.
     *
     * @param object object
     * @param msg msg
     * @return require true result
     */
    public static boolean requireTrue(boolean object, Supplier<NMsg> msg) {
        if (!object) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }

    /**
     * Require named false.
     *
     * @param value value
     * @param name name
     * @return require named false result
     */
    public static boolean requireNamedFalse(boolean value, String name) {
        /**
         * Require false.
         *
         * @param value value
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require false result
         */
        return requireFalse(value, () -> NMsg.ofC("should not be %s", createMessage(name)));
    }

    /**
     * Require named false.
     *
     * @param value value
     * @param name name
     * @return require named false result
     */
    public static boolean requireNamedFalse(boolean value, NMsg name) {
        /**
         * Require true.
         *
         * @param value value
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value, () -> NMsg.ofC("should not be %s", createMessage(name)));
    }


    /**
     * Require named false.
     *
     * @param object object
     * @param msg msg
     * @return require named false result
     */
    public static boolean requireNamedFalse(boolean object, Supplier<NMsg> msg) {
        if (object) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }

    /**
     * Require equals.
     *
     * @param a a
     * @param b b
     * @param msg msg
     * @return require equals result
     */
    public static <T> T requireEquals(T a, T b, Supplier<NMsg> msg) {
        if (!Objects.equals(a, b)) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return a;
    }

    /**
     * Require named not equals.
     *
     * @param a a
     * @param b b
     * @param name name
     * @return require named not equals result
     */
    public static <T> T requireNamedNotEquals(T a, T b, String name) {
        /**
         * Require not equals.
         *
         * @param a a
         * @param b b
         * @param failed" failed"
         * @param createMessage(name)) create message(name))
         * @return require not equals result
         */
        return requireNotEquals(a, b, () -> NMsg.ofC("%s non equality failed", createMessage(name)));
    }

    /**
     * Require named not equals.
     *
     * @param a a
     * @param b b
     * @param name name
     * @return require named not equals result
     */
    public static <T> T requireNamedNotEquals(T a, T b, NMsg name) {
        /**
         * Require not equals.
         *
         * @param a a
         * @param b b
         * @param failed" failed"
         * @param createMessage(name)) create message(name))
         * @return require not equals result
         */
        return requireNotEquals(a, b, () -> NMsg.ofC("%s non equality failed", createMessage(name)));
    }

    /**
     * Require named not equals.
     *
     * @param a a
     * @param b b
     * @param name name
     * @return require named not equals result
     */
    public static <T> T requireNamedNotEquals(T a, T b, Supplier<NMsg> name) {
        /**
         * Require not equals.
         *
         * @param a a
         * @param b b
         * @param failed" failed"
         * @param createMessage(name)) create message(name))
         * @return require not equals result
         */
        return requireNotEquals(a, b, () -> NMsg.ofC("%s non equality failed", createMessage(name)));
    }

    /**
     * Require not equals.
     *
     * @param a a
     * @param b b
     * @param msg msg
     * @return require not equals result
     */
    public static <T> T requireNotEquals(T a, T b, Supplier<NMsg> msg) {
        if (Objects.equals(a, b)) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return a;
    }

    /**
     * Require named equals.
     *
     * @param a a
     * @param b b
     * @param name name
     * @return require named equals result
     */
    public static <T> T requireNamedEquals(T a, T b, String name) {
        /**
         * Require equals.
         *
         * @param a a
         * @param b b
         * @param failed" failed"
         * @param createMessage(name)) create message(name))
         * @return require equals result
         */
        return requireEquals(a, b, () -> NMsg.ofC("%s equality failed", createMessage(name)));
    }

    /**
     * Require named equals.
     *
     * @param a a
     * @param b b
     * @param name name
     * @return require named equals result
     */
    public static <T> T requireNamedEquals(T a, T b, NMsg name) {
        /**
         * Require equals.
         *
         * @param a a
         * @param b b
         * @param failed" failed"
         * @param createMessage(name)) create message(name))
         * @return require equals result
         */
        return requireEquals(a, b, () -> NMsg.ofC("%s equality failed", createMessage(name)));
    }

    /**
     * Require named equals.
     *
     * @param a a
     * @param b b
     * @param name name
     * @return require named equals result
     */
    public static <T> T requireNamedEquals(T a, T b, Supplier<NMsg> name) {
        /**
         * Require equals.
         *
         * @param a a
         * @param b b
         * @param failed" failed"
         * @param createMessage(name)) create message(name))
         * @return require equals result
         */
        return requireEquals(a, b, () -> NMsg.ofC("%s equality failed", createMessage(name)));
    }


    /**
     * Require false.
     *
     * @param value value
     * @param msg msg
     * @return require false result
     */
    public static boolean requireFalse(boolean value, Supplier<NMsg> msg) {
        if (value) {
            throw NException.ofSafeAssertException(createMessage(msg));
        }
        return value;
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(Collection<?> value, String name) {
        /**
         * Require true.
         *
         * @param !value.isEmpty() !value.is empty()
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(!value.isEmpty(), () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(Collection<?> value, NMsg name) {
        /**
         * Require true.
         *
         * @param !value.isEmpty() !value.is empty()
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(!value.isEmpty(), () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(Collection<?> value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param !value.isEmpty() !value.is empty()
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(!value.isEmpty(), () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(Collection<?> value, String name) {
        /**
         * Require true.
         *
         * @param value.isEmpty() value.is empty()
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.size()) value.size())
         * @return require true result
         */
        return requireTrue(value.isEmpty(), () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value == null ? "null" : value.size()));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(Collection<?> value, NMsg name) {
        /**
         * Require true.
         *
         * @param value.isEmpty() value.is empty()
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.size()) value.size())
         * @return require true result
         */
        return requireTrue(value.isEmpty(), () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value == null ? "null" : value.size()));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(Collection<?> value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param value.isEmpty() value.is empty()
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.size()) value.size())
         * @return require true result
         */
        return requireTrue(value.isEmpty(), () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value == null ? "null" : value.size()));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(CharSequence value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length() != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(CharSequence value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length() != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(CharSequence value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length() != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(CharSequence value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value) value)
         * @return require true result
         */
        return requireTrue(value.length() == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(CharSequence value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value) value)
         * @return require true result
         */
        return requireTrue(value.length() == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(CharSequence value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value) value)
         * @return require true result
         */
        return requireTrue(value.length() == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(Object[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(Object[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(Object[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(Object[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(Object[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(Object[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(boolean[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(boolean[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(boolean[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(boolean[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(boolean[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(boolean[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(byte[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(byte[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(byte[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(byte[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(byte[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(byte[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(short[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(short[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(short[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(short[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(short[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(short[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(char[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(char[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(char[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(char[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(char[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(char[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(int[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(int[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(int[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(int[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(int[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(int[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(long[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(long[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(long[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(long[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(long[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(long[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(float[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(float[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(float[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(float[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(float[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(float[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(double[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(double[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named non empty.
     *
     * @param value value
     * @param name name
     * @return require named non empty result
     */
    public static boolean requireNamedNonEmpty(double[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s" %s"
         * @param createMessage(name)) create message(name))
         * @return require true result
         */
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createMessage(name)));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(double[] value, String name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(double[] value, NMsg name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

    /**
     * Require named empty.
     *
     * @param value value
     * @param name name
     * @return require named empty result
     */
    public static boolean requireNamedEmpty(double[] value, Supplier<NMsg> name) {
        /**
         * Require true.
         *
         * @param 0 0
         * @param %s %s
         * @param %s" %s"
         * @param createMessage(name) create message(name)
         * @param value.length) value.length)
         * @return require true result
         */
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createMessage(name), value.length));
    }

}
