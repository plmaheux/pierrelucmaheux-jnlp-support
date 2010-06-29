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

package com.google.code.flyway.core.dbsupport.hsql;

import org.springframework.test.context.ContextConfiguration;

import com.google.code.flyway.core.MigrationTestCase;

/**
 * Test to demonstrate the migration functionality using Hsql.
 */
@ContextConfiguration(locations = { "classpath:migration/hsql/hsql-context.xml" })
public class HsqlMigrationMediumTest extends MigrationTestCase {
	@Override
	protected String getBaseDir() {
		return "migration/sql";
	}
}