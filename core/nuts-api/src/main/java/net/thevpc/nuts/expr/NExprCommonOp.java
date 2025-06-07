package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public enum NExprCommonOp implements NEnum {
    PLUS("+"), MINUS("-"), MUL("*"), DIV("/"), REM("%"), XOR("^"), POW("**"), OR_BITS("|"), AND_BITS("&"), OR("||"), AND("&&"), EQ("=="), NOT("!"), LT("<"), GT(">"), LTE("<="), GTE(">="), NE("!="), DOT("."), ASSIGN("=");
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    private final String image;
    private final String[] aliases;
    private final static Map<String, NExprCommonOp> byImage = new HashMap<>();

    static {
        for (NExprCommonOp value : values()) {
            HashSet<String> u = new HashSet<>(Arrays.asList(value.name().toLowerCase(), value.id().toLowerCase(), value.image.toLowerCase()));
            for (String alias : value.aliases) {
                u.add(NStringUtils.trim(alias.toLowerCase()));
            }
            for (String s : u) {
                register(s, value);
            }
        }
    }

    private static void register(String id, NExprCommonOp value) {
        if (byImage.containsKey(id)) {
            throw new IllegalArgumentException("duplicate image: " + value.name());
        } else {
            byImage.put(id, value);
        }
    }

    NExprCommonOp(String image, String... aliases) {
        this.id = NNameFormat.ID_NAME.format(name());
        this.image = image;
        this.aliases = aliases;
    }

    public static NOptional<NExprCommonOp> parse(String value) {
        return NEnumUtils.parseEnum(value, NExprCommonOp.class, s -> {
            NExprCommonOp u = byImage.get(NStringUtils.trim(value).toLowerCase());
            return NOptional.of(u);
        });
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    public String image() {
        return image;
    }
}
