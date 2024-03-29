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
package com.googlecode.flyway.core.migration.spring;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.util.ClassUtils;
import com.googlecode.flyway.core.util.scanner.ClassPathScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration resolver for Spring Jdbc migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 */
public class SpringJdbcMigrationResolver implements MigrationResolver {
    /**
     * The base package on the classpath where to migrations are located.
     */
    private final String basePackage;

    /**
     * Creates a new instance.
     *
     * @param basePackage The base package on the classpath where to migrations are located.
     */
    public SpringJdbcMigrationResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<Migration> resolveMigrations() {
        List<Migration> migrations = new ArrayList<Migration>();

        try {
            Class<?>[] classes = new ClassPathScanner().scanForClasses(basePackage, SpringJdbcMigration.class);
            for (Class<?> clazz : classes) {
                SpringJdbcMigration springJdbcMigration = (SpringJdbcMigration) ClassUtils.instantiate(clazz.getName());
                migrations.add(new SpringJdbcMigrationExecutor(springJdbcMigration));
            }
        } catch (Exception e) {
            throw new FlywayException("Unable to resolve Spring Jdbc Java migrations in location: " + basePackage, e);
        }

        return migrations;
    }
}
