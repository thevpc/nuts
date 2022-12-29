package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBDefaultIndexDefinition;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableDefinition;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;

public class ArtifactsIndexDB {
    public static final String DEFAULT_ARTIFACT_TABLE_NAME = "Artifacts";
    //    private String tableName;
//    private NanoDB db;
//    private NutsWorkspace ws;
    private final NanoDBTableFile<NId> table;


    public ArtifactsIndexDB(String tableName, NanoDB db, NSession session) {
//        this.tableName = tableName;
//        this.db = db;
//        this.ws = ws;
        table = db.createTable(def(tableName, db), true, session);
    }

    public static ArtifactsIndexDB of(NSession session) {
        synchronized (session.getWorkspace()) {
            ArtifactsIndexDB o = (ArtifactsIndexDB) session.env().getProperties().get(ArtifactsIndexDB.class.getName());
            if (o == null) {
                o = new ArtifactsIndexDB(DEFAULT_ARTIFACT_TABLE_NAME, CacheDB.of(session), session);
                session.env().getProperties().put(ArtifactsIndexDB.class.getName(), o);
            }
            return o;
        }
    }

    private static NanoDBTableDefinition<NId> def(String name, NanoDB db) {
        return new NanoDBTableDefinition<NId>(
                name, NId.class, db.getSerializers().of(NId.class, false),
                new NanoDBDefaultIndexDefinition<>("id", String.class, false, x -> x.getLongId()
                        .builder().setRepository(x.getRepository()).build().toString()
                ),
                new NanoDBDefaultIndexDefinition<>("groupId", String.class, false, NId::getGroupId),
                new NanoDBDefaultIndexDefinition<>("artifactId", String.class, false, NId::getArtifactId),
                new NanoDBDefaultIndexDefinition<>("repository", String.class, false, NId::getRepository)
        );
    }

    public NStream<NId> findAll(NSession session) {
        return table.stream(session);
    }

    public NStream<NId> findByGroupId(String groupId, NSession session) {
        return table.findByIndex("groupId", groupId, session);
    }

    public NStream<NId> findByArtifactId(String artifactId, NSession session) {
        return table.findByIndex("artifactId", artifactId, session);
    }

    public void add(NId id, NSession session) {
        table.add(id, session);
    }

    public void flush(NSession session) {
        table.flush(session);
    }


    public boolean contains(NId id, NSession session) {
        return table.findByIndex("id",
                id.getLongId()
                        .builder().setRepository(id.getRepository())
                        .build().toDependency()
                , session).findAny().orNull() != null;
    }
}
