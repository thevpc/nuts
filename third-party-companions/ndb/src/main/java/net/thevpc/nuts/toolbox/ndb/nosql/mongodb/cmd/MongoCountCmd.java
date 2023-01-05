package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.result.DeleteResult;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CountCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.DeleteCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import org.bson.Document;

public class MongoCountCmd extends CountCmd<NMongoConfig> {
    public MongoCountCmd(NMongoMain support) {
        super(support);
    }

    @Override
    public NMongoMain getSupport() {
        return (NMongoMain) super.getSupport();
    }

    protected void runCount(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            session.out().printlnf(mongoCollection.countDocuments(docWhere));
        });
    }


}
