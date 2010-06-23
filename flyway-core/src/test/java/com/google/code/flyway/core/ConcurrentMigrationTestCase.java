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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class ConcurrentMigrationTestCase {
	/**
	 * The number of threads to use in this test.
	 */
	private static final int NUM_THREADS = 10;

	/**
	 * Flag to indicate the concurrent test has failed.
	 */
	private boolean failed;

	/**
	 * The datasource to use for concurrent migration tests.
	 */
	@Resource
	protected DataSource concurrentMigrationDataSource;

	/**
	 * @return The directory containing the migrations for the tests.
	 */
	protected abstract String getBaseDir();

	@Test
	public void migrateConcurrently() throws Exception {
		final Flyway flyway = new Flyway();
		flyway.setDataSource(concurrentMigrationDataSource);
		flyway.setBaseDir(getBaseDir());

		assertFalse(flyway.getMetaDataTable().exists());
		flyway.getMetaDataTable().create();
		assertTrue(flyway.getMetaDataTable().exists());

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					flyway.migrate();
				} catch (Exception e) {
					e.printStackTrace();
					failed = true;
				}
			}
		};

		Thread[] threads = new Thread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(runnable);
		}
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i].start();
		}
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i].join();
		}

		assertFalse(failed);
		assertEquals(2, flyway.getMetaDataTable().migrationCount());
		SchemaVersion schemaVersion = flyway.getMetaDataTable().latestAppliedMigration().getVersion();
		assertEquals("1.1", schemaVersion.getVersion());
		assertEquals("Populate table", schemaVersion.getDescription());
		assertEquals(0, flyway.migrate());
	}
}