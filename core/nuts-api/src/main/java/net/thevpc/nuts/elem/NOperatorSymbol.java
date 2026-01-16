package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum NOperatorSymbol implements NEnum {
    PLUS("+"),
    PLUS2("++"),
    PLUS3("+++"),
    PLUS_EQ("+="),
    PLUS_EQ2("+=="),
    MINUS("-"),
    MINUS2("--"),
    MINUS3("---"),
    MINUS_EQ("-="),
    MINUS_EQ2("-=="),
    MUL("*"),
    MUL2("**"),
    MUL3("***"),
    MUL_EQ("*="),
    MUL_EQ2("*=="),
    DIV("/"),
    HAT("^"),
    HAT2("^^"),
    HAT3("^^^"),
    REM("%"),
    REM2("%%"),
    REM3("%%%"),
    EQ("="),
    EQ2("=="),
    EQ3("==="),
    TILDE("~"),
    TILDE2("~~"),
    TILDE3("~~~"),
    TILDE_EQ2("~=="),
    TILDE_EQ("~="),
    LT("<"),
    LT2("<<"),
    LT_GT("<>"),
    LT3("<<<"),
    GT(">"),
    GT2(">>"),
    GT3(">>>"),
    LTE("<="),
    GTE(">="),
    COLON2("::"),
    COLON_EQ(":="),
    COLON_EQ2(":=="),
    MINUS_GT("->"),
    MINUS2_GT("-->"),
    EQ_GT("=>"),
    EQ2_GT("==>"),
    LT_MINUS2("<--"),
    LT_EQ2("<=="),
    PIPE("|"),
    PIPE2("||"),
    PIPE3("|||"),
    AND("&"),
    AND2("&&"),
    AND3("&&&"),
    HASH("#"),
    HASH2("##"),
    HASH3("###"),
    HASH4("####"),
    HASH5("#####"),
    HASH6("######"),
    HASH7("#######"),
    HASH8("########"),
    HASH9("#########"),
    HASH10("##########"),
    INTERROGATION("?"),
    INTERROGATION2("??"),
    INTERROGATION3("???"),
    EXCLAMATION("!"),
    EXCLAMATION2("!!"),
    EXCLAMATION_EQ2("!=="),
    EXCLAMATION_EQ("!="),
    EXCLAMATION3("!!!"),
    AT2("@@"),
    AT3("@@");

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    private final String lexeme;
    private static final Map<String, NOperatorSymbol> BY_LEXEME;

    static {
        Map<String, NOperatorSymbol> m = new HashMap<>();
        for (NOperatorSymbol op : values()) {
            m.put(op.lexeme, op);
        }
        BY_LEXEME = Collections.unmodifiableMap(m); // immutable if you like safety
    }

    NOperatorSymbol(String lexeme) {
        this.id = NNameFormat.ID_NAME.format(name());
        this.lexeme = lexeme;
    }

    public static NOptional<NOperatorSymbol> parse(String value) {
        return NEnumUtils.parseEnum(value, NOperatorSymbol.class, new Function<NEnumUtils.EnumValue, NOptional<NOperatorSymbol>>() {
            @Override
            public NOptional<NOperatorSymbol> apply(NEnumUtils.EnumValue enumValue) {
                NOperatorSymbol u = BY_LEXEME.get(enumValue.getValue().trim());
                if (u != null) {
                    return NOptional.of(u);
                }
                return null;
            }
        });
    }

    public String lexeme() {
        return lexeme;
    }

    @Override
    public String id() {
        return id;
    }
}
