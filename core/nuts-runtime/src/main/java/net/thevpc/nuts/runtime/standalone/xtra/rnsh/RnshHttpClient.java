package net.thevpc.nuts.runtime.standalone.xtra.rnsh;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.web.NWebCli;
import net.thevpc.nuts.web.NWebRequest;
import net.thevpc.nuts.web.NWebResponse;

import java.util.*;
import java.util.stream.Collectors;

public class RnshHttpClient {
    public static final String CONTEXT_PATH_PARAM = "context";
    private NConnexionString connexionString;
    private LoginResult loginResult;
    private int accessTokenSafePeriod = 5000;
    private int refreshTokenSafePeriod = 30000;

    public NConnexionString getConnexionString() {
        return connexionString;
    }

    public RnshHttpClient setConnexionString(NConnexionString connexionString) {
        this.connexionString = connexionString;
        return this;
    }

    public String version() {
        return NWebCli.of().POST(resolveUrl("version"))
                .doWith(this::prepareSecurity)
                .run().getContentAsString();
    }

    public NInputSource getFile(String remotePath) {
        return NWebCli.of().POST(resolveUrl("get-file"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("path", remotePath)
                )
                .doWith(this::prepareSecurity)
                .run().getContent();
    }

    public void getFile(String remotePath, String localPath) {
        NFileInfo u = getFileInfo(remotePath);
        if (u != null) {
            switch (NUtils.firstNonNull(u.getPathType(), NPathType.NOT_FOUND)) {
                case DIRECTORY: {
                    NPath.of(localPath).mkdirs();
                    for (String s : listNames(u.getPath())) {
                        getFile(NStringUtils.pjoin("/", u.getPath(), s), NStringUtils.pjoin("/", localPath, s));
                    }
                    break;
                }
                case FILE: {
                    NPath.of(localPath).mkParentDirs().copyFromInputStreamProvider(getFile(remotePath));
                    break;
                }
            }
        }
    }

    public void putFile(String localPath, String remotePath) {
        NPath fromPathObj = NPath.of(localPath);
        NPath toPathObj = NPath.of(remotePath);
        if (!fromPathObj.isDirectory()) {
            List<NPath> directories = new ArrayList<>();
            fromPathObj.walk().filter(x -> x.isDirectory()).forEach(x -> {
                boolean found = false;
                for (NPath directory : directories) {
                    if (x.isEqOrDeepChildOf(directory)) {
                        //ignore
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    directories.add(x);
                }
            });
            for (NPath directory : directories) {
                NPath v = translate(directory, fromPathObj, toPathObj);
                v.mkdirs();
            }
            fromPathObj.walk().filter(x -> x.isRegularFile()).forEach(x -> {
                NPath toStr = translate(x, fromPathObj, toPathObj);

                NWebCli.of().POST(resolveUrl("put-file"))
                        .addFormData("content", x.toString())
                        .addFormData("path", toStr.toString())
                        .doWith(this::prepareSecurity)
                        .run().getContent();
            });
        } else if (fromPathObj.isRegularFile()) {
            NWebCli.of().POST(resolveUrl("put-file"))
                    .addFormData("content", fromPathObj.toString())
                    .addFormData("path", toPathObj.toString())
                    .doWith(this::prepareSecurity)
                    .run().getContent();
        }
    }

    private NPath translate(NPath fromBase, NPath toBase, NPath fromPath) {
        String u = fromPath.toRelative(fromBase).get();
        NPath v = toBase.resolve(u);
        return v;
    }

    public void putFile(NInputContentProvider localPath, String remotePath) {
        NWebCli.of().POST(resolveUrl("put-file"))
                .addFormData("content", localPath)
                .addFormData("path", remotePath)
                .doWith(this::prepareSecurity)
                .run().getContent();
    }

    public boolean ensureConnectedSafely() {
        if (isValidAccessToken()) {
            if (isWarningRefreshToken()) {
                try {
                    refreshToken();
                    return true;
                } catch (Exception e) {
                    //
                }
            }
            return true;
        }
        if (isValidRefreshToken()) {
            try {
                refreshToken();
                return true;
            } catch (Exception e) {
                //
            }
        }
        try {
            login(connexionString.getUser(), connexionString.getPassword());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public RnshHttpClient ensureConnected() {
        if (isValidAccessToken()) {
            if (isWarningRefreshToken()) {
                try {
                    refreshToken();
                    return this;
                } catch (Exception e) {
                    //
                }
            }
            return this;
        }
        if (isValidRefreshToken()) {
            try {
                refreshToken();
                return this;
            } catch (Exception e) {
                //
            }
        }
        login(connexionString.getUser(), connexionString.getPassword());
        return this;
    }

    private NConnexionString toSafeConnexionString(NConnexionString connexionString) {
        if (connexionString == null) {
            return new NConnexionString();
        }
        NConnexionString c = connexionString.copy();
        c.setPassword("***");
        return c;
    }

    public static class LoginResult {
        public String userId;
        public String userName;
        public String accessToken;
        public String refreshToken;
        public long accessTokenLastValidityTime;
        public long refreshTokenLastValidityTime;
    }

    public boolean isValidAccessToken() {
        if (this.loginResult != null) {
            if (this.loginResult.accessToken != null && this.loginResult.accessTokenLastValidityTime != 0) {
                if (new Date(this.loginResult.accessTokenLastValidityTime - accessTokenSafePeriod).compareTo(new Date()) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWarningRefreshToken() {
        if (this.loginResult != null) {
            if (this.loginResult.refreshToken != null && this.loginResult.refreshTokenLastValidityTime != 0) {
                Date now = new Date();
                if (
                        new Date(this.loginResult.refreshTokenLastValidityTime - accessTokenSafePeriod).compareTo(now) >= 0
                                && new Date(this.loginResult.refreshTokenLastValidityTime - refreshTokenSafePeriod).compareTo(now) < 0
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValidRefreshToken() {
        if (this.loginResult != null) {
            if (this.loginResult.refreshToken != null && this.loginResult.refreshTokenLastValidityTime != 0) {
                if (new Date(this.loginResult.refreshTokenLastValidityTime - accessTokenSafePeriod).compareTo(new Date()) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void refreshToken(String refreshToken) {
        if (NBlankable.isBlank(refreshToken)) {
            throw new NIllegalArgumentException(NMsg.ofC("missing refresh token"));
        }
        NWebResponse response = NWebCli.of().POST(resolveUrl("refresh-token"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("token", this.loginResult.refreshToken)
                )
                .run();
        if (response.isOk()) {
            LoginResult rr = response.getContentAsJson(LoginResult.class);
            if (rr != null) {
                if (!NBlankable.isBlank(rr.accessToken)) {
                    this.loginResult = rr;
                    return;
                }
            }
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("unable to login to %s", toSafeConnexionString(resolveConnexionString())));
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to login to %s", toSafeConnexionString(resolveConnexionString())));
    }

    private void refreshToken() {
        if (this.loginResult == null || NBlankable.isBlank(this.loginResult.refreshToken)
                || this.loginResult.refreshTokenLastValidityTime == 0
                || new Date(this.loginResult.accessTokenLastValidityTime).compareTo(new Date()) <= 0
        ) {
            throw new NIllegalArgumentException(NMsg.ofC("missing refresh token"));
        }
        refreshToken(this.loginResult.refreshToken);
    }

    public void login(String login, String password) {
        NWebResponse response = NWebCli.of().POST(resolveUrl("login"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("userName", login)
                                .put("password", password).build()
                )
                .run();
        if (response.isOk()) {
            LoginResult rr = response.getContentAsJson(LoginResult.class);
            if (rr != null) {
                if (!NBlankable.isBlank(rr.accessToken)) {
                    this.loginResult = rr;
                    return;
                }
            }
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("unable to login to %s", toSafeConnexionString(resolveConnexionString())));
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to login to %s", toSafeConnexionString(resolveConnexionString())));
    }

    private void prepareSecurity(NWebRequest r) {
        if (loginResult != null && !NBlankable.isBlank(loginResult.accessToken)) {
            if (!NBlankable.isBlank(loginResult.accessToken)) {
                r.setAuthorizationBearer(loginResult.accessToken);
            }
        }
    }

    private NConnexionString resolveConnexionString() {
        NConnexionString c = connexionString == null ? new NConnexionString() : connexionString.copy();
        if (NBlankable.isBlank(c.getHost())) {
            c.setHost("localhost");
        }
        if (NBlankable.isBlank(c.getPort())) {
            c.setPort("8899");
        }
        Map<String, List<String>> qm = c.getQueryMap().orElse(new HashMap<>());
        String context = NOptional.ofFirst(qm.get(CONTEXT_PATH_PARAM)).orElse(null);
        if (NBlankable.isBlank(context)) {
            qm.put(CONTEXT_PATH_PARAM, new ArrayList<>(Arrays.asList("/")));
        } else {
            qm.put(CONTEXT_PATH_PARAM, new ArrayList<>(Arrays.asList(context)));
        }
        c.setQueryMap(qm);
        return c;
    }

    private String resolveUrl(String extra) {
        NConnexionString c = resolveConnexionString();
        NConnexionString c2 = new NConnexionString();
        String context = NOptional.ofFirst(c.getQueryMap().orElse(new HashMap<>()).get(CONTEXT_PATH_PARAM)).orElse("/");
        c2.setProtocol("https");
        switch (c.getProtocol()) {
            case "rnsh":
            case "rnsh-http":{
                c2.setProtocol("http");
                break;
            }
            case "rnshs":
            case "rnsh-https":{
                c2.setProtocol("https");
                break;
            }
        }
        c2.setHost(c.getHost());
        c2.setPort(c.getPort());
        c2.setPath(NStringUtils.pjoin("/",context, extra));
        return c2.toString();
    }

    public ExecResult exec(String... command) {
        return exec(command, null);
    }

    public static class ExecResult {
        private int code;
        private NInputSource out;
        private NInputSource err;

        public ExecResult(int code, NInputSource out, NInputSource err) {
            this.code = code;
            this.out = out;
            this.err = err;
        }

        public int getCode() {
            return code;
        }

        public NInputSource getOut() {
            return out;
        }

        public NInputSource getErr() {
            return err;
        }
    }

    public ExecResult exec(String[] command, NInputContentProvider inputSource) {
        NWebResponse r = NWebCli.of().POST(resolveUrl("exec"))
                .setFormData("command", NCmdLine.of(command == null ? new String[0] : command).toString())
                .setFormData(inputSource == null ? null : "in", inputSource)
                .doWith(this::prepareSecurity)
                .run();
        return new ExecResult(
                r.getHeader("X-EXEC-CODE").flatMap(x -> NLiteral.of(x).asInt()).orElse(0),
                r.getContent(),
                NInputSource.of(NInputSource.of(new byte[0]))
        );
    }

    public static class NFileInfo {
        private String path;
        private long contentLength;
        private String contentType;
        private NPathType pathType;
        private int directoryChildrenCount;

        public int getDirectoryChildrenCount() {
            return directoryChildrenCount;
        }

        public NFileInfo setDirectoryChildrenCount(int directoryChildrenCount) {
            this.directoryChildrenCount = directoryChildrenCount;
            return this;
        }

        public String getPath() {
            return path;
        }

        public NFileInfo setPath(String path) {
            this.path = path;
            return this;
        }

        public long getContentLength() {
            return contentLength;
        }

        public NFileInfo setContentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public String getContentType() {
            return contentType;
        }

        public NFileInfo setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public NPathType getPathType() {
            return pathType;
        }

        public NFileInfo setPathType(NPathType pathType) {
            this.pathType = pathType;
            return this;
        }
    }

    public NFileInfo getFileInfo(String remotePath) {
        return NWebCli.of().POST(resolveUrl("file-info"))
                .doWith(this::prepareSecurity)
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("path", remotePath)
                )
                .run().getContentAsJson(NFileInfo.class);
    }

    public String[] listNames(String remotePath) {
        return NWebCli.of().POST(resolveUrl("directory-list-names"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("path", remotePath)
                )
                .doWith(this::prepareSecurity)
                .run().getContentAsJson(String[].class);
    }

    public String digest(String remotePath, String algo) {
        Map<String, Object> path = (Map<String, Object>) NWebCli.of().POST(resolveUrl("file-digest"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("path", remotePath)
                                .put("algo", algo)
                )
                .doWith(this::prepareSecurity)
                .run().getContentAsJson(Map.class);
        return path == null ? null : (String) path.get("hash");
    }


    public List<NPathChildStringDigestInfo> directoryListDigest(String remotePath, String algo) {
        Map<String, Object>[] res = (Map[]) NWebCli.of().POST(resolveUrl("directory-list-digest"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("path", remotePath)
                                .put("algo", algo)
                )
                .doWith(this::prepareSecurity)
                .run().getContentAsJson(Map[].class);
        if (res == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(res).map(x ->
                new NPathChildStringDigestInfo()
                        .setName((String) x.get("name"))
                        .setDigest((String) x.get("digest"))
        ).collect(Collectors.toList());
    }

    public NFileInfo[] listFileInfos(String remotePath) {
        return NWebCli.of().POST(resolveUrl("directory-list-infos"))
                .setJsonRequestBody(
                        NMapBuilder.ofLinked()
                                .put("path", remotePath)
                )
                .doWith(this::prepareSecurity)
                .run().getContentAsJson(NFileInfo[].class);
    }
}
