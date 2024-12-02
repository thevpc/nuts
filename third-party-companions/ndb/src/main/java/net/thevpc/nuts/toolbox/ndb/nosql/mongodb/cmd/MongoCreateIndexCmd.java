package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CreateIndexCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoCreateIndexCmd extends CreateIndexCmd<NMongoConfig> {
    public MongoCreateIndexCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runCreateIndex(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docSet = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    docSet.putAll(Document.parse(s));
                }
            }
            NSession session = NSession.get().get();
            session.out().println(mongoCollection.createIndex(docSet));
        });
    }

}
