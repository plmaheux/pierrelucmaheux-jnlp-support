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

package com.google.code.flyway.core;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * A migration of a single version of the schema.
 *
 * @author Axel Fontaine
 */
public interface Migration {
    /**
     * @return The schema version after the migration is complete.
     */
    SchemaVersion getVersion();

    /**
     * @return The state of this migration.
     */
    MigrationState getState();

    /**
     * @return The time (in ms) it took to execute.
     */
    long getExecutionTime();

    /**
     * @return The script name for the migration history.
     */
    String getScriptName();

    /**
     * Performs the migration. The migration state and the execution time are updated accordingly.
     *
     * @param jdbcTemplate To execute the migration statements.
     */
    void migrate(SimpleJdbcTemplate jdbcTemplate);
}
