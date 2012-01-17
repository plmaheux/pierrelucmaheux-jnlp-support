/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.clean;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionException;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;

/**
 * Main workflow for cleaning the database.
 */
public class DbCleaner {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbCleaner.class);

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The transaction template to use.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * The schemas to clean.
     */
    private final String[] schemas;

    /**
     * Creates a new database cleaner.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the database.
     * @param dbSupport           Database-specific functionality.
     * @param schemas             The schemas to clean.
     */
    public DbCleaner(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport, String[] schemas) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.schemas = schemas;
    }

    /**
     * Cleans the schemas of all objects.
     *
     * @throws FlywayException when clean failed.
     */
    public void clean() throws FlywayException {
        for (String schema : schemas) {
            cleanSchema(schema);
        }
    }

    /**
     * Cleans this schema of all objects.
     *
     * @param schema The schema to clean.
     *
     * @throws FlywayException when clean failed.
     */
    private void cleanSchema(String schema) {
        LOG.debug("Starting to drop all database objects in schema '" + schema + "' ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            final SqlScript cleanScript = dbSupport.createCleanScript(schema);
            try {
                transactionTemplate.execute(new TransactionCallback<Void>() {
                    public Void doInTransaction() {
                        cleanScript.execute(jdbcTemplate);
                        return null;
                    }
                });
            } catch (TransactionException e) {
                throw new FlywayException("Clean failed! Schema: " + schema, e);
            }
        } catch (SQLException e) {
            throw new FlywayException("Error while generating clean script", e);
        }
        stopWatch.stop();
        LOG.info(String.format("Cleaned database schema '%s' (execution time %s)",
                schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }
}
