package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.ShowTablesCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MongoShowTablesCmd extends ShowTablesCmd<NMongoConfig> {
    public MongoShowTablesCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runShowTables(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoClient(options, mongoClient -> {
            getSupport().doWithMongoDB(options, db -> {
                List<NElement> databases = db.listCollections()
                        .into(new ArrayList<>())
                        .stream().map(x -> NElements.of(session).parse(x.toJson(), NElement.class))
                        .map(x->{
                            if(eq.isLongMode()){
                                return x;
                            }
                            return x.asObject().get().get("name").get();
                        })
                        .collect(Collectors.toList());
                session.out().println(databases);
            });
        });
    }

}
