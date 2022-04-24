package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStream;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBDefaultIndexDefinition;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableDefinition;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;

public class ArtifactsIndexDB {
    public static final String DEFAULT_ARTIFACT_TABLE_NAME = "Artifacts";
    //    private String tableName;
//    private NanoDB db;
//    private NutsWorkspace ws;
    private final NanoDBTableFile<NutsId> table;


    public ArtifactsIndexDB(String tableName, NanoDB db, NutsSession session) {
//        this.tableName = tableName;
//        this.db = db;
//        this.ws = ws;
        table = db.createTable(def(tableName, db), true, session);
    }

    public static ArtifactsIndexDB of(NutsSession session) {
        synchronized (session.getWorkspace()) {
            ArtifactsIndexDB o = (ArtifactsIndexDB) session.env().getProperties().get(ArtifactsIndexDB.class.getName());
            if (o == null) {
                o = new ArtifactsIndexDB(DEFAULT_ARTIFACT_TABLE_NAME, CacheDB.of(session), session);
                session.env().getProperties().put(ArtifactsIndexDB.class.getName(), o);
            }
            return o;
        }
    }

    private static NanoDBTableDefinition<NutsId> def(String name, NanoDB db) {
        return new NanoDBTableDefinition<NutsId>(
                name, NutsId.class, db.getSerializers().of(NutsId.class, false),
                new NanoDBDefaultIndexDefinition<>("id", String.class, false, x -> x.getLongId()
                        .builder().setRepository(x.getRepository()).build().toString()
                ),
                new NanoDBDefaultIndexDefinition<>("groupId", String.class, false, NutsId::getGroupId),
                new NanoDBDefaultIndexDefinition<>("artifactId", String.class, false, NutsId::getArtifactId),
                new NanoDBDefaultIndexDefinition<>("repository", String.class, false, NutsId::getRepository)
        );
    }

    public NutsStream<NutsId> findAll(NutsSession session) {
        return table.stream(session);
    }

    public NutsStream<NutsId> findByGroupId(String groupId, NutsSession session) {
        return table.findByIndex("groupId", groupId, session);
    }

    public NutsStream<NutsId> findByArtifactId(String artifactId, NutsSession session) {
        return table.findByIndex("artifactId", artifactId, session);
    }

    public void add(NutsId id, NutsSession session) {
        table.add(id, session);
    }

    public void flush(NutsSession session) {
        table.flush(session);
    }


    public boolean contains(NutsId id, NutsSession session) {
        return table.findByIndex("id",
                id.getLongId()
                        .builder().setRepository(id.getRepository())
                        .build().toDependency()
                , session).findAny().orNull() != null;
    }
}
