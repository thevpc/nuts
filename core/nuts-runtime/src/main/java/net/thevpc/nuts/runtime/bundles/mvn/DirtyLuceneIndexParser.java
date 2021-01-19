package net.thevpc.nuts.runtime.bundles.mvn;

import net.thevpc.nuts.runtime.bundles.datastr.EvictingQueue;

import java.io.*;
import java.util.Iterator;

public class DirtyLuceneIndexParser implements Iterator<String>, Closeable {
    private PushbackReader reader;
    private String last;
    private EvictingQueue<Integer> whites = new EvictingQueue<>(10);
    private long count = 0;
    private boolean closed=false;

    public DirtyLuceneIndexParser(InputStream reader) {
        this.reader = new PushbackReader(new InputStreamReader(reader));
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
//                    System.out.println("Accept "+(char)c);
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
                        if (whites.size() == 3 && whites.get(0).equals(0) && whites.get(1).equals(0) && whites.get(2).equals(0)) {
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
//                    System.out.println("Ignore "+(int)c);
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
//                            System.out.println("\t### ignore "+((int) c));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
            throw new UncheckedIOException(ex);
        }
    }
}
