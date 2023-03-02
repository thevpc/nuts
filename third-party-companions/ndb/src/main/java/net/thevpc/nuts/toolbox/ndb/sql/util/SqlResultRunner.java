package net.thevpc.nuts.toolbox.ndb.sql.util;

import java.sql.ResultSet;

public interface SqlResultRunner {
    void run(ResultSet rs, SqlHelper.SqlResultMetaData m) throws Exception;
}
