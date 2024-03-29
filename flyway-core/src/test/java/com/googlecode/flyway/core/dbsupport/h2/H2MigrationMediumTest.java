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
package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.h2.Driver;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using H2.
 */
public class H2MigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(new Driver(), "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "",
                "CREATE SCHEMA IF NOT EXISTS flyway_1",
                "CREATE SCHEMA IF NOT EXISTS flyway_2",
                "CREATE SCHEMA IF NOT EXISTS flyway_3");
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void dollarQuotedString() throws Exception {
        flyway.setLocations("migration/dbsupport/h2/sql/dollar_quoted_string");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1.1", schemaVersion.toString());
        assertEquals("Populate table", flyway.status().getDescription());

        assertEquals("'Mr. Semicolon+Linebreak;\nanother line'",
                jdbcTemplate.queryForString("select name from test_user where name like '%line'''"));
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/h2/sql/sequence");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Sequence", flyway.status().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("select nextval('the_beast')"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void domain() throws Exception {
        flyway.setLocations("migration/dbsupport/h2/sql/domain");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Domain", flyway.status().getDescription());

        assertEquals("axel@spam.la", jdbcTemplate.queryForString("select address from test_user where name = 'Axel'"));

        flyway.clean();
        flyway.migrate();
    }
}