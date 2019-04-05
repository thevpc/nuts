/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author vpc
 */
public class NutsIOHelper {

    public static SourceItem createSource(InputStream source) {
        if (source == null) {
            return null;
        }
        return new SourceItem(source, false, false) {

            @Override
            public InputStream getStream() {
                return (InputStream) getValue();
            }

        };
    }

    public static SourceItem createSource(Path source) {
        if (source == null) {
            return null;
        }
        return new SourceItem(source, true, true) {
            @Override
            public Path getPath() {
                return (Path) getValue();
            }

            @Override
            public URL getURL() throws MalformedURLException {
                return getPath().toUri().toURL();
            }

            @Override
            public InputStream getStream() {
                try {
                    return Files.newInputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static SourceItem createSource(File source) {
        if (source == null) {
            return null;
        }
        return new SourceItem(source, true, true) {
            @Override
            public Path getPath() {
                return ((File) getValue()).toPath();
            }

            @Override
            public URL getURL() throws MalformedURLException {
                return getPath().toUri().toURL();
            }

            @Override
            public InputStream getStream() {
                try {
                    return Files.newInputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static SourceItem createSource(URL source) {
        if (source == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(((URL) source).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            Path finalPath = basePath;
            return new SourceItem(source, true, true) {
                @Override
                public Path getPath() {
                    return finalPath;
                }

                @Override
                public URL getURL() throws MalformedURLException {
                    return (URL) getValue();
                }

                @Override
                public InputStream getStream() {
                    try {
                        return Files.newInputStream(finalPath);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            };
        } else {
            return new SourceItem(source, false, true) {
                @Override
                public URL getURL() throws MalformedURLException {
                    return (URL) getValue();
                }

                @Override
                public InputStream getStream() {
                    try {
                        return ((URL) getValue()).openStream();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            };
        }
    }

    public static SourceItem createSource(String source) {
        if (source == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(source);
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createSource(basePath);
        }

        try {
            basePath = Paths.get(new URL(source).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createSource(basePath);
        }

        URL baseURL = null;
        try {
            baseURL = new URL(source);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            return createSource(baseURL);
        }

        throw new IllegalArgumentException("Unsuported source : " + source);
    }

    public static SourceItem createSource(Object source) {
        if (source == null) {
            return null;
        } else if (source instanceof InputStream) {
            return createSource((InputStream) source);
        } else if (source instanceof Path) {
            return createSource((Path) source);
        } else if (source instanceof File) {
            return createSource((File) source);
        } else if (source instanceof URL) {
            return createSource((URL) source);
        } else if (source instanceof String) {
            return createSource((String) source);
        } else {
            throw new IllegalArgumentException("Unsupported type " + source.getClass().getName());
        }
    }

    public static TargetItem createTarget(OutputStream target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, false) {
            @Override
            public OutputStream getStream() {
                return (OutputStream) getValue();
            }
        };
    }

    public static TargetItem createTarget(String target) {
        if (target == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(target);
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createTarget(target);
        }

        try {
            basePath = Paths.get(new URL(target).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createTarget(target);
        }

        URL baseURL = null;
        try {
            baseURL = new URL(target);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            return createTarget(baseURL);
        }
        throw new IllegalArgumentException("Unsuported source : " + target);
    }

    public static TargetItem createTarget(Path target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, true) {
            @Override
            public Path getPath() {
                return (Path) getValue();
            }

            @Override
            public OutputStream getStream() {
                try {
                    return Files.newOutputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static TargetItem createTarget(File target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, true) {
            @Override
            public Path getPath() {
                return ((File) getValue()).toPath();
            }

            @Override
            public OutputStream getStream() {
                try {
                    return Files.newOutputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static TargetItem createTarget(Object target) {
        if (target == null) {
            return null;
        } else if (target instanceof OutputStream) {
            return createTarget((OutputStream) target);
        } else if (target instanceof Path) {
            return createTarget((Path) target);
        } else if (target instanceof File) {
            return createTarget((File) target);
        } else if (target instanceof String) {
            return createTarget((String) target);
        } else {
            throw new IllegalArgumentException("Unsupported type " + target.getClass().getName());
        }
    }

    public static abstract class SourceItem {

        private Object value;
        private boolean path;
        private boolean url;

        public SourceItem(Object value, boolean path, boolean url) {
            this.value = value;
            this.path = path;
            this.url = url;
        }

        public boolean isPath() {
            return path;
        }

        public boolean isURL() {
            return url;
        }

        public Path getPath() {
            throw new UnsupportedOperationException("Not supported");
        }

        public URL getURL() throws MalformedURLException {
            throw new UnsupportedOperationException("Not supported");
        }

        public Object getValue() {
            return value;
        }

        public abstract InputStream getStream();
    }

    public static abstract class TargetItem {

        private Object value;
        private boolean path;

        public TargetItem(Object value, boolean path) {
            this.value = value;
            this.path = path;
        }

        public boolean isPath() {
            return path;
        }

        public Object getValue() {
            return value;
        }

        public Path getPath() {
            throw new UnsupportedOperationException("Not supported");
        }

        public abstract OutputStream getStream();
    }
}
