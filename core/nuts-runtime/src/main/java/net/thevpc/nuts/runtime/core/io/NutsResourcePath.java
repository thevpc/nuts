package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NutsResourcePath extends NutsPathBase {
    private String path;
    private List<NutsId> ids;
    private String location;
    private boolean urlPathLookedUp = false;
    private NutsPath urlPath = null;

    public NutsResourcePath(String path, NutsSession session) {
        super(session);
        this.path = path;
        String idsStr;
        if (path.startsWith("nuts-resource://(")) {
            int x = path.indexOf(')');
            if (x > 0) {
                idsStr = path.substring("nuts-resource://(".length(), x);
                location = path.substring(x + 1);
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid path %s", path));
            }
        } else if (path.startsWith("nuts-resource://")) {
            int x = path.indexOf('/', "nuts-resource://".length());
            if (x > 0) {
                idsStr = path.substring("nuts-resource://".length(), x);
                location = path.substring(x);
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid path %s", path));
            }
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid path %s", path));
        }
        NutsIdParser nutsIdParser = session.getWorkspace().id().parser().setLenient(false);
        this.ids = Arrays.stream(idsStr.split(";")).map(x -> {
            x = x.trim();
            if (x.length() > 0) {
                return nutsIdParser.parse(x);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }

    public String getName() {
        return CoreIOUtils.getURLName(path);
    }

    @Override
    public String asString() {
        return path;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public NutsPath toCompressedForm() {
        return new NutsCompressedPath(this);
    }

    @Override
    public URL toURL() {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url %s", toString()));
        }
        return up.toURL();
    }

    @Override
    public Path toFilePath() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file %s", toString()));
    }

    @Override
    public NutsInput input() {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
        }
        return up.input();
    }

    @Override
    public NutsOutput output() {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve output stream %s", toString()));
        }
        return up.output();
    }

    @Override
    public void delete(boolean recurse) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
        }
        up.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
        }
        up.mkdir(parents);
    }

    @Override
    public boolean exists() {
        NutsPath up = toURLPath();
        if (up == null) {
            return false;
        }
        return up.exists();
    }

    @Override
    public long length() {
        NutsPath up = toURLPath();
        if (up == null) {
            return -1;
        }
        return up.length();
    }

    @Override
    public Instant getLastModifiedInstant() {
        NutsPath up = toURLPath();
        if (up == null) {
            return null;
        }
        return up.getLastModifiedInstant();
    }

    public NutsPath toURLPath() {
        if (!urlPathLookedUp) {
            urlPathLookedUp = true;
            try {
                String loc = location;
                URL resource = getSession().getWorkspace().search().addIds(
                        this.ids.toArray(new NutsId[0])
                ).setLatest(true).getResultClassLoader().getResource(loc);
                if (resource != null) {
                    urlPath = getSession().getWorkspace().io().path(resource);
                }
            } catch (Exception e) {
                //ignore...
            }
        }
        return urlPath;
    }

    @Override
    public NutsFormat formatter() {
        return new MyPathFormat(this)
                .setSession(getSession())
                ;
    }

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {
        private NutsResourcePath p;

        public MyPathFormat(NutsResourcePath p) {
            super(p.getSession().getWorkspace(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            String path = p.path;
            NutsTextManager text = getSession().getWorkspace().text();
            NutsTextBuilder tb = text.builder();
            tb.append("nuts-resource://", NutsTextStyle.primary1());
            if (path.startsWith("nuts-resource://(")) {
                tb.append("(", NutsTextStyle.separator());
                int x = path.indexOf(')');
                if (x > 0) {
                    tb.append(path.substring("nuts-resource://(".length(), x));
                    tb.append(")", NutsTextStyle.separator());
                    tb.append(path.substring(x + 1), NutsTextStyle.path());
                } else {
                    return text.toText(path);
                }
            } else if (path.startsWith("nuts-resource://")) {
                int x = path.indexOf('/', "nuts-resource://".length());
                if (x > 0) {
                    tb.append(path.substring("nuts-resource://".length(), x));
                    tb.append(path.substring(x), NutsTextStyle.path());
                } else {
                    return text.toText(path);
                }
            } else {
                return text.toText(path);
            }
            return text.toText(path);
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }


}
