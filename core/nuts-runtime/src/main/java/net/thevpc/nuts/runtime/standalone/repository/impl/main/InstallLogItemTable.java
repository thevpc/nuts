package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableStore;

public class InstallLogItemTable {
    static NanoDBTableStore<NInstallLogRecord> of(NWorkspace workspace) {
        NanoDB db = ((NWorkspaceExt)workspace).store().varDB();
        return db.tableBuilder(NInstallLogRecord.class).setNullable(false)
                .addAllFields()
                .getOrCreate();
    }

}
