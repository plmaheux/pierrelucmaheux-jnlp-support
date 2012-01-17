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
package com.googlecode.flyway.core.dbsupport.sqlserver;

import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import net.sourceforge.jtds.jdbc.Driver;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using SQL Server with the Jtds driver.
 */
public class JtdsSQLServerCaseSensitiveMigrationMediumTest extends SQLServerCaseSensitiveMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("sqlserver.user", "sa");
        String password = customProperties.getProperty("sqlserver.password", "flyway");
        String url = customProperties.getProperty("sqlserver.jtds_url", "jdbc:jtds:sqlserver://localhost:1433/flyway_db");

        return new DriverDataSource(new Driver(), url, user, password);
    }
}
