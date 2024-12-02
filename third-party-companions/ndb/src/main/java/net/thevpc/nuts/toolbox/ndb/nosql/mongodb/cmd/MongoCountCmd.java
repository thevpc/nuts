package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CountCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoCountCmd extends CountCmd<NMongoConfig> {
    public MongoCountCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runCount(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            NSession session = NSession.get().get();
            session.out().println(mongoCollection.countDocuments(docWhere));
        });
    }


}
