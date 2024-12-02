package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.FindIterable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.FindCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoFindCmd extends FindCmd<NMongoConfig> {
    public MongoFindCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    protected void run(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document w = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    w.putAll(Document.parse(s));
                }
            }
            Document ss = Document.parse("{}");
            for (String s : eq.getSort()) {
                if (!NBlankable.isBlank(s)) {
                    ss.putAll(Document.parse(s));
                }
            }

            FindIterable r = mongoCollection.find(w);
            if (!ss.isEmpty()) {
                r = r.sort(ss);
            }
            if(eq.getLimit()>0){
                r = r.limit((int) eq.getLimit());
            }
            if(eq.getSkip()>0){
                r = r.skip((int) eq.getSkip());
            }
            List<Object> values = new ArrayList<>();
            r.forEach(x -> {
                values.add(x);
            });
            NSession session = NSession.get().get();
            session.out().println(values);
        });
    }
}
