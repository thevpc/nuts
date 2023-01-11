package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.result.UpdateResult;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CreateIndexCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.UpdateCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import org.bson.Document;

public class MongoCreateIndexCmd extends CreateIndexCmd<NMongoConfig> {
    public MongoCreateIndexCmd(NMongoMain support) {
        super(support);
    }

    @Override
    public NMongoMain getSupport() {
        return (NMongoMain) super.getSupport();
    }

    protected void runCreateIndex(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docSet = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    docSet.putAll(Document.parse(s));
                }
            }
            session.out().println(mongoCollection.createIndex(docSet));
        });
    }

}
