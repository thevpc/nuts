package net.thevpc.nuts.toolbox.ncode.filters;

import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparator;
import net.thevpc.nuts.toolbox.ncode.sources.JavaTypeSource;
import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.SourceFilter;
import net.thevpc.nuts.toolbox.ncode.sources.ZipEntrySource;
import net.thevpc.nuts.toolbox.ncode.sources.ZipSource;

import java.util.List;
import java.util.zip.ZipEntry;

public class JavaSourceFilter implements SourceFilter {

    private final List<StringComparator> types;
    private final List<StringComparator> files;

    public JavaSourceFilter(List<StringComparator>  types, List<StringComparator>  files) {
        this.types = types;
        this.files = files;
    }

    private boolean matchesFile(String s){
        if(files.isEmpty()){
            return true;
        }
        for (StringComparator file : files) {
            if(file.matches(s)){
                return true;
            }
        }
        return false;
    }
    private boolean matchesType(String s){
        if(files.isEmpty()){
            return true;
        }
        for (StringComparator t : types) {
            if(t.matches(s)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(Source source) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            if(matchesFile(source.getExternalPath())){
                String n = s.getClassName();
                if (matchesType(n)) {
                    return true;
                }
            }
        }else if (source instanceof ZipSource) {
            ZipSource s = (ZipSource) source;
            if(matchesFile(source.getExternalPath())){
                String n = s.getName();
                if(n.endsWith(".class")) {
                    n=n.substring(0,n.length()-".class".length());
                    String[] q = n.split("[$]");
                    if (matchesType(q[0])) {
                        return true;
                    }
                }
            }
        }else if (source instanceof ZipEntrySource) {
            ZipEntrySource s = (ZipEntrySource) source;
            if(matchesFile(source.getExternalPath())){
                String n = s.getName();
                if(n.endsWith(".class")) {
                    n=n.substring(0,n.length()-".class".length());
                    String[] q = n.split("[$]");
                    if (matchesType(q[0])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean lookInto(Source source) {
        return true;
    }
}
