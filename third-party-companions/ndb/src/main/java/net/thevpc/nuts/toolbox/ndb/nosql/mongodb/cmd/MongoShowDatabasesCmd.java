package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.FindIterable;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.cmd.FindCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.ShowDatabasesCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MongoShowDatabasesCmd extends ShowDatabasesCmd<NMongoConfig> {
    public MongoShowDatabasesCmd(NMongoMain support) {
        super(support);
    }

    @Override
    public NMongoMain getSupport() {
        return (NMongoMain) super.getSupport();
    }

    protected void runShowDatabases(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoClient(options, mongoClient -> {
            List<NElement> databases = mongoClient.listDatabases()
                    .into(new ArrayList<>())
                    .stream().map(x -> NElements.of(session).parse(x.toJson(), NElement.class))
                    .map(x->{
                        if(eq.isLongMode()){
                            return x;
                        }
                        return x.asObject().get().get("name").get();
                    })
                    .collect(Collectors.toList());
            session.out().printlnf(databases);
        });
    }

}
