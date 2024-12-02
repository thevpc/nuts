package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.InsertCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoInsertCmd extends InsertCmd<NMongoConfig> {
    public MongoInsertCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    protected void runInsert(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document d = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    d.putAll(Document.parse(s));
                }
            }
            NSession session = NSession.get().get();
            session.out().println(mongoCollection.insertOne(d));
        });
    }
}
