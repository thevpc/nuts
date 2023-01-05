/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.nosql.mongodb;

import com.mongodb.MongoNamespace;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd.*;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author thevpc
 */
public class NMongoMain extends NdbSupportBase<NMongoConfig> {
    private static void setMongoLogEnabled(boolean enable) {
        if (enable) {
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.DEBUG);
        } else {
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.ERROR);
        }
    }

    public NMongoMain(NApplicationContext appContext) {
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
        if (NBlankable.isBlank(options.getRemoteUser())) {
            options.setRemoteUser(System.getProperty("user.name"));
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
}
