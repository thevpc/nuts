package net.thevpc.nuts.runtime.format.text.bloc;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.common.StringReaderExt;

import java.util.ArrayList;
import java.util.List;

public class StringReaderExtUtils {
    public static NutsTextNode[] readSpaces(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && ar.peekChar() <= 32) {
            sb.append(ar.nextChar());
        }
        return new NutsTextNode[]{
                factory.plain(sb.toString())
        };
    }
    public static NutsTextNode[] readSlashSlashComments(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        StringBuilder sb=new StringBuilder();
        if(!ar.peekChars("//")){
            return null;
        }
        sb.append(ar.nextChars(2));
        boolean inLoop=true;
        while(inLoop && ar.hasNext()){
            switch (ar.peekChar()){
                case '\n':
                case '\r':{
                    sb.append(ar.nextChar());
                    if(ar.hasNext() && ar.peekChar()=='\n'){
                        sb.append(ar.nextChar());
                    }
                    inLoop=false;
                    break;
                }
                default:{
                    sb.append(ar.nextChar());
                }
            }
        }
        return new NutsTextNode[]{
                factory.styled(sb.toString(),NutsTextNodeStyle.COMMENTS1)
        };
    }
    public static NutsTextNode[] readSlashStarComments(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        StringBuilder sb=new StringBuilder();
        if(!ar.peekChars("/*")){
            return null;
        }
        sb.append(ar.nextChars(2));
        boolean inLoop=true;
        while(inLoop && ar.hasNext()){
            switch (ar.peekChar()){
                case '*':{
                    if(ar.peekChars("*/")) {
                        sb.append(ar.nextChars(2));
                        inLoop = false;
                    }else{
                        sb.append(ar.nextChar());
                    }
                    break;
                }
                default:{
                    sb.append(ar.nextChar());
                }
            }
        }
        return new NutsTextNode[]{
                factory.styled(sb.toString(),NutsTextNodeStyle.COMMENTS2)
        };
    }

    public static NutsTextNode[] readJSDoubleQuotesString(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        List<NutsTextNode> all = new ArrayList<>();
        boolean inLoop = true;
        StringBuilder sb = new StringBuilder();
        if (ar.hasNext() && ar.peekChars("\"")) {
            sb.append(ar.nextChar());
            while (inLoop && ar.hasNext()) {
                switch (ar.peekChar()) {
                    case '\\': {
                        if (sb.length() > 0) {
                            all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
                            sb.setLength(0);
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(ar.nextChar());
                        if (ar.hasNext()) {
                            sb2.append(ar.nextChar());
                            if (ar.peekChar() == 'u') {
                                for (int i = 0; i < 4; i++) {
                                    char c2 = ar.peekChar();
                                    if (Character.isDigit(c2) || (Character.toUpperCase(c2) >= 'A' && Character.toUpperCase(c2) <= 'F')) {
                                        sb2.append(ar.nextChar());
                                    }
                                }
                            }
                        }
                        all.add(factory.styled(sb2.toString(), NutsTextNodeStyle.SEPARATOR1));
                        break;
                    }
                    case '\"': {
                        sb.append(ar.nextChar());
                        inLoop = false;
                        break;
                    }
                    default: {
                        sb.append(ar.nextChar());
                    }
                }
            }
            if (sb.length() > 0) {
                all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
                sb.setLength(0);
            }
            return all.toArray(new NutsTextNode[0]);
        } else {
            return null;
        }
    }


    public static NutsTextNode[] readJSSimpleQuotes(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        List<NutsTextNode> all = new ArrayList<>();
        boolean inLoop = true;
        StringBuilder sb = new StringBuilder();
        if (ar.hasNext() && ar.peekChars("\'")) {
            sb.append(ar.nextChar());
            while (inLoop && ar.hasNext()) {
                switch (ar.peekChar()) {
                    case '\\': {
                        if (sb.length() > 0) {
                            all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
                            sb.setLength(0);
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(ar.nextChar());
                        if (ar.hasNext()) {
                            sb2.append(ar.nextChar());
                            if (ar.peekChar() == 'u') {
                                for (int i = 0; i < 4; i++) {
                                    char c2 = ar.peekChar();
                                    if (Character.isDigit(c2) || (Character.toUpperCase(c2) >= 'A' && Character.toUpperCase(c2) <= 'F')) {
                                        sb2.append(ar.nextChar());
                                    }
                                }
                            }
                        }
                        all.add(factory.styled(sb2.toString(), NutsTextNodeStyle.SEPARATOR1));
                        break;
                    }
                    case '\'': {
                        sb.append(ar.nextChar());
                        inLoop = false;
                        break;
                    }
                    default: {
                        sb.append(ar.nextChar());
                    }
                }
            }
            if (sb.length() > 0) {
                all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING2));
                sb.setLength(0);
            }
            return all.toArray(new NutsTextNode[0]);
        } else {
            return null;
        }
    }

    public static NutsTextNode[] readJSIdentifier(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        List<NutsTextNode> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (!ar.hasNext() || !Character.isJavaIdentifierStart(ar.peekChar())) {
            return null;
        }
        sb.append(ar.nextChar());
        while (ar.hasNext()) {
            if (Character.isJavaIdentifierPart(ar.peekChar())) {
                sb.append(ar.nextChar());
            } else {
                break;
            }
        }
        all.add(factory.plain(sb.toString()));
        return all.toArray(new NutsTextNode[0]);

    }


    public static NutsTextNode[] readNumber(NutsWorkspace ws, StringReaderExt ar) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        boolean nbrVisited = false;
        boolean minusVisited = false;
        boolean EminusVisited = false;
        boolean dotVisited = false;
        boolean EVisited = false;
        boolean Enbr = false;
        int index = 0;
        int lastOk = -1;
        boolean inLoop = true;
        while (inLoop && ar.hasNext(index)) {
            char c = ar.peekChar(index);
            switch (c) {
                case 'E': {
                    if (EVisited || !nbrVisited) {
                        inLoop = false;
                    } else {
                        EVisited = true;
                    }
                    break;
                }
                case '.': {
                    if (dotVisited) {
                        inLoop = false;
                    } else {
                        lastOk = index;
                        dotVisited = true;
                    }
                    break;
                }
                case '-': {
                    if (EVisited) {
                        if (EminusVisited || Enbr) {
                            inLoop = false;
                        } else {
                            EminusVisited = true;
                        }
                    } else {
                        nbrVisited = true;
                    }
                    break;
                }
                default: {
                    if (Character.isDigit(c)) {
                        if (EVisited) {
                            Enbr = true;
                        } else {
                            nbrVisited = true;
                        }
                        lastOk = index;
                    } else {
                        inLoop = false;
                    }
                }
            }
            index++;
        }
        if (lastOk >= 0) {
            return new NutsTextNode[]{
                    factory.styled(ar.nextChars(lastOk+1), NutsTextNodeStyle.NUMBER1)
            };
        }
        return null;
    }
}
