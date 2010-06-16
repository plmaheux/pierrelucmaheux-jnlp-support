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

package com.google.code.flyway.maven;

import com.google.code.flyway.core.Flyway;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Map;

/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 *
 * @goal migrate
 * @requiresDependencyResolution compile
 * @configurator include-project-dependencies
 */
public class MigrateMojo extends AbstractMojo {
    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.
     *
     * @parameter
     * @required
     */
    private String driver;

    /**
     * The jdbc url to use to connect to the database.
     *
     * @parameter
     * @required
     */
    private String url;

    /**
     * The user to use to connect to the database.
     *
     * @parameter
     * @required
     */
    private String user;

    /**
     * The password to use to connect to the database.
     *
     * @parameter
     */
    private String password = "";

    /**
     * The base package where the Java migrations are located. (default: db.migration)
     *
     * @parameter
     */
    private String basePackage;

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: sql/location)
     *
     * @parameter
     */
    private String baseDir;

    /**
     * The name of the schema metadata table that will be used by flyway. (default: schema_version)
     *
     * @parameter
     */
    private String schemaMetaDataTable;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     *
     * @parameter
     */
    private Map<String, String> placeholders;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            Driver driverClazz = (Driver) Class.forName(driver).newInstance();
            DataSource dataSource =
                    new SimpleDriverDataSource(driverClazz, url, user, password);

            Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource);
            if (basePackage != null) {
                flyway.setBasePackage(basePackage);
            }
            if (baseDir != null) {
                flyway.setBaseDir(baseDir);
            }
            if (schemaMetaDataTable != null) {
                flyway.setSchemaMetaDataTable(schemaMetaDataTable);
            }
            if (placeholders != null) {
                flyway.setPlaceholders(placeholders);
            }

            flyway.migrate();
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to find driver class: " + driver, e);
        } catch (InstantiationException e) {
            throw new MojoExecutionException("Unable to instantiate driver class: " + driver, e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Unable to access driver class: " + driver, e);
        } catch (Exception e) {
            throw new MojoExecutionException("Error migrating database: " + e.getMessage(), e);
        }
    }
}
