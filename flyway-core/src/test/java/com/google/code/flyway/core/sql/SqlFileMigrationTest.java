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

package com.google.code.flyway.core.sql;

import static org.junit.Assert.assertEquals;

import com.google.code.flyway.core.sql.SqlMigration;
import org.junit.Test;

/**
 * Testcase for SqlMigration.
 */
public class SqlFileMigrationTest {
    /**
     * Test for extractVersionStringFromFileName.
     */
    @Test
    public void extractVersionStringFromFileName() {
        assertEquals("V8_0", SqlMigration.extractVersionStringFromFileName("sql/V8_0.sql"));
        assertEquals("V9_0__CommentAboutContents", SqlMigration.extractVersionStringFromFileName("sql/V9_0__CommentAboutContents.sql"));
    }
}
