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

package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.Migration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for BaseJavaMigration.
 */
public class BaseJavaMigrationTest {
    @Test
    public void version() {
        Migration migration = new V1_2_3__Dummy_migration();
        assertEquals("1.2.3", migration.getVersion().getVersion());
        assertEquals("Dummy migration", migration.getVersion().getDescription());
    }
}
