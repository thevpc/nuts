package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class NutsPathBase implements NutsPath {
    private final NutsSession session;
    private String userKind;

    public NutsPathBase(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("invalid session");
        }
        //session will be used later
        this.session = session;
    }

    @Override
    public String getBaseName() {
        String n = getName();
        int i = n.indexOf('.');
        if (i < 0) {
            return n;
        }
        if (i == n.length() - 1) {
            return n;
        }
        return n.substring(0, i);
    }

    @Override
    public String getLastExtension() {
        String n = getName();
        int i = n.lastIndexOf('.');
        if (i < 0) {
            return "";
        }
        return n.substring(i + 1);
    }

    @Override
    public String getFullExtension() {
        String n = getName();
        int i = n.indexOf('.');
        if (i < 0) {
            return "";
        }
        return n.substring(i + 1);
    }

    @Override
    public boolean isURL() {
        return asURL() != null;
    }

    @Override
    public boolean isFile() {
        return asFile() != null;
    }

    public URL asURL() {
        try {
            return toURL();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Path asFile() {
        try {
            return toFile();
        } catch (Exception ex) {
            return null;
        }
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPath delete() {
        return delete(false);
    }

    @Override
    public Stream<String> lines() {
        BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()));
        Iterator<String> sourceIterator = new Iterator<String>() {
            String line = null;

            @Override
            public boolean hasNext() {
                boolean hasNext = false;
                try {
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        throw new NutsIOException(session,e);
                    }
                    hasNext = line != null;
                    return hasNext;
                } finally {
                    if (!hasNext) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            throw new NutsIOException(session,e);
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
        BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()));
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
            throw new NutsIOException(session, e);
        }
        return lines;
    }

    @Override
    public String getUserKind() {
        return userKind;
    }

    @Override
    public NutsPathBase setUserKind(String userKind) {
        this.userKind = userKind;
        return this;
    }

    public NutsString toNutsString() {
        return NutsTexts.of(session).ofPlain(toString());
    }

    @Override
    public NutsFormat formatter() {
        return new PathFormat(this)
                .setSession(getSession())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(/*session, */userKind, toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPathBase that = (NutsPathBase) o;
        return  //Objects.equals(session, that.session)
                //&&
                Objects.equals(userKind, that.userKind)
                && Objects.equals(toString(), toString())
                ;
    }

    private static class PathFormat extends DefaultFormatBase<NutsFormat> {
        private final NutsPathBase p;

        public PathFormat(NutsPathBase p) {
            super(p.session, "path");
            this.p = p;
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(NutsTexts.of(p.session).ofStyled(p.toNutsString(), NutsTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }

        @Override
        public int getSupportLevel(NutsSupportLevelContext context) {
            return DEFAULT_SUPPORT;
        }
    }

    @Override
    public NutsStream<NutsPath> walk() {
        return walk(Integer.MAX_VALUE,new NutsPathOption[0]);
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPathOption... options) {
        return walk(Integer.MAX_VALUE,options);
    }

    @Override
    public NutsStream<NutsPath> walk(int maxDepth) {
        return walk(maxDepth<=0?Integer.MAX_VALUE:maxDepth,new NutsPathOption[0]);
    }

    @Override
    public Writer getWriter() {
        return new BufferedWriter(new OutputStreamWriter(getOutputStream()));
    }

    @Override
    public Reader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
