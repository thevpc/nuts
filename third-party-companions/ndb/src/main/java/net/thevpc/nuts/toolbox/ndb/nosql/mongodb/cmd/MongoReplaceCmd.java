package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CountCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.ReplaceCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import org.bson.Document;

public class MongoReplaceCmd extends ReplaceCmd<NMongoConfig> {
    public MongoReplaceCmd(NMongoMain support) {
        super(support);
    }

    @Override
    public NMongoMain getSupport() {
        return (NMongoMain) super.getSupport();
    }

    protected void runReplace(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            Document docSet = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    docSet.putAll(Document.parse(s));
                }
            }
            Object z = mongoCollection.findOneAndReplace(
                    docWhere,
                    docSet
            );
            session.out().println(z);
        });
    }


}
