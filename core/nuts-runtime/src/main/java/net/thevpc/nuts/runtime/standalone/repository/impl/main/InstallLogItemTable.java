package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;

public class InstallLogItemTable {
    static NanoDBTableFile<NInstallLogRecord> of(NSession session) {
        NanoDB db = InstallLogDB.of(session);
        return db.tableBuilder(NInstallLogRecord.class, session).setNullable(false)
                .addAllFields()
                .getOrCreate();
    }
}
