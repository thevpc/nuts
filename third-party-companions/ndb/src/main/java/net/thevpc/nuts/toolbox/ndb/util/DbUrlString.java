package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbUrlString {
    private static Pattern SSH_PATTERN = Pattern.compile(
            "(ssh:" +
                    "((?<suser>([a-zA-Z]([a-zA-Z0-9_-])*))(:(?<spassword>([^@]+)))?@)?" +
                    "((?<sserver>([a-zA-Z0-9._-]+))(:(?<sport>[0-9]+))?)" +
                    ")(/(?<spath>.*))"
    );
    private static Pattern DB_PATTERN = Pattern.compile(
            "((((?<duser>([a-zA-Z]([a-zA-Z0-9_-])*))(:(?<dpassword>([^@]+)))?@)?" +
                    "((?<dserver>([a-zA-Z0-9._-]+))(:(?<dport>[0-9]+))?)?)" +
                    "(/(?<dpath>(.*)))?)"
    );
    private static Pattern DB_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_-])*");
    private String sshUser;
    private String sshPassword;
    private String sshServer;
    private Integer sshPort;
    private String dbUser;
    private String dbPassword;
    private String dbServer;
    private Integer dbPort;
    private String dbPath;

    public static NOptional<DbUrlString> parse(String value) {
        if (value == null || NBlankable.isBlank(value)) {
            return NOptional.ofNamedEmpty("DbUrlString");
        }
        value = value.trim();
        DbUrlString v = new DbUrlString();
        if (value.startsWith("ssh:")) {
            if (!fillSsh(v, value)) {
                return NOptional.ofNamedEmpty("ssh url");
            }
            return NOptional.of(v);
        } else {
            if (!fillDb(v, value)) {
                return NOptional.ofNamedEmpty("db url");
            }
            return NOptional.of(v);
        }
    }

    private static boolean fillSsh(DbUrlString v, String value) {
        Matcher matcher = SSH_PATTERN.matcher(value);
        if (matcher.matches()) {
            String sPortString = matcher.group("sport");
            v.setSshServer(matcher.group("sserver"));
            v.setSshUser(matcher.group("suser"));
            v.setSshPassword(matcher.group("spassword"));
            v.setSshPort((sPortString == null || sPortString.isEmpty()) ? null : Integer.parseInt(sPortString));
            String spath = matcher.group("spath");
            if (!spath.isEmpty()) {
                if (!fillDb(v, spath)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean fillDb(DbUrlString v, String spath) {
        Matcher matcher;
        if (DB_NAME.matcher(spath).matches()) {
            v.setDbPath(spath);
            return true;
        } else {
            matcher = DB_PATTERN.matcher(spath);
            if (matcher.matches()) {
                String dPortString = matcher.group("dport");
                v.setDbServer(matcher.group("dserver"));
                v.setDbUser(matcher.group("duser"));
                v.setDbPassword(matcher.group("dpassword"));
                v.setDbPort((dPortString == null || dPortString.isEmpty()) ? null : Integer.parseInt(dPortString));
                v.setDbPath(matcher.group("dpath"));
                return true;
            }
        }
        return false;
    }

    public DbUrlString() {
    }


    public String getDbUser() {
        return dbUser;
    }

    public DbUrlString setDbUser(String dbUser) {
        this.dbUser = dbUser;
        return this;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public DbUrlString setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
        return this;
    }

    public String getDbServer() {
        return dbServer;
    }

    public DbUrlString setDbServer(String dbServer) {
        this.dbServer = dbServer;
        return this;
    }

    public Integer getDbPort() {
        return dbPort;
    }

    public DbUrlString setDbPort(Integer dbPort) {
        this.dbPort = dbPort;
        return this;
    }

    public String getDbPath() {
        return dbPath;
    }

    public DbUrlString setDbPath(String dbPath) {
        this.dbPath = dbPath;
        return this;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public DbUrlString setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
        return this;
    }

    public String getSshPassword() {
        return sshPassword;
    }

    public DbUrlString setSshPassword(String sshPassword) {
        this.sshPassword = sshPassword;
        return this;
    }

    public String getSshUser() {
        return sshUser;
    }

    public DbUrlString setSshUser(String sshUser) {
        this.sshUser = sshUser;
        return this;
    }

    public String getSshServer() {
        return sshServer;
    }

    public DbUrlString setSshServer(String sshServer) {
        this.sshServer = sshServer;
        return this;
    }

    private boolean includeSsh() {
        if (!NBlankable.isBlank(sshUser)) {
            return true;
        }
        if (!NBlankable.isBlank(sshPassword)) {
            return true;
        }
        if (!NBlankable.isBlank(sshServer)) {
            return true;
        }
        if (sshPort != null) {
            return true;
        }
        return false;
    }

    public String toUrl() {
        StringBuilder sb = new StringBuilder();
        if (includeSsh()) {
            sb.append("ssh:");
            if (!NBlankable.isBlank(sshUser)) {
                sb.append(sshUser);
                if (!NBlankable.isBlank(sshPassword)) {
                    sb.append(':');
                    sb.append(sshPassword);
                }
                sb.append('@');
            }
            sb.append(sshServer);
            if (sshPort != null) {
                sb.append(":");
                sb.append(sshPort);
            }
            sb.append('/');
        }
        if (!NBlankable.isBlank(dbUser)) {
            sb.append(dbUser);
            if (!NBlankable.isBlank(dbPassword)) {
                sb.append(':');
                sb.append(dbPassword);
            }
            sb.append('@');
        }
        if (!NBlankable.isBlank(dbServer)) {
            sb.append(dbServer);
            if (dbPort != null) {
                sb.append(':');
                sb.append(dbPort);
            }
        }
        if (!NBlankable.isBlank(dbPath)) {
            if(sb.length()==0 || sb.charAt(sb.length()-1)!='/') {
                sb.append('/');
            }
            sb.append(dbPath);
        }
        return sb.toString();
    }

    @Override
    public String toString() {

        return "DbUrlString{" +
                "sshUser=" + NStringUtils.formatStringLiteral(sshUser) +
                ", sshPassword=" + NStringUtils.formatStringLiteral(sshPassword) +
                ", sshServer=" + NStringUtils.formatStringLiteral(sshServer) +
                ", sshPort=" + sshPort +
                ", dbUser=" + NStringUtils.formatStringLiteral(dbUser) +
                ", dbPassword=" + NStringUtils.formatStringLiteral(dbPassword) +
                ", dbServer=" + NStringUtils.formatStringLiteral(dbServer) +
                ", dbPort=" + dbPort +
                ", dbPath=" + NStringUtils.formatStringLiteral(dbPath) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbUrlString that = (DbUrlString) o;
        return Objects.equals(sshUser, that.sshUser) && Objects.equals(sshPassword, that.sshPassword) && Objects.equals(sshServer, that.sshServer) && Objects.equals(sshPort, that.sshPort) && Objects.equals(dbUser, that.dbUser) && Objects.equals(dbPassword, that.dbPassword) && Objects.equals(dbServer, that.dbServer) && Objects.equals(dbPort, that.dbPort) && Objects.equals(dbPath, that.dbPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sshUser, sshPassword, sshServer, sshPort, dbUser, dbPassword, dbServer, dbPort, dbPath);
    }
}
