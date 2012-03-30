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
package com.googlecode.flyway.core.util;

/**
 * Utility methods for dealing with classes.
 */
public class ClassUtils {
    /**
     * Prevents instantiation.
     */
    private ClassUtils() {
        // Do nothing
    }

    /**
     * Creates a new instance of this class.
     *
     * @param className The fully qualified name of the class to instantiate.
     * @param <T>       The type of the new instance.
     * @return The new instance.
     * @throws Exception Thrown when the instantiation failed.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T instantiate(String className) throws Exception {
        return (T) Class.forName(className, true, getClassLoader()).newInstance();
    }

    /**
     * @return The classloader to use for loading classes.
     */
    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param className the name of the class to check
     * @return whether the specified class is present
     */
    public static boolean isPresent(String className) {
        try {
            getClassLoader().loadClass(className);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Computes the short name (name without package) of this class.
     *
     * @param aClass The class to analyse.
     * @return The short name.
     */
    public static String getShortName(Class<?> aClass) {
        String name = aClass.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }
}
