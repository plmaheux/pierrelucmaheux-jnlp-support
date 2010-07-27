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

package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:migration/oracle/oracle-context.xml"})
public class OracleMigrationMediumTest {
    @Autowired
    private DataSource dataSource;

    private Flyway flyway;

    @Before
    public void setUp() {
        flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.clean();
    }

    /**
     * Tests migrations containing placeholders.
     */
    @Test
    public void migrationsWithPlaceholders() throws Exception {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", "test_user");
        flyway.setPlaceholders(placeholders);
        flyway.setBaseDir("migration/oracle/sql/placeholders");

        flyway.migrate();
        SchemaVersion schemaVersion = flyway.getMetaDataTable().latestAppliedMigration().getVersion();
        assertEquals("1.1", schemaVersion.getVersion());
        assertEquals("Populate table", schemaVersion.getDescription());

        SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        assertEquals("Mr. T triggered", jdbcTemplate.queryForObject("select name from test_user", String.class));

        flyway.clean();

        int countUserObjects = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");
        assertEquals(0, countUserObjects);

        final List<Migration> migrationList = flyway.getMetaDataTable().allAppliedMigrations();
        for (Migration migration : migrationList) {
            Assert.assertNotNull(migration.getScriptName() + " has no checksum", migration.getChecksum());
        }

    }

    /**
     * Tests clean for Oracle Spatial Extensions.
     */
    @Test
    public void cleanSpatialExtensions() throws Exception {
        flyway.setBaseDir("migration/oracle/sql/spatial");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }
}
