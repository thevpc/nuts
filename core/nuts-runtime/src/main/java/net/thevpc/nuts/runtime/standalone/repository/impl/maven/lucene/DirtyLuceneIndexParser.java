package net.thevpc.nuts.runtime.standalone.repository.impl.maven.lucene;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.util.NEvictingIntQueue;
import net.thevpc.nuts.util.NIteratorBase;

import java.io.*;

public class DirtyLuceneIndexParser extends NIteratorBase<String> implements Closeable {
    private PushbackReader reader;
    private String last;
    private NEvictingIntQueue whites = new NEvictingIntQueue(10);
    private long count = 0;
    private boolean closed=false;
    private InputStream source0;

    public DirtyLuceneIndexParser(InputStream reader) {
        this.source0 = reader;
        this.reader = new PushbackReader(new InputStreamReader(reader));
    }

    @Override
    public NElement describe() {
        return NElements.of().ofObjectBuilder()
                .name("ScanLucene")
                .set("source",source0.toString())
                .build();
    }

    public static boolean isVisibleChar(char c) {
        return
                (c >= 'a' && c <= 'z')
                        || (c >= 'A' && c <= 'Z')
                        || (c >= '0' && c <= '9')
                        || c == '.'
                        || c == '_'
                        || c == '-'
                        || c == '|';

    }

    @Override
    public boolean hasNext() {
        last = preload();
        return last != null;
    }

    @Override
    public String next() {
        return last;
    }

    private String preload() {
        try {
            while (true) {
                if(reader ==null || closed){
                    return null;
                }
                int c = reader.read();
                if (c < 0) {
                    break;
                }
                if (isVisibleChar((char) c)) {
                    StringBuilder sb = new StringBuilder();
                    boolean withPipe = c == '|';
                    sb.append((char) c);
                    while (true) {
                        c = reader.read();
                        if (c < 0) {
                            break;
                        }
                        if (!isVisibleChar((char) c)) {
                            reader.unread(c);
                            break;
                        } else {
                            withPipe |= (c == '|');
                            sb.append((char) c);
                        }
                    }
                    if (withPipe) {
                        String s = sb.toString();
                        boolean ignore = false;
                        if (whites.size() == 3 && whites.get(0)==0 && whites.get(1)==0 && whites.get(2)==0) {
                            s = s.substring(1);
                        }
                        if (!ignore) {
                            if (s.startsWith("|")) {
                                s = s.substring(1);
                            }
                            if (s.contains("|sources|") || s.contains("|javadoc|")) {
                                ignore = true;
                            } else if (s.endsWith("|tests|jar")) {
                                ignore = true;
                            }
                        }
                        if (!ignore) {
                            String[] split = s.split("[|]");
                            if (split.length < 3) {
                                ignore = true;
                            } else {
                                if (split[0].indexOf('.') < 0) {
                                    ignore = true;
                                }
                            }
                            if (!ignore) {
                                count++;
                                return split[0] + ":" + split[1] + "#" + split[2];
                            }
                        }
                    }
                } else {
                    whites.clear();
                    whites.add(c);
                    while (true) {
                        c = reader.read();
                        if (c < 0) {
                            break;
                        }
                        if (isVisibleChar((char) c)) {
                            reader.unread(c);
                            break;
                        } else {
                            whites.add(c);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        close();
        return null;
    }

    @Override
    public void close() {
        if(!closed){
            closed=true;
        }
        try {
            reader.close();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }
}
