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
package com.googlecode.flyway.core.dbsupport.db2;

import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DB2-specific JdbcTemplate customizations.
 */
public class DB2JdbcTemplate extends JdbcTemplate {
    /**
     * Creates a new DB2JdbcTemplate.
     *
     * @param connection The DB connection to use.
     */
    public DB2JdbcTemplate(Connection connection) {
        super(connection);
    }

    @Override
    protected void setNull(PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setNull(parameterIndex, Types.VARCHAR);
    }
}
