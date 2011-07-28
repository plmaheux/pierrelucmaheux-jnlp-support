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
package com.googlecode.flyway.ant;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.ExceptionUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.springframework.util.StringUtils;

/**
 * Base class for all Flyway Ant tasks.
 */
abstract class AbstractFlywayTask extends Task {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AbstractFlywayTask.class);

    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.<br> default property:
     * ${flyway.driver}
     */
    private String driver;

    /**
     * The jdbc url to use to connect to the database.<br> default property: ${flyway.url}
     */
    private String url;

    /**
     * The user to use to connect to the database.<br> default property: ${flyway.user}<br>
     * The credentials can be specified by user/password or serverId from settings.xml
     */
    private String user;

    /**
     * The password to use to connect to the database. (default: <i>blank</i>)<br> default property: ${flyway.password}
     */
    private String password;

    /**
     * Comma-separated list of the schemas managed by Flyway. The first schema in the list will be the one containing
     * the metadata table. (default: The default schema for the datasource connection)<br> default property:
     * ${flyway.schemas}
     */
    private String schemas;

    /**
     * <p>The name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode) the
     * metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When the
     * <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema of
     * the list. </p> (default: schema_version)<br> default property: ${flyway.table}
     */
    private String table;

    /**
     * @param driver The fully qualified classname of the jdbc driver to use to connect to the database.<br> default property:
     *               ${flyway.driver}
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * @param url The jdbc url to use to connect to the database.<br> default property: ${flyway.url}
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @param user The user to use to connect to the database.<br> default property: ${flyway.user}
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @param password The password to use to connect to the database. (default: <i>blank</i>)<br> default property: ${flyway.password}
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param schemas Comma-separated list of the schemas managed by Flyway. The first schema in the list will be the one containing
     *                the metadata table. (default: The default schema for the datasource connection)<br> default property:
     *                ${flyway.schemas}
     */
    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }

    /**
     * @param table <p>The name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode) the
     *              metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When the
     *              <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema of
     *              the list. </p> (default: schema_version)<br> default property: ${flyway.table}
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Creates the datasource base on the provided parameters.
     *
     * @return The fully configured datasource.
     * @throws Exception Thrown when the datasource could not be created.
     */
    /* private -> for testing */ BasicDataSource createDataSource() throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(usePropertyIfNotSet(driver, "driver"));
        dataSource.setUrl(usePropertyIfNotSet(url, "url"));
        dataSource.setUsername(usePropertyIfNotSet(user, "user"));
        String passwordValue = usePropertyIfNotSet(password, "password");
        if (passwordValue == null) {
            passwordValue = "";
        }
        dataSource.setPassword(passwordValue);
        return dataSource;
    }

    /**
     * Retrieves a value either directly or if not set, from the flyway system property.
     * @param value The value to check.
     * @param flywayProperty The flyway system property. Ex. 'url' for 'flyway.url'
     * @return The value.
     */
    private String usePropertyIfNotSet(String value, String flywayProperty) {
        if (value != null) {
            return value;
        }

        return getProject().getProperty("flyway." + flywayProperty);
    }

    @Override
    public void execute() throws BuildException {
        AntLogAppender.startTaskLog(getProject());
        try {
            Flyway flyway = new Flyway();

            BasicDataSource dataSource = createDataSource();
            try {
                flyway.setDataSource(dataSource);
                if (schemas != null) {
                    flyway.setSchemas(StringUtils.tokenizeToStringArray(usePropertyIfNotSet(schemas, "schemas"), ","));
                }
                if (table != null) {
                    flyway.setTable(usePropertyIfNotSet(table, "table"));
                }

                doExecute(flyway);
            } finally {
                dataSource.close();
            }
        } catch (Exception e) {
            LOG.error(e.toString());

            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error("Caused by " + rootCause.toString());
            }
            throw new BuildException("Flyway Error: " + e.toString(), e);
        } finally {
            AntLogAppender.endTaskLog();
        }
    }

    /**
     * Executes this mojo.
     *
     * @param flyway The flyway instance to operate on.
     * @throws Exception any exception
     */
    protected abstract void doExecute(Flyway flyway) throws Exception;
}
