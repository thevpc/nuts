package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.sql.util.*;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

public class SqlShowSchemaCmd<C extends NdbConfig> extends NdbCmd<C> {
    public SqlShowSchemaCmd(NdbSupportBase<C> support, String... names) {
        super(support, "show-schema");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NSession session, NCmdLine commandLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        NRef<NPath> file = NRef.ofNull();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(commandLine, session, name);
                        break;
                    }
                    case "--long": {
                        commandLine.withNextFlag((v, a, s) -> eq.setLongMode(v));
                        break;
                    }
                    case "--file": {
                        commandLine.withNextEntryValue((v, a, s) -> file.set(NPath.of(v.toString(), session)));
                        break;
                    }
                    default: {
                        fillOptionLast(commandLine, otherOptions);
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }
        //if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
        //    commandLine.throwMissingArgumentByName("--dbname");
        //}
        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        runShowSchema(eq, options, file.get(), session);
    }


    protected void runShowSchema(ExtendedQuery eq, C options, NPath path, NSession session) {
        SqlDB sqlDB = computeSchema(eq, options, session);
        if (path == null) {
            session.out().println(sqlDB);
        } else {
            NElements.of(session).setContentType(session.getOutputFormat()).setNtf(false).print(path);
        }
    }

    protected SqlDB computeSchema(ExtendedQuery eq, C options, NSession session) {
        SqlSupport<C> ss = (SqlSupport<C>) getSupport();
        return ss.callInDb(new SqlCallable<SqlDB>() {
            @Override
            public SqlDB run(SqlHelper c, NSession session) throws Exception {
                String catalog = null;
                String schema = null;
                SqlDB d = new SqlDB();

                Connection connection = c.getConnection();
                DatabaseMetaData metaData = connection.getMetaData();
                String connSchema = connection.getSchema();
                String connCat = connection.getCatalog();
                SqlHelper.loop(
                        metaData.getTables(connCat, connSchema, null, null),
                        (r, m) -> {
                            try {
                                String tabCat = r.getString("TABLE_CAT");
                                String tabSchema = r.getString("TABLE_SCHEM");
                                if (tabCat == null) {
                                    tabCat = connCat;
                                }
                                if (tabSchema == null) {
                                    tabSchema = connSchema;
                                }
                                if (
                                        Objects.equals(tabCat, connCat)
                                                && Objects.equals(tabSchema, connSchema)
                                ) {
                                    String tableType = r.getString("TABLE_TYPE");

                                    SqlTable t = d.getOrCreateTable(tabCat, tabSchema, r.getString("TABLE_NAME"), tableType);
                                    t.remarks = r.getString("REMARKS");
                                    t.typesCatalog = NStringUtils.trimToNull(r.getString("TYPE_CAT"));
                                    t.typesSchema = NStringUtils.trimToNull(r.getString("TYPE_SCHEM"));
                                    t.typeName = NStringUtils.trimToNull(r.getString("TYPE_NAME"));
                                    t.selfReferencingColName = NStringUtils.trimToNull(r.getString("SELF_REFERENCING_COL_NAME"));
                                    t.refGeneration = NStringUtils.trimToNull(r.getString("REF_GENERATION"));
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                for (SqlCatalog sCat : d.catalogs.values()) {
                    for (SqlSchema sSchema : sCat.schemas.values()) {
                        for (Map<String, SqlTable> value : sSchema.tableMaps().values()) {
                            for (SqlTable sTable : value.values()) {
                                fillTableColumns(metaData, connSchema, connCat, sCat, sSchema, sTable);
                                fillTableIndexes(metaData, sCat, sSchema, sTable);
                            }
                        }
                    }
                }
                return d.sort();
            }
        }, options, session);
    }

    private static void fillTableIndexes(DatabaseMetaData metaData, SqlCatalog sCat, SqlSchema sSchema, SqlTable sTable) throws SQLException {
        SqlHelper.loop(
                metaData.getIndexInfo(sCat.catalogName, sSchema.schemaName, sTable.tableName, false, true),
                (r, m) -> {
                    try {
                        String tabCat = r.getString("TABLE_CAT");
                        String tabSchema = r.getString("TABLE_SCHEM");
                        String tabName = r.getString("TABLE_NAME");
                        if (tabCat == null) {
                            tabCat = sCat.catalogName;
                        }
                        if (tabSchema == null) {
                            tabSchema = sSchema.schemaName;
                        }
                        if (tabName == null) {
                            tabName = sTable.tableName;
                        }
                        if (
                                Objects.equals(tabCat, sCat.catalogName)
                                        && Objects.equals(tabSchema, sSchema.schemaName)
                                        && Objects.equals(tabName, sTable.tableName)
                        ) {

                            String indexQualifier = r.getString("INDEX_QUALIFIER");
                            String indexName = r.getString("INDEX_NAME");
                            SqlIndex t = sTable.indexes.stream().filter(x ->
                                    Objects.equals(x.indexName, indexName)
                                            && Objects.equals(x.indexQualifier, indexQualifier)
                            ).findFirst().orElse(null);
                            if (t == null) {
                                t = new SqlIndex();
                                t.indexQualifier = indexQualifier;
                                t.indexName = indexName;
                                sTable.indexes.add(t);
                            }
                            SqlIndexColumn c = new SqlIndexColumn();
                            c.nonUnique = r.getBoolean("NON_UNIQUE");
                            c.type = SqlHelper.getIndexTypeName(r.getShort("TYPE"));
                            c.ordinalPosition = r.getShort("ORDINAL_POSITION");
                            c.columnName = r.getString("COLUMN_NAME");
                            String ascOrDesc = NStringUtils.trim(r.getString("ASC_OR_DESC")).toLowerCase();
                            c.asc = ascOrDesc.isEmpty() ? null : ascOrDesc.equals("a");
                            c.pages = r.getLong("PAGES");
                            c.filterCondition = r.getString("FILTER_CONDITION");
                            t.columns.add(c);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static void fillTableColumns(DatabaseMetaData metaData, String connSchema, String connCat, SqlCatalog sCat, SqlSchema sSchema, SqlTable sTable) throws SQLException {
        SqlHelper.loop(
                metaData.getColumns(sCat.catalogName, sSchema.schemaName, sTable.tableName, null),
                (r, m) -> {
                    try {
                        String tabCat = r.getString("TABLE_CAT");
                        String tabSchema = r.getString("TABLE_SCHEM");
                        if (tabCat == null) {
                            tabCat = connCat;
                        }
                        if (tabSchema == null) {
                            tabSchema = connSchema;
                        }
                        if (
                                Objects.equals(tabCat, connCat)
                                        && Objects.equals(tabSchema, connSchema)
                        ) {
                            SqlColumn t = new SqlColumn();
                            t.columnName = r.getString("COLUMN_NAME");
                            t.dataType = SqlHelper.getSqlTypeName(r.getInt("DATA_TYPE")).toLowerCase();
                            t.typeName = r.getString("TYPE_NAME");
                            t.columnSize = r.getInt("COLUMN_SIZE");
                            //t.bufferLength = r.getInt("BUFFER_LENGTH");
                            t.decimalDigits = r.getInt("DECIMAL_DIGITS");
                            t.numPrecRadix = r.getInt("NUM_PREC_RADIX");
                            t.nullable = SqlHelper.getColumnNullableName(r.getInt("NULLABLE"));
                            t.nullable2 = SqlHelper.getColumnYesNoEmptyName(r.getString("IS_NULLABLE"));
                            t.remarks = r.getString("REMARKS");
                            t.columnDef = r.getString("COLUMN_DEF");
                            //t.sqlDataType = r.getInt("SQL_DATA_TYPE");
                            //t.sqlDateTimeSub = r.getInt("SQL_DATETIME_SUB");
                            t.charOctetLength = r.getInt("CHAR_OCTET_LENGTH");
                            t.ordinalPosition = r.getInt("ORDINAL_POSITION");
                            t.scopeCatalog = r.getString("SCOPE_CATALOG");
                            t.scopeSchema = r.getString("SCOPE_SCHEMA");
                            t.scopeTable = r.getString("SCOPE_TABLE");
                            t.sourceDataType = r.getShort("SOURCE_DATA_TYPE");
                            t.autoIncrement = SqlHelper.getColumnYesNoEmptyName(r.getString("IS_AUTOINCREMENT"));
                            t.generatedColumn = SqlHelper.getColumnYesNoEmptyName(r.getString("IS_GENERATEDCOLUMN"));
                            sTable.columns.add(t);

                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        SqlHelper.loop(
                metaData.getPrimaryKeys(sCat.catalogName, sSchema.schemaName, sTable.tableName),
                (r, m) -> {
                    try {
                        String tabCat = r.getString("TABLE_CAT");
                        String tabSchema = r.getString("TABLE_SCHEM");
                        String tabName = r.getString("TABLE_NAME");
                        if (tabCat == null) {
                            tabCat = connCat;
                        }
                        if (tabSchema == null) {
                            tabSchema = connSchema;
                        }
                        if (tabName == null) {
                            tabName = sTable.tableName;
                        }
                        if (
                                Objects.equals(tabCat, connCat)
                                        && Objects.equals(tabSchema, connSchema)
                                        && Objects.equals(tabName, sTable.tableName)
                        ) {
                            SqlPrimaryKey t = new SqlPrimaryKey();
                            t.columnName = r.getString("COLUMN_NAME");
                            t.keySeq = r.getShort("KEY_SEQ");
                            t.pkName = r.getString("PK_NAME");
                            sTable.primaryKeys.add(t);
                            SqlColumn c = sTable.columns.stream().filter(x -> x.columnName.equals(t.columnName))
                                    .findFirst().get();
                            c.primaryKeySeq = t.keySeq;
                            c.primaryKey = true;
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        SqlHelper.loop(
                metaData.getImportedKeys(sCat.catalogName, sSchema.schemaName, sTable.tableName),
                (r, m) -> {
                    try {
//                        String fkTableCat = r.getString("FKTABLE_CAT");
//                        String fkTableSchema = r.getString("FKTABLE_SCHEM");
//                        String fkTableName = r.getString("FKTABLE_NAME");
                        String fkColumnName = r.getString("FKCOLUMN_NAME");
                        SqlImportedColumn t = new SqlImportedColumn();
                        t.pkTableCat = r.getString("PKTABLE_CAT");
                        t.pkTableSchema = r.getString("PKTABLE_SCHEM");
                        t.pkTableName = r.getString("PKTABLE_NAME");
                        t.pkColumnName = r.getString("PKCOLUMN_NAME");
                        t.keySeq = r.getShort("KEY_SEQ");
                        t.updateRule = SqlHelper.getImportedKeyRuleName(r.getShort("UPDATE_RULE"));
                        t.deleteRule = SqlHelper.getImportedKeyRuleName(r.getShort("DELETE_RULE"));
                        t.fkName = r.getString("FK_NAME");
                        t.pkName = r.getString("PK_NAME");
                        t.deferrability = SqlHelper.getImportedKeyDeferrability(r.getShort("DEFERRABILITY"));
                        SqlColumn ff = sTable.columns.stream().filter(c -> c.columnName.equals(fkColumnName))
                                .findFirst().get();
                        ff.foreignKeys.add(t);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }


}
