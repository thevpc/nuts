package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.MongoNamespace;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.RenameTableCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;

public class MongoRenameTableCmd extends RenameTableCmd<NMongoConfig> {
    public MongoRenameTableCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }


    @Override
    protected void runRenameTable(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            String dbn = options.getDatabaseName();
            mongoCollection.renameCollection(new MongoNamespace(dbn, eq.getNewName()));
            NSession session = NSession.of().get();
            session.out().println(true);
        });
    }


}
