package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;

public class InstallLogItemTable {
    static NanoDBTableFile<NInstallLogRecord> of(NWorkspace workspace) {
        NanoDB db = InstallLogDB.of(workspace);
        return db.tableBuilder(NInstallLogRecord.class).setNullable(false)
                .addAllFields()
                .getOrCreate();
    }
}
