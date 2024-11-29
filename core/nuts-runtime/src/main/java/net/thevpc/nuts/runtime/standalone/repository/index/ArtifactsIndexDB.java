package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.env.NEnvs;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NWorkspace;
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


    public ArtifactsIndexDB(String tableName, NanoDB db) {
//        this.tableName = tableName;
//        this.db = db;
//        this.ws = ws;
        table = db.createTable(def(tableName, db), true);
    }

    public static ArtifactsIndexDB of() {
        synchronized (NWorkspace.get()) {
            ArtifactsIndexDB o = (ArtifactsIndexDB) NEnvs.of().getProperties().get(ArtifactsIndexDB.class.getName());
            if (o == null) {
                o = new ArtifactsIndexDB(DEFAULT_ARTIFACT_TABLE_NAME, CacheDB.of());
                NEnvs.of().getProperties().put(ArtifactsIndexDB.class.getName(), o);
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

    public NStream<NId> findAll() {
        return table.stream();
    }

    public NStream<NId> findByGroupId(String groupId) {
        return table.findByIndex("groupId", groupId);
    }

    public NStream<NId> findByArtifactId(String artifactId) {
        return table.findByIndex("artifactId", artifactId);
    }

    public void add(NId id) {
        table.add(id);
    }

    public void flush() {
        table.flush();
    }


    public boolean contains(NId id) {
        return table.findByIndex("id",
                id.getLongId()
                        .builder().setRepository(id.getRepository())
                        .build().toDependency()
        ).findAny().orNull() != null;
    }
}
