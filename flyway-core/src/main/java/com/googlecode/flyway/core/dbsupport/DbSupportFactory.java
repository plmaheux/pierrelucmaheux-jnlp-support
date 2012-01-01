/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport;

import com.googlecode.flyway.core.dbsupport.db2.DB2DbSupport;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.dbsupport.hsql.HsqlDbSupport;
import com.googlecode.flyway.core.dbsupport.mysql.MySQLDbSupport;
import com.googlecode.flyway.core.dbsupport.oracle.OracleDbSupport;
import com.googlecode.flyway.core.dbsupport.postgresql.PostgreSQLDbSupport;
import com.googlecode.flyway.core.dbsupport.sqlserver.SQLServerDbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Factory for obtaining the correct DbSupport instance for the current connection.
 */
public class DbSupportFactory {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbSupportFactory.class);

    /**
     * Prevent instantiation.
     */
    private DbSupportFactory() {
        //Do nothing
    }

    /**
     * Initializes the appropriate DbSupport class for the database product used by the data source.
     *
     * @param connection The Jdbc connection to use to query the database.
     * @return The appropriate DbSupport class.
     */
    public static DbSupport createDbSupport(Connection connection) {
        String databaseProductName = getDatabaseProductName(connection);
        if (databaseProductName == null) {
            throw new FlywayException("Unable to determine database. Product name is null.");
        }

        LOG.debug("Database: " + databaseProductName);

        if ("H2".equals(databaseProductName)) {
            return new H2DbSupport(connection);
        }
        if ("HSQL Database Engine".equals(databaseProductName)
                || "Google SQL Service/HSQL Database Engine".equals(databaseProductName)) {
            return new HsqlDbSupport(connection);
        }
        if ("Microsoft SQL Server".equals(databaseProductName)) {
            return new SQLServerDbSupport(connection);
        }
        if ("MySQL".equals(databaseProductName) ||
                "Google SQL Service/MySQL".equals(databaseProductName)) {
            return new MySQLDbSupport(connection);
        }
        if ("Oracle".equals(databaseProductName)) {
            return new OracleDbSupport(connection);
        }
        if ("PostgreSQL".equals(databaseProductName)) {
            return new PostgreSQLDbSupport(connection);
        }
        if (databaseProductName.startsWith("DB2")) {
            // DB2 returns also OS it's running on
            // e.g. DB2/NT
            return new DB2DbSupport(connection);
        }

        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the name of the database product.
     *
     * @param connection The connection to use to query the database.
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private static String getDatabaseProductName(Connection connection) {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            if (databaseMetaData == null) {
                throw new FlywayException("Unable to read database metadata while it is null!");
            }
            return databaseMetaData.getDatabaseProductName();
        } catch (SQLException e) {
            throw new FlywayException("Error while determining database product name", e);
        }
    }

}
