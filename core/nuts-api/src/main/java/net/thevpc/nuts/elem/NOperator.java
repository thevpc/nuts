//package net.thevpc.nuts.elem;
//
//import net.thevpc.nuts.util.NEnum;
//import net.thevpc.nuts.util.NEnumUtils;
//import net.thevpc.nuts.util.NNameFormat;
//import net.thevpc.nuts.util.NOptional;
//
//public enum NOperator implements NEnum {
//    EQ,
//    PLUS,
//    MINUS;
//
//    /**
//     * lower-cased identifier for the enum entry
//     */
//    private final String id;
//
//    NOperator() {
//        this.id = NNameFormat.ID_NAME.format(name());
//    }
//
//    public static NOptional<NOperator> parse(String value) {
//        return NEnumUtils.parseEnum(value, NOperator.class, ev -> {
//            switch (ev.getNormalizedValue()) {
//                case "+":
//                    return NOptional.of(NOperator.PLUS);
//                case "-":
//                    return NOptional.of(NOperator.MINUS);
//                case "=":
//                    return NOptional.of(NOperator.EQ);
//            }
//            return null;
//        });
//    }
//
//    public NOperatorType type() {
//        switch (this) {
//            case EQ:
//            case PLUS:
//            case MINUS: {
//                return NOperatorType.BINARY_INFIX;
//            }
//        }
//        return NOperatorType.UNARY_PREFIX;
//    }
//
//    public NElementType elementType() {
//        switch (this) {
//            case EQ:
//            case PLUS:
//            case MINUS: {
//                return NOperatorType.BINARY_INFIX;
//            }
//        }
//        return NOperatorType.UNARY_PREFIX;
//    }
//
//    @Override
//    public String id() {
//        return id();
//    }
//}
