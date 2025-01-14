package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.*;

import java.util.HashMap;
import java.util.Map;

public enum NExprCommonOp implements NEnum {
    PLUS("+")
    ,MINUS("-")
    ,MUL("*")
    ,DIV("/")
    ,REM("%")
    ,XOR("^")
    ,POW("**")
    ,OR_BITS("|")
    ,AND_BITS("&")
    ,OR("||")
    ,AND("&&")
    ,EQ("==")
    ,NOT("!")
    ,LT("<")
    ,GT(">")
    ,LTE("<=")
    ,GTE(">=")
    ,NE("!=")
    ,DOT(".")
    ,ASSIGN("=")
    ;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    private final String image;
    private final static Map<String,NExprCommonOp> byImage=new HashMap<>();
    static{
        for (NExprCommonOp value : values()) {
            byImage.put(value.image, value);
        }
    }

    NExprCommonOp(String image) {
        this.id = NNameFormat.ID_NAME.format(name());
        this.image = image;
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
