//package net.thevpc.nuts.runtime.standalone.util.fprint.parser.steps;
//
//import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
//import net.thevpc.nuts.runtime.standalone.util.fprint.parser.DefaultNutsTextNodeParser;
//import net.thevpc.nuts.runtime.standalone.util.fprint.parser.FDocNode;
//import net.thevpc.nuts.runtime.standalone.util.fprint.parser.NutsTextNode;
//
//public class QuotedParserStepOld extends ParserStep {
//
//    int status = 0;
//    boolean escape = false;
//    StringBuilder start = new StringBuilder();
//    StringBuilder end = new StringBuilder();
//    StringBuilder value = new StringBuilder();
//    int maxSize = 3;
//
//    public QuotedParserStepOld(char c) {
//        start.append(c);
//    }
//
//    public QuotedParserStepOld(String c) {
//        start.append(c);
//    }
//
//    @Override
//    public void consume(char c, DefaultNutsTextNodeParser.State p) {
//        switch (status) {
//            case 0: {
//                if (c == start.charAt(0)) {
//                    if (start.length() < maxSize) {
//                        start.append(c);
//                    } else {
//                        status = 2;
//                        end.append(c);
//                        p.applyPop();
//                    }
//                } else {
//                    switch (c) {
//                        case '\\': {
//                            escape = true;
//                            break;
//                        }
//                        default: {
//                            value.append(c);
//                        }
//                    }
//                    status = 1;
//                }
////                    p.applyContinue();
//                return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
//            }
//            case 1: {
//                if (escape) {
//                    escape = false;
//                    value.append(c);
//                } else {
//                    if (c == start.charAt(0)) {
//                        status = 2;
//                        end.append(c);
//                        if (end.length() >= start.length()) {
//                            p.applyPop();
//                            return;//new ConsumeResut(ConsumeResutType.POP, null);
//                        }
//                    } else {
//                        switch (c) {
//                            case '\\': {
//                                escape = true;
//                                break;
//                            }
//                            default: {
//                                value.append(c);
//                            }
//                        }
//                    }
//                }
//                return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
//            }
//            case 2: {
//                if (c == start.charAt(0)) {
//                    end.append(c);
//                    if (end.length() >= start.length()) {
//                        p.applyPop();
//                        return;//new ConsumeResut(ConsumeResutType.POP, null);
//                    } else {
//                        return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
//                    }
//                } else {
//                    value.append(end);
//                    end.delete(0, end.length());
//                    switch (c) {
//                        case '\\': {
//                            escape = true;
//                            break;
//                        }
//                        default: {
//                            value.append(c);
//                        }
//                    }
//                    status = 1;
//                    return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
//                }
//            }
//        }
//        throw new IllegalArgumentException("Unexpected");
//    }
//
//    @Override
//    public void appendChild(ParserStep tt) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public NutsTextNode toFDocNode() {
//        return new FDocNode.Escaped(start.toString(), end.toString(), value.toString(), !isComplete());
//    }
//
//    @Override
//    public void end(DefaultNutsTextNodeParser.State p) {
//        if(!isComplete()) {
//            while (end.length() < start.length()) {
//                end.append(start.charAt(0));
//            }
//        }
//        p.applyPop();
//    }
//
//    public boolean isComplete() {
//        return status == 2 && end.length() == start.length();
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("Quoted(" + CoreStringUtils.dblQuote(start.toString()));
//        sb.append(",");
//        sb.append(CoreStringUtils.dblQuote(value.toString()));
//        sb.append(",status=").append(status == 0 ? "EXPECT_START" : status == 1 ? "EXPECT_CONTENT" : status == 2 ? "EXPECT_END" : String.valueOf(status));
//        sb.append(",end=");
//        sb.append(end);
//        if (escape) {
//            sb.append(",<ESCAPED>");
//        }
//        sb.append(isComplete() ? "" : ",incomplete");
//        return sb.append(")").toString();
//    }
//}
