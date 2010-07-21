/**
 * Copyright (C) 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.flyway.core;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.dbsupport.hsql.HsqlDbSupport;
import com.googlecode.flyway.core.dbsupport.mysql.MySQLDbSupport;
import com.googlecode.flyway.core.dbsupport.oracle.OracleDbSupport;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.migration.java.JavaMigrationResolver;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.runtime.DbCleaner;
import com.googlecode.flyway.core.runtime.DbMigrator;
import com.googlecode.flyway.core.runtime.MetaDataTable;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central service locator.
 */
public class Flyway {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    /**
     * The base package where the Java migrations are located. (default:
     * db.migration)
     */
    private String basePackage = "db.migration";

    /**
     * The base directory on the classpath where the Sql migrations are located.
     * (default: sql/location)
     */
    private String baseDir = "db/migration";

    /**
     * The encoding of Sql migrations.
     * (default: UTF-8)
     */
    private String encoding = "UTF-8";

    /**
     * The name of the schema metadata table that will be used by flyway.
     * (default: schema_version)
     */
    private String schemaMetaDataTable = "schema_version";

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration
     * scripts.
     */
    private Map<String, String> placeholders = new HashMap<String, String>();

    /**
     * The prefix of every placeholder. (default: ${ )
     */
    private String placeholderPrefix = "${";

    /**
     * The suffix of every placeholder. (default: } )
     */
    private String placeholderSuffix = "}";

    /**
     * @param basePackage The base package where the migrations are located. (default:
     *                    db.migration)
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param baseDir The base directory on the classpath where the Sql migrations
     *                are located. (default: sql/location)
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @param encoding The encoding of Sql migrations.
     *                 (default: UTF-8)
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @param schemaMetaDataTable The name of the schema metadata table that will be used by
     *                            flyway. (default: schema_maintenance_history)
     */
    public void setSchemaMetaDataTable(String schemaMetaDataTable) {
        this.schemaMetaDataTable = schemaMetaDataTable;
    }

    /**
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql
     *                     migration scripts.
     */
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * The transaction template to use.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * Database-specific functionality.
     */
    private DbSupport dbSupport;

    /**
     * Supports reading and writing to the metadata table.
     */
    private MetaDataTable metaDataTable;

    /**
     * @return Supports reading and writing to the metadata table.
     */
    public MetaDataTable getMetaDataTable() {
        return metaDataTable;
    }

    /**
     * @param dataSource The datasource to use. Must have the necessary privileges to
     *                   execute ddl.
     */
    public void setDataSource(DataSource dataSource) {
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        jdbcTemplate = new JdbcTemplate(dataSource);

        dbSupport = initDbSupport();
        LOG.debug("Schema: " + dbSupport.getCurrentSchema(jdbcTemplate));

        metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, schemaMetaDataTable);
    }

    /**
     * Starts the database migration.
     *
     * @return The number of successfully applied migrations.
     * @throws Exception Thrown when the migration failed.
     */
    public int migrate() throws Exception {
        if (!dbSupport.supportsLocking()) {
            LOG.info(getDatabaseProductName() + " does not support locking. No concurrent migration supported.");
        }

        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(new SqlMigrationResolver(baseDir, placeholderReplacer, encoding));
        migrationResolvers.add(new JavaMigrationResolver(basePackage));

        DbMigrator dbMigrator = new DbMigrator(transactionTemplate, jdbcTemplate, dbSupport, migrationResolvers,
                metaDataTable);
        return dbMigrator.migrate();
    }

    /**
     * Drops all object in the schema.
     */
    public void clean() {
        DbCleaner dbCleaner = new DbCleaner(transactionTemplate, jdbcTemplate, dbSupport);
        dbCleaner.clean();
    }

    /**
     * Logs the status (current version) of the database.
     */
    public void status() {
        Migration migration = getMetaDataTable().latestAppliedMigration();

        List<Migration> migrations = new ArrayList<Migration>();
        if (migration != null) {
            migrations.add(migration);
        }

        dumpMigrations(migrations);
    }

    /**
     * Logs the history (all applied migrations) of the database.
     */
    public void history() {
        dumpMigrations(getMetaDataTable().allAppliedMigrations());
    }

    /**
     * Dumps this list of migrations in the log file.
     *
     * @param migrations The list of migrations to dump.
     */
    private void dumpMigrations(List<Migration> migrations) {
        LOG.info("+-------------+---------------------------+----------------+-----------+");
        LOG.info("| Version     | Description               | Execution time | State     |");
        LOG.info("+-------------+---------------------------+----------------+-----------+");

        if (migrations.isEmpty()) {
            LOG.info("| No migrations applied yet                                            |");
        } else {
            for (Migration migration : migrations) {
                LOG.info("| " + StringUtils.trimOrPad(migration.getVersion().getVersion(), 11)
                        + " | " + StringUtils.trimOrPad(migration.getVersion().getDescription(), 25)
                        + " | " + StringUtils.trimOrPad(TimeFormat.format(migration.getExecutionTime()), 14)
                        + " | " + StringUtils.trimOrPad(migration.getState().name(), 9) + " |");
            }
        }

        LOG.info("+-------------+---------------------------+----------------+-----------+");
    }

    /**
     * Creates and initializes the Flyway metadata table.
     *
     * @param initialVersion (Optional) The initial version to put in the metadata table. Only migrations with a version number
     *                       higher than this one will be considered for this database.
     */
    public void init(SchemaVersion initialVersion) {
        metaDataTable.init(initialVersion);
    }

    /**
     * Initializes the appropriate DbSupport class for the database product used
     * by the data source.
     *
     * @return The appropriate DbSupport class.
     */
    private DbSupport initDbSupport() {
        String databaseProductName = getDatabaseProductName();

        LOG.debug("Database: " + databaseProductName);

        Collection<DbSupport> dbSupports = new ArrayList<DbSupport>();

        dbSupports.add(new HsqlDbSupport());
        dbSupports.add(new H2DbSupport());
        dbSupports.add(new MySQLDbSupport());
        dbSupports.add(new OracleDbSupport());

        for (DbSupport dbSupport : dbSupports) {
            if (dbSupport.supportsDatabase(databaseProductName)) {
                return dbSupport;
            }
        }

        throw new IllegalArgumentException("Unsupported Database: " + databaseProductName);
    }

    /**
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private String getDatabaseProductName() {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                if (databaseMetaData == null) {
                    throw new IllegalStateException("Unable to read database metadata while it is null!");
                }
                return connection.getMetaData().getDatabaseProductName();
            }
        });
    }
}
