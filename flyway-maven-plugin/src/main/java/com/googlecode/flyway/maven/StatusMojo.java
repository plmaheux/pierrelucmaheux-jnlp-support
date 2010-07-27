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

package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.migration.Migration;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Maven goal that shows the status (current version) of the database.
 *
 * @goal status
 * @requiresDependencyResolution compile
 * @configurator include-project-dependencies
 * @since 0.8
 */
@SuppressWarnings({"UnusedDeclaration"})
public class StatusMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute() throws MojoExecutionException {
        Flyway flyway = new Flyway();
        flyway.setDataSource(getDataSource());
        Migration migration = flyway.status();

        List<Migration> migrations = new ArrayList<Migration>();
        if (migration != null) {
            migrations.add(migration);
        }

        MigrationDumper.dumpMigrations(migrations);
    }
}