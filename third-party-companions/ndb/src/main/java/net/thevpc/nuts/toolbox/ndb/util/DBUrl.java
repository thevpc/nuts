package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringMapFormatBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * name
 * ssh://remoteUser@remoteServer[tempFolder]:/user:password@host:port
 */
public class DBUrl {
    private String name;
    private String host;
    private String port;
    private String dbname;
    private String user;
    private String password;
    private String remotePassword;
    private String remoteServer;
    private String remoteUser;
    private String remoteTempFolder;

    public static NOptional<DBUrl> parse(String name) {
        if (name == null) {
            return NOptional.ofNamedEmpty("dburl");
        }
        name = name.trim();
        if (name.startsWith("@")) {
            return NOptional.of(new DBUrl(name.substring(1)));
        }
        String _remoteUser = "(?<remoteUser>[a-zA-Z][^:]*)";
        String _remotePassword = "(?<password>[^:]*)";
        String _remoteServer = "(?<remoteServer>[^:\\]]+)";
        String _params = "(?<params>\\[[^\\]]*\\])?";
        String _password = "(?<password>[^:]*)";
        String _user = "(?<user>[a-zA-Z][^:]*)";
        String _host = "(?<host>[^:]+)";
        String _port = "(?<port>[\\d]+)";
        Pattern.compile(
                NMsg.ofV(
                        "(ssh://($remoteUser(:$remotePassword)?@)?$remoteServer$_params:/)?($user(:)@)$host(:$port)",
                        NMaps.of(
                                "remoteUser", _remoteUser,
                                "remotePassword", _remotePassword,
                                "remoteServer", _remoteServer,
                                "password", _password,
                                "params", _params,
                                "user", _user,
                                "host", _host,
                                "port", _port
                        )
                ).toString()

        );
        return null;
    }

    public DBUrl(String name) {
        this.name = name;
    }

    public DBUrl(String host, String port, String dbname, String user, String password, String remoteServer, String remoteUser, String remotePassword, String remoteTempFolder) {
        this(null, host, port, dbname, user, password, remoteServer, remoteUser, remotePassword, remoteTempFolder);
    }

    public DBUrl(String name, String host, String port, String dbname, String user, String password, String remoteServer, String remoteUser, String remotePassword, String remoteTempFolder) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.password = password;
        this.remoteServer = remoteServer;
        this.remoteUser = remoteUser;
        this.remotePassword = remotePassword;
        this.remoteTempFolder = remoteTempFolder;
    }

    public String toString() {
        if (name != null) {
            return "@" + name;
        }
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(remoteServer)) {
            sb.append("ssh://");
            if (!NBlankable.isBlank(remoteUser)) {
                sb.append(remoteUser);
                if (!NBlankable.isBlank(remotePassword)) {
                    sb.append(":");
                    sb.append(remotePassword);
                }
                sb.append("@");
            }
            sb.append(remoteServer);
            if (!NBlankable.isBlank(remoteTempFolder)) {
                Map<String, String> e = new HashMap<>();
                e.put("temp", remoteTempFolder);
                sb.append("[");
                sb.append(NStringMapFormat.COMMA_FORMAT.builder().setEscapeChars("]").build().format(e));
                sb.append("]");
            }
            sb.append(":");
        }
        if (!NBlankable.isBlank(user)) {
            sb.append(user);
            if (!NBlankable.isBlank(password)) {
                sb.append(":");
                sb.append(password);
            }
            sb.append("@");
        }
        sb.append(remoteServer);
        if (!NBlankable.isBlank(port)) {
            sb.append(":");
            sb.append(port);
        }
        return sb.toString();
    }
}
