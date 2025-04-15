package net.thevpc.nuts.runtime.standalone.xtra.rnsh;

import net.thevpc.nuts.NCallableSupport;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RnshPathFactorySPI implements NPathFactorySPI {
    @Override
    public NCallableSupport<NPathSPI> createPath(String path, ClassLoader classLoader) {
        if (
                path.startsWith("rnsh-http:")
                        || path.startsWith("rnsh-https:")
        ) {
            NConnexionString cnx = NConnexionString.of(path).orNull();
            if (cnx != null) {
                return NCallableSupport.of(3, () -> new NServerPathSPI(cnx));
            }
        }
        return NCallableSupport.invalid(NMsg.ofC("Invalid path: %s", path));
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String path = context.getConstraints();
        if (
                path.startsWith("rnsh-http:")
                        || path.startsWith("rnsh-https:")
        ) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        return NConstants.Support.NO_SUPPORT;
    }

    public static class NServerPathSPI implements NPathSPI {
        final NConnexionString cnx;
        final RnshHttpClient client;
        final String remotePath;

        private NServerPathSPI(NConnexionString cnx, String remotePath, RnshHttpClient client) {
            this.cnx = cnx;
            this.client = client;
            this.remotePath = remotePath;
        }

        public NServerPathSPI(NConnexionString cnx) {
            this.cnx = cnx;
            this.client = new RnshHttpClient();
            this.client.setConnexionString(cnx);
            this.remotePath = NStringUtils.firstNonBlank(cnx.getPath(), "/");
        }

        @Override
        public byte[] getDigest(NPath basePath, String algo) {
            client.ensureConnected();
            String hash = client.digest(remotePath, algo);
            return NHex.toBytes(hash);
        }

        @Override
        public List<NPathChildDigestInfo> listDigestInfo(NPath basePath, String algo) {
            client.ensureConnected();
            List<NPathChildStringDigestInfo> nPathChildStringDigestInfos = client.directoryListDigest(remotePath, algo);
            if (nPathChildStringDigestInfos == null) {
                return new ArrayList<>();
            }
            return nPathChildStringDigestInfos
                    .stream().map(x ->
                            new NPathChildDigestInfo()
                                    .setName(x.getName())
                                    .setDigest(NHex.toBytes(x.getDigest()))
                    )
                    .collect(Collectors.toList());
        }

        @Override
        public NStream<NPath> list(NPath basePath) {
            if(!client.ensureConnectedSafely()){
                return NStream.ofEmpty();
            }
            return
                    NStream.ofArray(client.listNames(remotePath))
                            .map(x ->
                                    NPath.of(cnx.resolve(x).toString())
                            )
                    ;
        }

        @Override
        public NPathType type(NPath basePath) {
            if(!client.ensureConnectedSafely()){
                return NPathType.NOT_FOUND;
            }
            try {
                client.ensureConnected();
                RnshHttpClient.NFileInfo i = client.getFileInfo(remotePath);
                if (i != null) {
                    return i.getPathType();
                }
            } catch (Exception e) {
                //
            }
            return NPathType.NOT_FOUND;
        }

        @Override
        public boolean exists(NPath basePath) {
            if(!client.ensureConnectedSafely()){
                return false;
            }
            return type(basePath) != NPathType.NOT_FOUND;
        }

        @Override
        public long contentLength(NPath basePath) {
            if(!client.ensureConnectedSafely()){
                return -1;
            }
            try {
                RnshHttpClient.NFileInfo i = client.getFileInfo(remotePath);
                return i.getContentLength();
            } catch (Exception e) {
                return -1;
            }
        }

        @Override
        public InputStream getInputStream(NPath basePath, NPathOption... options) {
            return client.getFile(remotePath).getInputStream();
        }

        @Override
        public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
            String name = NPath.of(remotePath).getName();
            PipedInputStream in = new PipedInputStream(1024 * 1024);
            PipedOutputStream out = null;
            try {
                out = new PipedOutputStream(in);
            } catch (IOException e) {
                throw new NIOException(e);
            }
            client.putFile(new NInputContentProvider() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getContentType() {
                    return "application/octet-stream";
                }

                @Override
                public String getCharset() {
                    return null;
                }

                @Override
                public InputStream getInputStream() {
                    return in;
                }
            }, remotePath);
            return out;
        }

        @Override
        public void delete(NPath basePath, boolean recurse) {
            client.exec("rm", "-R", remotePath);
        }

        @Override
        public void mkdir(boolean parents, NPath basePath) {
            client.exec("mkdir", parents ? "-p" : null, remotePath);
        }

        @Override
        public List<String> getNames(NPath basePath) {
            return cnx.getNames();
        }

        @Override
        public NPath getRoot(NPath basePath) {
            if (isRoot(basePath)) {
                return basePath;
            }
            return NPath.of(cnx.getRoot().toString());
        }

        @Override
        public Boolean isRoot(NPath basePath) {
            return "/".equals(String.valueOf(cnx.getPath()));
        }

        @Override
        public NPath getParent(NPath basePath) {
            if (isRoot(basePath)) {
                return null;
            }
            return NPath.of(cnx.getParent().toString());
        }
    }
}
