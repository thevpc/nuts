package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class MongoAggregateCmd extends NdbCmd<NMongoConfig> {
    public MongoAggregateCmd(NMongoSupport support) {
        super(support, "aggregate");
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        NMongoConfig otherOptions = createConfigInstance();

        String status = "";
        while (cmdLine.hasNext()) {
            switch (status) {
                case "": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--config": {
                            readConfigNameOption(cmdLine, session, name);
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            cmdLine.withNextEntry((v, a, s) -> eq.setTable(v));
                            break;
                        }
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        case "--limit": {
                            cmdLine.withNextValue((v, a, s) -> eq.setLimit(v.asLong().get()));
                            break;
                        }
                        case "--skip": {
                            cmdLine.withNextValue((v, a, s) -> eq.setSkip(v.asLong().get()));
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            fillOptionLast(cmdLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--where": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getWhere().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sort": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSort().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            cmdLine.throwMissingArgument("--table");
        }
        NMongoConfig options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        run(eq, options, session);
    }


    protected void run(ExtendedQuery eq, NMongoConfig options, NSession session) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            List<Bson> pipline = new ArrayList<>();
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    s = s.trim();
                    if (s.length() > 0) {
                        pipline.addAll(getSupport().parseBsonList(s));
                    }
                }
            }
            AggregateIterable r = mongoCollection.aggregate(pipline);
            List<Object> values = new ArrayList<>();
            long skip = Math.max(0, eq.getSkip());
            long limit = Math.max(0, eq.getLimit());
            MongoCursor it = r.iterator();
            while (it.hasNext()) {
                if (skip > 0) {
                    skip--;
                    continue;
                }
                values.add(it.next());
                if (limit > 0) {
                    limit--;
                    if (limit <= 0) {
                        break;
                    }
                }
            }
            session.out().println(values);
        });
    }


}
