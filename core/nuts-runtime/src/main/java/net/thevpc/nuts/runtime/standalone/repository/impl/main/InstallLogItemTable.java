package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBTableFile;

public class InstallLogItemTable {
    static NanoDBTableFile<NutsInstallLogRecord> of(NutsSession session) {
        NanoDB db = InstallLogDB.of(session);
        return db.tableBuilder(NutsInstallLogRecord.class, session).setNullable(false)
                .addAllFields()
                .getOrCreate();
    }
}
