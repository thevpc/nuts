package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.NSession;

public interface SqlRunnable {
    void run(SqlHelper h, NSession session) throws Exception;
}
