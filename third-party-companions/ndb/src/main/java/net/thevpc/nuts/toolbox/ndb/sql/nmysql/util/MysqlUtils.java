package net.thevpc.nuts.toolbox.ndb.sql.nmysql.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MysqlUtils {

    public static String newDateString() {
        return new SimpleDateFormat("yyyy-MM-dd-HHmmss-SSS").format(new Date());
    }

    public static String toValidFileName(String name, String defaultName) {
        String r = name==null?"":name;
        if (r.isEmpty()) {
            return defaultName;
        }
        return r
                .replace('/', '_')
                .replace('*', '_')
                .replace('?', '_')
                .replace('.', '_')
                .replace('\\', '_');
    }

    public static String getFileName(String name) {
        name=name.replace(File.separatorChar,'/');
        int i = name.lastIndexOf('/');
        if(i>=0){
            name=name.substring(i+1);
        }
        return name;
    }

}
