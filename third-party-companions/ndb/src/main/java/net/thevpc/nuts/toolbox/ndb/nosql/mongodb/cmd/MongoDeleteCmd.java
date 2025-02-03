package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.result.DeleteResult;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.DeleteCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoDeleteCmd extends DeleteCmd<NMongoConfig> {
    public MongoDeleteCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    protected void runDelete(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            DeleteResult r = eq.getOne() ?
                    mongoCollection.deleteOne(docWhere)
                    : mongoCollection.deleteMany(docWhere);
            NOut.println(r);
        });
    }

}
