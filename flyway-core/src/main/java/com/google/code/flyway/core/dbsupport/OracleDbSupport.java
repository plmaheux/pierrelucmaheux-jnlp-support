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

package com.google.code.flyway.core.dbsupport;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport implements DbSupport {
    @Override
    public String[] createSchemaMetaDataTableSql(String tableName) {
        String createTableSql = "CREATE TABLE " + tableName + " (" +
                "    version VARCHAR2(20) NOT NULL PRIMARY KEY," +
                "    description VARCHAR2(100)," +
                "    script VARCHAR2(100) NOT NULL UNIQUE," +
                "    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    execution_time INT," +
                "    state VARCHAR2(15) NOT NULL," +
                "    current_version NUMBER(1) NOT NULL" +
                ")";
        String addIndexSql =
                "ALTER TABLE " + tableName + " ADD INDEX " + tableName + "_current_version_index (current_version)";

        return new String[]{createTableSql, addIndexSql};
    }

    @Override
    public String getCurrentSchema(Connection connection) throws SQLException {
        return connection.getMetaData().getUserName();
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "Oracle".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(SimpleJdbcTemplate jdbcTemplate, String schema, String schemaMetaDataTable) throws SQLException {
        int count = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM user_tables WHERE table_name = ?", schemaMetaDataTable.toUpperCase());
        return count > 0;
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }
}
