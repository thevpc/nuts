package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NConnexionString;

import java.util.Objects;
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
    private NConnexionString ssh;
    private NConnexionString db;

    public static NOptional<DbUrlString> parse(String value) {
        if (value == null || NBlankable.isBlank(value)) {
            return NOptional.ofNamedEmpty("DbUrlString");
        }
        value = value.trim();
        DbUrlString v = new DbUrlString();
        String dbStr = null;
        String dbStrQ = null;
        if (value.startsWith("ssh:")) {
            NOptional<NConnexionString> ssh = NConnexionString.of(value);
            if (ssh.isPresent()) {
                v.ssh = ssh.get();
                dbStr = v.ssh.getPath();
                if(dbStr!=null && dbStr.startsWith("/")){
                    dbStr=dbStr.substring(1);
                }
                dbStrQ = v.ssh.getQueryString();
                v.ssh.setPath(null);
                v.ssh.setQueryString(null);
            } else {
                dbStr = value;
            }
        } else {
            dbStr = value;
        }
        if(dbStr!=null){
            if(dbStrQ!=null){
                dbStr+='?'+dbStrQ;
            }
        }
        NOptional<NConnexionString> db = NConnexionString.of(dbStr);
        if (db.isPresent()) {
            v.db = db.get();
            String path = v.db.getPath();
            if(path!=null && path.startsWith("/")){
                path=path.substring(1);
                v.db.setPath(path);
            }
            return NOptional.of(v);
        } else {
            v.db = new NConnexionString().setPath(dbStr);
        }
        return NOptional.of(v);
    }


    public DbUrlString() {
    }


    public String toUrl() {
        if (ssh != null && db != null) {
            NConnexionString s = ssh.copy();
            NConnexionString d = db.copy();
            s.setPath(d.toString());
            return s.toString();
        }
        if (ssh != null) {
            NConnexionString s = ssh.copy();
            s.setPath("/");
            return s.toString();
        }
        if (db != null) {
            return db.toString();
        }
        return "";
    }

    public NConnexionString getSsh() {
        return ssh;
    }

    public DbUrlString setSsh(NConnexionString ssh) {
        this.ssh = ssh;
        return this;
    }

    public NConnexionString getDb() {
        return db;
    }

    public DbUrlString setDb(NConnexionString db) {
        this.db = db;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbUrlString that = (DbUrlString) o;
        return Objects.equals(ssh, that.ssh) && Objects.equals(db, that.db);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ssh, db);
    }

    @Override
    public String toString() {
        return "DbUrlString{" +
                "ssh=" + ssh +
                ", db=" + db +
                '}';
    }
}
