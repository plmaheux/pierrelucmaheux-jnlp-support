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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;

/**
 * Maven goal to validate the applied migrations in the database against the available classpath migrations in order to
 * detect accidental migration changes.
 *
 * @goal validate
 * @since 0.9
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class ValidateMojo extends AbstractMigrationLoadingMojo {
    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        super.doExecute(flyway);

        flyway.validate();
    }
}