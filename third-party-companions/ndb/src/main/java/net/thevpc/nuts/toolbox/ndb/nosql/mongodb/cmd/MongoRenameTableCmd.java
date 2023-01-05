package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.MongoNamespace;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CountCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.RenameTableCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import org.bson.Document;

public class MongoRenameTableCmd extends RenameTableCmd<NMongoConfig> {
    public MongoRenameTableCmd(NMongoMain support) {
        super(support);
    }

    @Override
    public NMongoMain getSupport() {
        return (NMongoMain) super.getSupport();
    }


    @Override
    protected void runRenameTable(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            String dbn = options.getDatabaseName();
            mongoCollection.renameCollection(new MongoNamespace(dbn, eq.getNewName()));
            session.out().printlnf(true);
        });
    }


}
