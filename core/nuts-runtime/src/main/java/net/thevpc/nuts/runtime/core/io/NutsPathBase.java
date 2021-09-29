package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class NutsPathBase implements NutsPath {
    private NutsSession session;

    public NutsPathBase(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("invalid session");
        }
        //session will be used later
        this.session = session;
    }

    @Override
    public String getBaseName() {
        String n=getName();
        int i = n.indexOf('.');
        if(i<0){
            return n;
        }
        if(i==n.length()-1){
            return n;
        }
        return n.substring(0,i);
    }

    @Override
    public String getLastExtension() {
        String n=getName();
        int i = n.lastIndexOf('.');
        if(i<0){
            return "";
        }
        return n.substring(i+1);
    }

    @Override
    public String getFullExtension() {
        String n=getName();
        int i = n.indexOf('.');
        if(i<0){
            return "";
        }
        return n.substring(i+1);
    }

    @Override
    public NutsString getFormattedName() {
        return getSession().text().forStyled(getName(),NutsTextStyle.path());
    }

    @Override
    public boolean isURL() {
        return asURL()!=null;
    }

    @Override
    public boolean isFilePath() {
        return asFilePath()!=null;
    }

    public NutsSession getSession() {
        return session;
    }

    public URL asURL() {
        try {
            return toURL();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Path asFilePath() {
        try {
            return toFilePath();
        } catch (Exception ex) {
            return null;
        }
    }


    public NutsString toNutsString() {
        return session.text().forPlain(toString());
    }

    @Override
    public NutsFormat formatter() {
        return new PathFormat(this)
                .setSession(getSession())
                ;
    }

    private static class PathFormat extends DefaultFormatBase<NutsFormat> {
        private NutsPathBase p;

        public PathFormat(NutsPathBase p) {
            super(p.session.getWorkspace(), "path");
            this.p = p;
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(p.session.text().forStyled(p.toNutsString(), NutsTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }

    @Override
    public Stream<String> lines() {
        BufferedReader br = new BufferedReader(new InputStreamReader(input().open()));
        Iterator<String> sourceIterator = new Iterator<String>() {
            String line = null;

            @Override
            public boolean hasNext() {
                boolean hasNext = false;
                try {
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    hasNext = line != null;
                    return hasNext;
                } finally {
                    if (!hasNext) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
            }

            @Override
            public String next() {
                return line;
            }
        };
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED),
                false);
    }

    @Override
    public List<String> head(int count) {
        return lines().limit(count).collect(Collectors.toList());
    }

    @Override
    public List<String> tail(int count) {
        LinkedList<String> lines = new LinkedList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(input().open()));
        String line;
        try {
            int count0 = 0;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                count0++;
                if (count0 > count) {
                    lines.remove();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return lines;
    }

}
