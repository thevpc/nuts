package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NTextFormatType;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NMsgTemplate {
    private String message;
    private NTextFormatType format;

    public static NMsgTemplate ofC(String message) {
        return of(message, NTextFormatType.CFORMAT);
    }

    public static NMsgTemplate ofJ(String message) {
        return of(message, NTextFormatType.JFORMAT);
    }

    public static NMsgTemplate ofV(String message) {
        return of(message, NTextFormatType.VFORMAT);
    }

    public static NMsgTemplate of(String message, NTextFormatType format) {
        return new NMsgTemplate(message, format);
    }

    public NMsgTemplate(String message, NTextFormatType format) {
        if (message == null) {
            throw new IllegalArgumentException("missing message");
        }
        if (format == null) {
            throw new IllegalArgumentException("missing format");
        }
        switch (format) {
            case CFORMAT:
            case JFORMAT:
            case VFORMAT: {
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid format. only Cformat, JFormat and VFormat are allowed");
            }
        }
        this.message = message;
        this.format = format;
    }

    public String getMessage() {
        return message;
    }

    public String[] getParamNames() {
        try {
            Set<String> paramSet = new HashSet<>();
            List<String> params = new ArrayList<>();
            switch (format) {
                case CFORMAT: {
                    StringReader r = new StringReader(message);
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '%') {
                            i = r.read();
                            if (i >= 0) {
                                char c2 = (char) i;
                                if (c2 == 'n') {
                                    //ignore
                                } else {
                                    params.add("");
                                }
                            }
                        }
                    }
                    break;
                }
                case JFORMAT: {
                    StringReader r = new StringReader(message);
                    int maxElems = 0;
                    int currentIndex = -1;
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '{') {
                            StringBuilder n = new StringBuilder();
                            while (true) {
                                i = r.read();
                                if (i < 0) {
                                    break;
                                }
                                c = (char) i;
                                if (c == '\\') {
                                    i=r.read();
                                    if(i<0){
                                        break;
                                    }else{
                                        n.append(c=(char) i);
                                    }
                                } else if (c == '}') {
                                    break;
                                }else{
                                    n.append(c);
                                }
                            }
                            currentIndex++;
                            String ns = n.toString();
                            int sep = ns.indexOf(':');
                            String nsIntString = "";
                            Integer nsInt = null;
                            if (sep < 0) {
                                nsIntString = ns.trim();
                            } else {
                                nsIntString = ns.substring(0, sep).trim();
                            }
                            if(nsIntString.isEmpty()){
                                nsIntString=String.valueOf(currentIndex);
                            }
                            NLiteral lit = NLiteral.of(nsIntString);
                            if (lit.isInt()) {
                                nsInt = lit.asInt().get();
                            }
                            if (nsInt != null) {
                                while (maxElems <= nsInt) {
                                    if (paramSet.add(String.valueOf(maxElems))) {
                                        params.add(String.valueOf(maxElems));
                                    }
                                    maxElems++;
                                }
                            } else {
                                if (paramSet.add(nsIntString)) {
                                    params.add(nsIntString);
                                }
                            }
                        } else if (c == '\\') {
                            r.read();
                        } else {
                            //ignore
                        }
                    }
                    break;
                }
                case VFORMAT: {
                    StringReader r = new StringReader(message);
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '$') {
                            i = r.read();
                            if (i < 0) {
                                if (paramSet.add("")) {
                                    params.add("");
                                }
                            } else {
                                c = (char) i;
                                if (c == '{') {
                                    StringBuilder n = new StringBuilder();
                                    while (true) {
                                        i = r.read();
                                        if (i < 0) {
                                            break;
                                        }
                                        c = (char) i;
                                        if (c != '}') {
                                            n.append(c);
                                        } else {
                                            break;
                                        }
                                    }
                                    String ns = n.toString();
                                    if (paramSet.add(ns)) {
                                        params.add(ns);
                                    }
                                } else {
                                    StringBuilder n = new StringBuilder();
                                    while (true) {
                                        r.mark(1);
                                        i = r.read();
                                        if (i < 0) {
                                            break;
                                        }
                                        c = (char) i;
                                        if (NMsg._isValidMessageVar(c)) {
                                            n.append(c);
                                        } else {
                                            r.reset();
                                            break;
                                        }
                                    }
                                    String ns = n.toString();
                                    if (paramSet.add(ns)) {
                                        params.add(ns);
                                    }
                                }
                            }
                        } else if (c == '\\') {
                            r.read();
                        } else {
                            //ignore
                        }
                    }
                    break;
                }
                default: {
                    throw new IllegalArgumentException("invalid format. only Cformat, JFormat and VFormat are allowed");
                }
            }
            return params.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NTextFormatType getFormat() {
        return format;
    }

    public NMsg build(NMsgParam... params) {
        switch (format) {
            case CFORMAT: {
                return NMsg.ofC(message, params);
            }
            case VFORMAT: {
                return NMsg.ofV(message, params);
            }
            case JFORMAT: {
                return NMsg.ofJ(message, params);
            }
            default: {
                throw new IllegalArgumentException("invalid format. only Cformat, JFormat and VFormat are allowed");
            }
        }
    }
}
