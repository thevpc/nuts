package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.toolbox.ndb.base.cmd.RestoreCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;

public class MongoRestoreCmd extends RestoreCmd<NMongoConfig> {
    public MongoRestoreCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }


}
