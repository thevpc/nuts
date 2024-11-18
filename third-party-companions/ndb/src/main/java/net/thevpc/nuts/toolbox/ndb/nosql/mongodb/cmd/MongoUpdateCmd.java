package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.result.UpdateResult;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.UpdateCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoUpdateCmd extends UpdateCmd<NMongoConfig> {
    public MongoUpdateCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    protected void runUpdate(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docSet = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    docSet.putAll(Document.parse(s));
                }
            }
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            UpdateResult r = eq.getOne() ?
                    mongoCollection.updateOne(docWhere, docSet)
                    : mongoCollection.updateMany(docWhere, docSet);
            NSession session = NSession.of().get();
            session.out().println(r);
        });
    }

}
