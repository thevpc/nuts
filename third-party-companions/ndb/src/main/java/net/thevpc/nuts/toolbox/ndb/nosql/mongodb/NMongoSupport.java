/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.nosql.mongodb;

import com.mongodb.client.*;
import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.base.cmd.CopyDBCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd.*;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public class NMongoSupport extends NdbSupportBase<NMongoConfig> {
    private static void setMongoLogEnabled(boolean enable) {
        if (enable) {
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.DEBUG);
        } else {
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.ERROR);
        }
    }

    public NMongoSupport(NApplicationContext appContext) {
        super("mongodb", NMongoConfig.class, appContext);
        setMongoLogEnabled(false);
        declareNdbCmd(new MongoFindCmd(this));
        declareNdbCmd(new MongoShowDatabasesCmd(this));
        declareNdbCmd(new MongoShowTablesCmd(this));
        declareNdbCmd(new MongoInsertCmd(this));
        declareNdbCmd(new MongoUpdateCmd(this));
        declareNdbCmd(new MongoDeleteCmd(this));
        declareNdbCmd(new MongoCountCmd(this));
        declareNdbCmd(new MongoReplaceCmd(this));
        declareNdbCmd(new MongoCreateIndexCmd(this));
        declareNdbCmd(new MongoRenameTableCmd(this));
        declareNdbCmd(new MongoAggregateCmd(this));
        declareNdbCmd(new MongoDumpCmd(this));
        declareNdbCmd(new MongoRestoreCmd(this));
        declareNdbCmd(new CopyDBCmd<>(this));
    }

    public static void doWithMongoCollection(NMongoConfig options, String collection, Consumer<MongoCollection> consumer) {
        doWithMongoClient(options, cli -> {
            MongoDatabase database = cli.getDatabase(options.getDatabaseName());
            consumer.accept(database.getCollection(collection));
        });
    }

    public static void doWithMongoDB(NMongoConfig options, Consumer<MongoDatabase> consumer) {
        doWithMongoClient(options, cli -> {
            MongoDatabase database = cli.getDatabase(options.getDatabaseName());
            consumer.accept(database);
        });
    }

    public static void doWithMongoClient(NMongoConfig options, Consumer<MongoClient> consumer) {
        StringBuilder connectionString = new StringBuilder("mongodb://");
        if (!NBlankable.isBlank(options.getUser())) {
            connectionString.append(options.getUser());
            if (!NBlankable.isBlank(options.getPassword())) {
                connectionString.append(":");
                connectionString.append(options.getPassword());
            }
            connectionString.append("@");
        }
        connectionString.append(options.getHost());
        if (!options.getHost().contains(",") && !options.getHost().contains(":")) {
            if (options.getPort() != null) {
                connectionString.append(":");
                connectionString.append(options.getPort());
            }
        }
        if (!NBlankable.isBlank(options.getDatabaseName())) {
            connectionString.append("/");
            connectionString.append(options.getDatabaseName());
        }
        // mongodb://[username:password@]host1[:port1][,...hostN[:portN]][/[defaultauthdb][?options]]

        try (MongoClient mongoClient = MongoClients.create(connectionString.toString())) {
            consumer.accept(mongoClient);
        }
    }

    public void revalidateOptions(NMongoConfig options) {
        int port = NOptional.of(options.getPort()).mapIf(x -> x <= 0, x -> null, x -> x).ifBlank(27017).get();
        String host = NOptional.of(options.getHost()).ifBlank("localhost").get();
        String user = options.getUser();
        String password = options.getPassword();
        if (NBlankable.isBlank(user) && NBlankable.isBlank(password)) {
            user = "";
            password = "";
        }
        options.setPassword(password);
        options.setUser(user);
        options.setHost(host);
        options.setPort(port);
        if (isRemoteHost(options.getRemoteServer())) {
            if (NBlankable.isBlank(options.getRemoteUser())) {
                options.setRemoteUser(System.getProperty("user.name"));
            }
        }
    }

    public List<Bson> parseBsonList(String JSON_DATA) {
        if (JSON_DATA.startsWith("[")) {
            final CodecRegistry codecRegistry = CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(),
                    new BsonValueCodecProvider(),
                    new DocumentCodecProvider()));

            JsonReader reader = new JsonReader(JSON_DATA);
            BsonArrayCodec arrayReader = new BsonArrayCodec(codecRegistry);

            BsonArray docArray = arrayReader.decode(reader, DecoderContext.builder().build());
            List<Bson> a = new ArrayList<>();
            for (BsonValue doc : docArray.getValues()) {
                a.add(doc.asDocument().toBsonDocument());
            }
            return a;
        } else {
            return Arrays.asList(Document.parse(JSON_DATA));
        }
    }


    public <C extends NdbConfig> String getDumpExt(C options, NSession session) {
        return ""; //folder
    }

    public CmdRedirect createDumpCommand(NPath remoteSql, NMongoConfig options, NSession session) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mongodump");
        cmd.add("--quiet");
        if (!NBlankable.isBlank(options.getHost())) {
            cmd.add("--host=" + options.getHost());
        }
        if(options.getPort()!=null && options.getPort().intValue()>0) {
            cmd.add("--port=" + options.getPort());
        }
        if (!NBlankable.isBlank(options.getUser())) {
            cmd.add("--username=" + options.getUser());
        }
        cmd.add("--db=" + options.getDatabaseName());
        cmd.add("--out=" + remoteSql.toString());
        return new CmdRedirect(NCommandLine.of(cmd), null);
    }


    public CmdRedirect createRestoreCommand(NPath remoteSql, NMongoConfig options, NSession session) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mongorestore");
        cmd.add("--quiet");
        if (!NBlankable.isBlank(options.getHost())) {
            cmd.add("--host=" + options.getHost());
        }
        if(options.getPort()!=null && options.getPort().intValue()>0) {
            cmd.add("--port=" + options.getPort());
        }
        cmd.add("--db=" + options.getDatabaseName());
        cmd.add("--drop");
        if (!NBlankable.isBlank(options.getUser())) {
            cmd.add("--username=" + options.getUser());
        }
        cmd.add(remoteSql.toString());
        return new CmdRedirect(NCommandLine.of(cmd), null);
    }

    @Override
    public DumpRestoreMode getDumpRestoreMode(NMongoConfig options, NSession session) {
        return DumpRestoreMode.FOLDER;
    }
}
