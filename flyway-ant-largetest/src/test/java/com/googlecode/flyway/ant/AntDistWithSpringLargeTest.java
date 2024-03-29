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
package com.googlecode.flyway.ant;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Ant tasks distribution with spring.
 */
@SuppressWarnings({"JavaDoc"})
public class AntDistWithSpringLargeTest extends AntLargeTest {
    @Override
    protected String getInstallDir() {
        return System.getProperty("installDirDistWithSpring", "flyway-ant-largetest/target/install/dist with spring");
    }

    @Test
    public void migrate() throws Exception {
        String stdOut = runAnt(0, "migrate", "-Dflyway.baseDir=largetest/sql");
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
        assertTrue(stdOut.contains("Populate table"));
    }

    /**
     * Execute 1 (SQL), 1.1 (SQL), 1.2 (Spring Jdbc) & 1.3 (Jdbc)
     */
    @Test
    public void sample() throws Exception {
        String stdOut = runAnt(0, "sample");
        assertTrue(stdOut.contains("Successfully applied 4 migrations"));
    }
}
