package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.FindIterable;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.base.cmd.FindCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MongoFindCmd extends FindCmd<NMongoConfig> {
    public MongoFindCmd(NMongoMain support) {
        super(support);
    }

    @Override
    public NMongoMain getSupport() {
        return (NMongoMain) super.getSupport();
    }

    @Override
    protected void run(ExtendedQuery eq, NMongoConfig options, NSession session) {
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
            session.out().println(values);
        });
    }
}
