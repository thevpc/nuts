package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.app.NutsCommandLineUtils;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;
import net.thevpc.nuts.NutsTextNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShellSpecialTextFormatter implements SpecialTextFormatter {
    private NutsWorkspace ws;

    public ShellSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        List<NutsTextNode> all=new ArrayList<>();
        BufferedReader reader=new BufferedReader(new StringReader(text));
        String line=null;
        boolean first=true;
        NutsTextNodeFactory factory = ws.formats().text().factory();
        while (true){
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if(first){
                first=false;
            }else{
                all.add(factory.plain("\n"));
            }
            all.add(commandToNode(line));
        }
        return factory.list(all);
    }


    public NutsTextNode commandToNode(String text) {
        String[] u = NutsCommandLineUtils.parseCommandLine(null, text);
        List<NutsTextNode> all=new ArrayList<>();
        boolean cmdName=true;
        NutsTextNodeFactory factory = ws.formats().text().factory();
        for (int i = 0; i < u.length; i++) {
            if(!all.isEmpty()){
                all.add(factory.plain(" "));
            }
            if(cmdName){
                if(u[i].startsWith("-") || u[i].startsWith("+")){
                    cmdName=false;
                    all.addAll(Arrays.asList(argToNodes(u[i])));
                }else if(i==0) {
                    all.add(factory.styled(u[i], NutsTextNodeStyle.KEYWORD1));
                }else{
                    cmdName=false;
                    all.addAll(Arrays.asList(argToNodes(u[i])));
                }
            }
        }
        return factory.list(all);
    }

    private NutsTextNode[] argToNodes(String s){
        boolean option=false;
        if(s.startsWith("-")||s.startsWith("+")){
            option=true;
        }
        int x=s.indexOf('=');
        int eq=-1;
        if(x>=0 && isCommonName(s.substring(0,x))){
            eq=x;
        }
        NutsTextNodeFactory factory = ws.formats().text().factory();

        if(eq!=-1) {
            if (option) {
                return new NutsTextNode[]{
                        factory.styled(s.substring(0, x),NutsTextNodeStyle.OPTION1),
                        factory.styled(String.valueOf(s.charAt(eq)),NutsTextNodeStyle.SEPARATOR1),
                        factory.plain(s.substring(x+1)),
                };
            }
            return new NutsTextNode[]{
                    factory.plain(s.substring(0, x)),
                    factory.styled(String.valueOf(s.charAt(eq)),NutsTextNodeStyle.SEPARATOR1),
                    factory.plain(s.substring(x+1)),
            };
        }else{
            if (option) {
                return new NutsTextNode[]{
                        factory.styled(s,NutsTextNodeStyle.OPTION1),
                };
            }
            return new NutsTextNode[]{
                    factory.plain(s),
            };
        }
    }

    private boolean isSubCommand(String substring) {
        if(!Character.isAlphabetic(substring.charAt(0))){
            return false;
        }
        for (char c : substring.toCharArray()) {
            if(!(Character.isAlphabetic(c) || c=='-' || c=='+' ||  c=='_')){
                return false;
            }
        }
        return true;
    }
    private boolean isCommonName(String substring) {
        for (char c : substring.toCharArray()) {
            if(!(Character.isAlphabetic(c) || Character.isDigit(c) || c=='-' || c=='+' ||  c=='_')){
                return false;
            }
        }
        return true;
    }

}
