package net.thevpc.nuts.runtime.standalone.io.path.spi.mem;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPathType;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.pipeline.NStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class NMemFileSPI implements NPathSPI {
    private final String path;
    private final NMemoryPathStore store;

    public NMemFileSPI(String path, NMemoryPathStore store) {
        this.path = path;
        this.store = store;
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }

    @Override
    public boolean isHidden(NPath basePath) {
        return true;
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        NMemStoreItem si = store.findStoreItem(basePath.names());
        if (si.dir) {
            return NStream.ofIterable(si.children).map(x -> NPath.of(new NMemFileSPI(x.path, store)));
        }
        return NStream.ofEmpty();
    }

    @Override
    public boolean exists(NPath basePath) {
        return store.findStoreItem(basePath.names()) != null;
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        NMemStoreItem a = store.findStoreItem(basePath.names());
        if (a != null) {
            if (!a.children.isEmpty() && !recurse) {
                throw new NIOException(NMsg.ofC("cannot delete non empty folder"));
            }
            if (a.parent != null) {
                a.parent.children.remove(a);
            }
            a.children.clear();
        }
    }

    @Override
    public NPathType getType(NPath basePath) {
        NMemStoreItem a = store.findStoreItem(basePath.names());
        if (a != null) {
            if (a.dir) {
                return NPathType.DIRECTORY;
            }
            return NPathType.FILE;
        }
        return NPathType.NOT_FOUND;
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return true;
    }


    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        NMemStoreItem a = store.findStoreItem(basePath.names());
        if (a != null) {
            long li = a.lastModified();
            if (li != 0) {
                return Instant.ofEpochMilli(li);
            }
        }
        return null;
    }

    @Override
    public long getContentLength(NPath basePath) {
        NMemStoreItem a = store.findStoreItem(basePath.names());
        if (a != null) {
            if (a.dir) {
                return -1;
            }
            return a.size();
        }
        return -1;
    }

    @Override
    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        NMemStoreItem a = store.findStoreItem(basePath.names());
        if (a != null) {
            if (!a.dir) {
                return new InputStream() {
                    int pos;

                    @Override
                    public int read() {
                        int r = a.read(pos);
                        if (r >= 0) {
                            pos++;
                        }
                        return r;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) {
                        int r = a.read(pos, b, off, len);
                        pos += r;
                        return r;
                    }

                    public int available() {
                        return Math.max((int) (a.size() - pos), 0);
                    }
                };
            }
        }
        throw new NIOException(NMsg.ofC("folder has no input stream"));
    }

    @Override
    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        List<String> names = basePath.names();
        NMemStoreItem a = store.findStoreItem(names);
        boolean exists=a!=null;
        if (a == null) {
            List<String> names2 = new ArrayList<>(names);
            NMemStoreItem ii = store.root;
            if (names2.size() > 1) {
                names2.remove(names2.size() - 1);
                ii = store.findStoreItem(names2);
                if (ii == null) {
                    throw new NIOException(NMsg.ofC("folder not found : mem://%s",
                            names2.stream().collect(Collectors.joining("/"))));
                }
            }
            String n = names.get(names.size() - 1); // note: use `names`, not the mutated `names2`
            a = new NMemStoreItem(false, n, ii, store);
        }
        if (a.dir) {
            throw new NIOException(NMsg.ofC("folder has no output stream"));
        }

        boolean append = false;
        boolean createNew = false;
        for (NPathOption option : options) {
            if (option == NPathOption.APPEND) append = true;
            if (option == NPathOption.CREATE_NEW) createNew = true;
        }
        if (createNew && exists) { // adjust to whatever "does this item already exist in store" check you use
            throw new NIOException(NMsg.ofC("file already exists : mem://%s",
                    names.stream().collect(Collectors.joining("/"))));
        }

        NMemStoreItem finalA = a;
        if (!append) {
            finalA.reset();
        }

        boolean finalAppend = append;
        return new OutputStream() {
            long pos = finalAppend ? finalA.size() : 0;

            @Override
            public void write(int b) {
                finalA.write(pos, (byte) b);
                pos++;
            }

            @Override
            public void write(byte[] b, int off, int len) {
                finalA.write(pos, b, off, len);
                pos += len;
            }
        };
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        List<String> names = basePath.names();
        NMemStoreItem a = store.findStoreItem(names);
        if (a == null) {
            store.storeItemMkdirs(names);
        } else {
            if (a.dir) {
                return;
            }
            throw new NIOException(NMsg.ofC("file already exists : %s", a.path));
        }
    }

    @Override
    public NPath getRoot(NPath basePath) {
        return NPath.of("mem://");
    }
}
