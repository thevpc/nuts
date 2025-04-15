package net.thevpc.nuts.runtime.standalone.tson.impl.parser.javacc;

import net.thevpc.nuts.runtime.standalone.tson.*;

public class CustomJavaccTsonStreamParserImpl implements TsonStreamParser {
    private SimpleCharStream jj_input_stream;
    private TsonStreamParserImplTokenManager token_source;
    private TsonStreamParserConfig config;
    private TsonParserVisitor visitor;
    private Token last;
    private Object source;

    public Object source(){
        return source;
    }
    public void source(Object source){
        this.source=source;
    }
    public CustomJavaccTsonStreamParserImpl(java.io.InputStream stream) {
        this(stream, null);
    }

    public CustomJavaccTsonStreamParserImpl(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new TsonStreamParserImplTokenManager(jj_input_stream);
    }

    public CustomJavaccTsonStreamParserImpl(java.io.Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new TsonStreamParserImplTokenManager(jj_input_stream);
    }


    @Override
    public void setConfig(TsonStreamParserConfig config) {
        this.config = config;
        this.visitor = config.getVisitor();
    }

    @Override
    public void parseElement() {
        elementLevel2();
    }

    @Override
    public void parseDocument() {
        elementLevel2();
        visitor.visitDocumentEnd();
    }

    void elementLevel2() {
        visitor.visitInstructionStart();
        elementLevel1();
        Token nextToken = nextToken();
        switch (nextToken.kind) {
            case TsonStreamParserImplConstants.COLON: {
                elementLevel1();
                visitor.visitKeyValueEnd();
                break;
            }
            default: {
                pushBackToken(nextToken);
                visitor.visitSimpleEnd();
            }
        }
    }

    private void pushBackToken(Token t) {
        last = t;
    }

    private Token nextToken() {
        if (last != null) {
            Token t = last;
            last = null;
            return t;
        }
        return token_source.getNextToken();
    }

    void annotations() {
        while (true) {
            Token token = nextToken();
            if (token.kind == TsonStreamParserImplConstants.AT) {
                token = nextToken();
                String annName = null;
                if (token.kind == TsonStreamParserImplConstants.NAME) {
                    annName = token.image;
                    token = nextToken();
                }
                visitor.visitAnnotationStart(annName);
                if (token.kind == TsonStreamParserImplConstants.LPAREN) {
                    token = nextToken();
                    if (token.kind == TsonStreamParserImplConstants.RPAREN) {
                        //
                    } else {
                        pushBackToken(token);
                        visitor.visitAnnotationParamStart();
                        elementLevel2();
                        visitor.visitAnnotationParamEnd();
                        boolean repeat = true;
                        while (repeat) {
                            token = nextToken();
                            if (token.kind == TsonStreamParserImplConstants.COMMA) {
                                visitor.visitAnnotationParamStart();
                                elementLevel2();
                                visitor.visitAnnotationParamEnd();
                            } else if (token.kind == TsonStreamParserImplConstants.RPAREN) {
                                repeat = false;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        }

                    }
                } else {
                    throw new IllegalArgumentException();
                }
                visitor.visitAnnotationEnd();
            } else {
                pushBackToken(token);
                return;
            }
        }
    }

    void elementLevel1() {
        visitor.visitElementStart();
        Token token;
//        Token token = nextToken();
//        if (
//                token.kind == TsonStreamParserImplConstants.ML_COMMENT
//                || token.kind == TsonStreamParserImplConstants.SL_COMMENT
//        ) {
//            visitor.visitComments(TsonParserUtils.escapeComments(token.image));
//        } else {
//            pushBackToken(token);
//        }

        annotations();
        token = nextToken();
        switch (token.kind) {
            case TsonStreamParserImplConstants.NULL: {
                visitor.visitPrimitiveEnd(Tson.ofNull());
                break;
            }
            case TsonStreamParserImplConstants.TRUE: {
                visitor.visitPrimitiveEnd(Tson.ofBoolean(true));
                break;
            }
            case TsonStreamParserImplConstants.FALSE: {
                visitor.visitPrimitiveEnd(Tson.ofBoolean(false));
                break;
            }
            case TsonStreamParserImplConstants.DATETIME: {
                visitor.visitPrimitiveEnd(Tson.parseLocalDateTime(token.image));
                break;
            }
            case TsonStreamParserImplConstants.DATE: {
                visitor.visitPrimitiveEnd(Tson.parseLocalDate(token.image));
                break;
            }
            case TsonStreamParserImplConstants.TIME: {
                visitor.visitPrimitiveEnd(Tson.parseLocalTime(token.image));
                break;
            }
            case TsonStreamParserImplConstants.REGEX: {
                visitor.visitPrimitiveEnd(Tson.parseRegex(token.image));
                break;
            }
            case TsonStreamParserImplConstants.NUMBER:
            {
                visitor.visitPrimitiveEnd(Tson.parseNumber(token.image));
                break;
            }

            case TsonStreamParserImplConstants.SINGLE_QUOTE_STR: {
                visitor.visitPrimitiveEnd(Tson.parseString(token.image));
                break;
            }
            case TsonStreamParserImplConstants.ANTI_QUOTE_STR: {
                visitor.visitPrimitiveEnd(Tson.parseString(token.image));
                break;
            }
            case TsonStreamParserImplConstants.DOUBLE_QUOTE_STR: {
                visitor.visitPrimitiveEnd(Tson.parseString(token.image));
                break;
            }
            case TsonStreamParserImplConstants.TRIPLE_SINGLE_QUOTE_STR: {
                visitor.visitPrimitiveEnd(Tson.parseString(token.image));
                break;
            }
            case TsonStreamParserImplConstants.TRIPLE_DOUBLE_QUOTE_STR: {
                visitor.visitPrimitiveEnd(Tson.parseString(token.image));
                break;
            }
            case TsonStreamParserImplConstants.TRIPLE_ANTI_QUOTE_STR: {
                visitor.visitPrimitiveEnd(Tson.parseString(token.image));
                break;
            }
            case TsonStreamParserImplConstants.ALIAS: {
                visitor.visitPrimitiveEnd(Tson.parseAlias(token.image));
                break;
            }
            case TsonStreamParserImplConstants.NAME: {
                Token nt = nextToken();
                visitor.visitNamedStart(nt.image);
                switch (nt.kind) {
                    case TsonStreamParserImplConstants.LPAREN: {
                        pushBackToken(token);
                        parStart(true);
                        break;
                    }
                    case TsonStreamParserImplConstants.LBRACK: {
                        arrStart(true);
                        break;
                    }
                    case TsonStreamParserImplConstants.LBRACE: {
                        objStart(true);
                        break;
                    }
                    default: {
                        visitor.visitPrimitiveEnd(Tson.ofName(nt.image));
                    }
                }
                break;
            }
            case TsonStreamParserImplConstants.LPAREN: {
                pushBackToken(token);
                parStart(false);
                break;
            }
            case TsonStreamParserImplConstants.LBRACK: {
                pushBackToken(token);
                arrStart(false);
                break;
            }
            case TsonStreamParserImplConstants.LBRACE: {
                pushBackToken(token);
                objStart(false);
                break;
            }
        }
    }

    private void parStart(boolean named) {
        visitor.visitParamsStart();
        Token token = nextToken(); //read (
        visitor.visitParamElementStart();
        elementLevel2();
        visitor.visitParamElementEnd();
        boolean repeat = true;
        while (repeat) {
            token = nextToken();
            switch (token.kind) {
                case TsonStreamParserImplConstants.COMMA: {
                    visitor.visitParamElementStart();
                    elementLevel2();
                    visitor.visitParamElementEnd();
                    break;
                }
                case TsonStreamParserImplConstants.RPAREN: {
                    repeat = false;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unexpected " + token);
                }
            }
        }
        visitor.visitParamsEnd();

        token = nextToken();
        switch (token.kind) {
            case TsonStreamParserImplConstants.LBRACK: {
                arrStart(true);
                break;
            }
            case TsonStreamParserImplConstants.LBRACE: {
                objStart(true);
                break;
            }
            default: {
                visitor.visitUpletEnd();
            }
        }
    }

    private void arrStart(boolean named) {
        if (named) {
            visitor.visitNamedArrayStart();
        } else {
            visitor.visitArrayStart();
        }
        Token token = nextToken(); //read [
        token = nextToken();
        if (token.kind == TsonStreamParserImplConstants.RBRACK) {
            //
        } else {
            pushBackToken(token);
            visitor.visitArrayElementStart();
            elementLevel2();
            visitor.visitArrayElementEnd();
            boolean repeat = true;
            while (repeat) {
                token = nextToken();
                switch (token.kind) {
                    case TsonStreamParserImplConstants.COMMA: {
                        visitor.visitArrayElementStart();
                        elementLevel2();
                        visitor.visitArrayElementEnd();
                        break;
                    }
                    case TsonStreamParserImplConstants.RBRACK: {
                        repeat = false;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected " + token);
                    }
                }
            }
        }
        if (named) {
            visitor.visitNamedArrayEnd();
        } else {
            visitor.visitArrayEnd();
        }
    }

    private void objStart(boolean named) {
        if (named) {
            visitor.visitNamedObjectStart();
        } else {
            visitor.visitObjectStart();
        }
        Token token = nextToken(); //read {
        token = nextToken();
        if (token.kind == TsonStreamParserImplConstants.RBRACE) {
            //
        } else {
            pushBackToken(token);
            visitor.visitObjectElementStart();
            elementLevel2();
            visitor.visitObjectElementEnd();
            boolean repeat = true;
            while (repeat) {
                token = nextToken();
                switch (token.kind) {
                    case TsonStreamParserImplConstants.COMMA: {
                        visitor.visitObjectElementStart();
                        elementLevel2();
                        visitor.visitObjectElementEnd();
                        break;
                    }
                    case TsonStreamParserImplConstants.RBRACE: {
                        repeat = false;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected " + token);
                    }
                }
            }
        }
        if (named) {
            visitor.visitNamedObjectEnd();
        } else {
            visitor.visitObjectEnd();
        }
    }

    void run() {

    }
}
