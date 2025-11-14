package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.elem.*;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

class YamlParser {
    private YamlTokenizer tokenizer;
    private YamlElement unread;

    private static class YamlElement {
        NElement element;
        int indentation;

        public YamlElement(NElement element, int indentation) {
            this.element = element;
            this.indentation = indentation;
        }
    }

    public NElement parseElement(Reader reader) {
        this.tokenizer = new YamlTokenizer(reader);
        List<NElement> all = new ArrayList<>();
        while (true) {
            YamlElement e = parseAny(0, AsWhat.ANY);
            if (e == null) {
                break;
            }
            all.add(e.element);
        }
        if (all.size() == 0) {
            return NElement.ofNull();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return NElement.ofArray(all.toArray(new NElement[0]));
    }

    private NElement parseAnyNoIndentation() {
        YamlToken t = tokenizer.next();
        if (t == null) {
            return null;
        } else {
            switch (t.type) {
                case OPEN_BRACKET: {
                    return parseArrayNoIndent(t);
                }
                case OPEN_BRACE: {
                    return parseObjectNoIndent(t);
                }
                case NAME:
                case DOUBLE_STRING:
                case SINGLE_STRING:
                case NULL:
                case INTEGER:
                case DECIMAL:
                case TRUE:
                case FALSE:
                case OPEN_STRING: {
                    return valElement(t);
                }
                default: {
                    throw new IllegalArgumentException("unexpected token " + t.type);
                }
            }
        }
    }

    private static enum AsWhat {
        KEY, VALUE,
        ANY
    }

    private YamlElement parseAny(int indentation, AsWhat asVal) {
        if (unread != null) {
            if (unread.indentation >= indentation) {
                return unread;
            }
            return null;
        }
        YamlToken t = tokenizer.next();
        if (t == null) {
            return null;
        } else {
            switch (t.type) {
                case DASH: {
                    if(asVal == AsWhat.KEY) {
                        throw new IllegalArgumentException("no allowed key as array element ");
                    }
                    YamlElement ee = parseArrayByDash(t);
                    return checkIndentation(ee, indentation);
                }
                case OPEN_BRACKET: {
                    if(asVal == AsWhat.KEY) {
                        throw new IllegalArgumentException("no allowed key as array element ");
                    }
                    NElement u = parseArrayNoIndent(t);
                    YamlElement ee = new YamlElement(u, indentation);
                    return checkIndentation(ee, indentation);
                }
                case OPEN_BRACE: {
                    if(asVal == AsWhat.KEY) {
                        throw new IllegalArgumentException("no allowed key as array element ");
                    }
                    NElement u = parseObjectNoIndent(t);
                    YamlElement ee = new YamlElement(u, indentation);
                    return checkIndentation(ee, indentation);
                }
                case NAME:
                case DOUBLE_STRING:
                case BLOCK_SCALAR:
                case SINGLE_STRING:
                case NULL:
                case INTEGER:
                case DECIMAL:
                case TRUE:
                case FALSE:
                case OPEN_STRING: {
                    NElement key = valElement(t);
                    if ((asVal == AsWhat.KEY || asVal == AsWhat.ANY) && t.hasIndentation() && t.indentation < indentation) {
                        tokenizer.pushBack(t);
                        return null;
                    }
                    if (asVal == AsWhat.KEY) {
                        YamlElement ee = new YamlElement(key, indentation);
                        return checkIndentation(ee, indentation);
                    }
                    if (asVal == AsWhat.VALUE) {
                        if (!t.hasIndentation() || (t.hasIndentation() && t.indentation <= indentation)) {
                            YamlElement ee = new YamlElement(key, indentation);
                            return checkIndentation(ee, indentation);
                        }
                    }
                    YamlToken t2 = tokenizer.next();
                    if (t2 == null) {
                        YamlElement ee = new YamlElement(key, indentation);
                        return checkIndentation(ee, indentation);
                    }
                    if (t2.type == YamlToken.Type.COLON) {
                        YamlElement u = parseAny(t.hasIndentation() ? t.indentation : indentation, AsWhat.VALUE);
                        YamlElement ee = parseObjectByPairs(NElement.ofPair(key, u.element), t.indentation);
                        return checkIndentation(ee, indentation);
                    }
                    tokenizer.pushBack(t2);
                    YamlElement ee = new YamlElement(key, indentation);
                    return checkIndentation(ee, indentation);
                }
                default: {
                    throw new IllegalArgumentException("unexpected token " + t.type);
                }
            }
        }
    }

    private YamlElement checkIndentation(YamlElement u, int indentation) {
        if (u != null) {
            if (u.indentation<0 ||  u.indentation>= indentation) {
                return u;
            } else {
                unread = u;
                return null;
            }
        } else {
            return null;
        }
    }

    private NElement valElement(YamlToken t) {
        switch (t.type) {
            case NAME: {
                return NElement.ofName((String) t.value);
            }
            case DOUBLE_STRING: {
                return NElement.ofString((String) t.value, NElementType.DOUBLE_QUOTED_STRING);
            }
            case BLOCK_SCALAR: {
                return NElement.ofString((String) t.value, NElementType.DOUBLE_QUOTED_STRING);
            }
            case SINGLE_STRING: {
                return NElement.ofString((String) t.value, NElementType.SINGLE_QUOTED_STRING);
            }
            case NULL: {
                return NElement.ofNull();
            }

            case INTEGER:
            case DECIMAL: {
                return NElement.ofNumber((Number) t.value);
            }
            case TRUE:
            case FALSE: {
                return NElement.ofBoolean((Boolean) t.value);
            }
            case OPEN_STRING: {
                return NElement.ofString((String) t.value, NElementType.DOUBLE_QUOTED_STRING);
            }
        }
        throw new IllegalArgumentException("unexpected token type: " + t.type);
    }

    private NElement parseArrayNoIndent(YamlToken t) {
        List<NElement> list = new ArrayList<>();
        boolean acceptValue = true;
        boolean acceptComma = false;
        boolean acceptClose = true;
        while (true) {
            t = tokenizer.next();
            if (t == null) {
                throw new IllegalArgumentException("missing ']'");
            }
            if (t.type == YamlToken.Type.COMMA) {
                if (!acceptComma) {
                    throw new IllegalArgumentException("unexpected ','");
                }
                acceptValue = true;
                acceptComma = false;
                acceptClose = false;
            } else if (t.type == YamlToken.Type.CLOSE_BRACKET) {
                if (!acceptClose) {
                    throw new IllegalArgumentException("unexpected ']'");
                }
                break;
            } else {
                if (!acceptValue) {
                    throw new IllegalArgumentException("unexpected value");
                }
                acceptValue = false;
                acceptComma = true;
                acceptClose = true;
                tokenizer.pushBack(t);
                list.add(parseAnyNoIndentation());
            }
        }
        return NElement.ofArray(list.toArray(new NElement[0]));
    }

    private NElement parseObjectNoIndent(YamlToken t) {
        List<NElement> list = new ArrayList<>();
        boolean acceptValue = true;
        boolean acceptComma = false;
        boolean acceptClose = true;
        while (true) {
            t = tokenizer.next();
            if (t == null) {
                throw new IllegalArgumentException("missing '}'");
            }
            if (t.type == YamlToken.Type.COMMA) {
                if (!acceptComma) {
                    throw new IllegalArgumentException("unexpected ','");
                }
                acceptValue = true;
                acceptComma = false;
                acceptClose = false;
            } else if (t.type == YamlToken.Type.CLOSE_BRACE) {
                if (!acceptClose) {
                    throw new IllegalArgumentException("unexpected ']'");
                }
                break;
            } else {
                if (!acceptValue) {
                    throw new IllegalArgumentException("unexpected value");
                }
                acceptValue = false;
                acceptComma = true;
                acceptClose = true;
                tokenizer.pushBack(t);
                NElement key = parseAnyNoIndentation();
                t = tokenizer.next();
                if (t == null || t.type != YamlToken.Type.COLON) {
                    throw new IllegalArgumentException("missing ':'");
                }
                NElement value = parseAnyNoIndentation();
                list.add(NElement.ofPair(key, value));

            }
        }
        return NElement.ofObject(list.toArray(new NElement[0]));
    }

    private YamlElement parseObjectByPairs(NPairElement pair, int indentation) {
        List<NPairElement> list = new ArrayList<>();
        list.add(pair);
        while (true) {
            YamlElement a = parseAny(indentation, AsWhat.KEY);
            if (a == null) {
                break;
            }
            YamlToken t = tokenizer.next();
            if (t == null || t.type != YamlToken.Type.COLON) {
                throw new IllegalArgumentException("missing ':'");
            }
            YamlElement b = parseAny(indentation, AsWhat.VALUE);
            list.add(NElement.ofPair(a.element, b.element));
        }
        return new YamlElement(NElement.ofObject(list.toArray(new NElement[0])), indentation);
    }

    private YamlElement parseArrayByDash(YamlToken dash) {
        List<NElement> list = new ArrayList<>();
        YamlElement afterDash = parseAny(dash.indentation, AsWhat.ANY);
        if (afterDash == null) {
            throw new IllegalArgumentException("unexpected end of array");
        }
        list.add(afterDash.element);
        while (true) {
            YamlToken t = tokenizer.next();
            if (t == null) {
                break;
            } else if (t.type == YamlToken.Type.DASH && t.indentation == dash.indentation) {
                afterDash = parseAny(dash.indentation, AsWhat.ANY);
                if (afterDash == null) {
                    throw new IllegalArgumentException("unexpected end of array");
                }
                list.add(afterDash.element);
            } else {
                tokenizer.pushBack(t);
                break;
            }
        }
        return new YamlElement(NElement.ofArray(list.toArray(new NElement[0])), dash.indentation);
    }
}
