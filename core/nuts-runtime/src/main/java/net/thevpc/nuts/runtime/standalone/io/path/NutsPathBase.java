package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class NutsPathBase implements NutsPath {
    private final NutsSession session;
    private DefaultNutsPathMetadata omd = new DefaultNutsPathMetadata(this);

    public NutsPathBase(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("invalid session");
        }
        //session will be used later
        this.session = session;
    }

    protected NutsPath copyExtraFrom(NutsPath other) {
        if (other instanceof NutsPathBase) {
            omd.setAll(omd);
        } else {
            omd.setAll(other.getInputMetaData());
            omd.setAll(other.getOutputMetaData());
        }
        return this;
    }

    @Override
    public boolean isUserCache() {
        return omd.isUserCache();
    }

    @Override
    public NutsPath setUserCache(boolean userCache) {
        this.omd.setUserCache(userCache);
        return this;
    }

    @Override
    public boolean isUserTemporary() {
        return omd.isUserTemporary();
    }

    @Override
    public NutsPath setUserTemporary(boolean temporary) {
        this.omd.setUserTemporary(temporary);
        return this;
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
    public String getLongBaseName() {
        String n = getName();
        int i = n.lastIndexOf('.');
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
                        throw new NutsIOException(session, e);
                    }
                    hasNext = line != null;
                    return hasNext;
                } finally {
                    if (!hasNext) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            throw new NutsIOException(session, e);
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

    public NutsString toNutsString() {
        return NutsTexts.of(session).ofPlain(toString());
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return new PathFormat(this)
                .setSession(getSession())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPathBase that = (NutsPathBase) o;
        return  Objects.equals(toString(), toString())
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
        return walk(Integer.MAX_VALUE, new NutsPathOption[0]);
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPathOption... options) {
        return walk(Integer.MAX_VALUE, options);
    }

    @Override
    public NutsStream<NutsPath> walk(int maxDepth) {
        return walk(maxDepth <= 0 ? Integer.MAX_VALUE : maxDepth, new NutsPathOption[0]);
    }

    @Override
    public Writer getWriter() {
        return new BufferedWriter(new OutputStreamWriter(getOutputStream()));
    }

    @Override
    public Reader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public boolean isHttp() {
        if (!isURL()) {
            return false;
        }
        String s = toString();
        return s.startsWith("http://") || s.startsWith("https://");
    }


    @Override
    public NutsInputSourceMetadata getInputMetaData() {
        return omd.asInput();
    }

    @Override
    public NutsOutputTargetMetadata getOutputMetaData() {
        return omd.asOutput();
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }

    @Override
    public void disposeMultiRead() {
        //do nothing
    }

    @Override
    public NutsMessage formatMessage(NutsSession session) {
        if(session!=null) {
            return NutsMessage.ofNtf(format(session));
        }
        return NutsMessage.ofPlain(toString());
    }

    //    public NutsRm fixMe_delete() {
//        checkSession();
//        Path t = _toPath(getTarget());
//        if (t == null) {
//            NutsUtils.requireNonBlank(getTarget(),getSession(),"target");
//        }
//        if (!Files.exists(t)) {
//            grabException(new FileNotFoundException(t.toString()));
//            return this;
//        }
//        if (Files.isRegularFile(t)) {
//            try {
//                Files.delete(t);
//            } catch (IOException e) {
//                grabException(e);
//                return this;
//            }
//            return this;
//        }
//        final int[] deleted = new int[]{0, 0, 0};
//        NutsLogger LOG = NutsLogger.of(CoreIOUtils.class,getSession());
//        try {
//            Files.walkFileTree(t, new FileVisitor<Path>() {
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    try {
//                        Files.delete(file);
//                        if (LOG != null) {
//                            LOG.with().session(getSession()).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
//                                    .log( NutsMessage.jstyle("delete file {0}", file));
//                        }
//                        deleted[0]++;
//                    } catch (IOException e) {
//                        if (LOG != null) {
//                            LOG.with().session(getSession()).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
//                                    .log( NutsMessage.jstyle("failed deleting file : {0}", file));
//                        }
//                        deleted[2]++;
//                        grabException(e);
//                    }
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                    try {
//                        Files.delete(dir);
//                        if (LOG != null) {
//                            LOG.with().session(getSession()).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
//                                    .log( NutsMessage.jstyle("delete folder {0}", dir));
//                        }
//                        deleted[1]++;
//                    } catch (IOException e) {
//                        if (LOG != null) {
//                            LOG.with().session(getSession()).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
//                                    .log( NutsMessage.jstyle("failed deleting folder : {0}", dir));
//                        }
//                        deleted[2]++;
//                        grabException(e);
//                    }
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            if (error != null) {
//                grabException(e);
//            }
//        }
//        return this;
//    }
//    private void grabException(IOException e) {
//        this.error = e;
//        if (isFailFast()) {
//            throw new NutsIOException(getSession(),e);
//        }
//    }
}
