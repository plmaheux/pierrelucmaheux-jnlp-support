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
package com.googlecode.flyway.maven.largetest;

import org.junit.Test;
import com.googlecode.flyway.core.util.FileCopyUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Maven Plugin.
 */
@SuppressWarnings({"JavaDoc"})
public class MavenLargeTest {
    /**
     * The installation directory for the test POMs.
     */
    private String installDir =
            new File(System.getProperty("installDir",
                    "flyway-maven-plugin-largetest/target/test-classes")).getAbsolutePath();

    @Test
    public void regular() throws Exception {
        String stdOut = runMaven(0, "regular", "clean", "flyway:init", "flyway:status");
        assertTrue(stdOut.contains("<< Flyway Init >>"));
    }

    @Test
    public void migrate() throws Exception {
        String stdOut = runMaven(0, "regular", "clean", "compile", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    @Test
    public void validate() throws Exception {
        String stdOut = runMaven(1, "validate", "clean", "compile", "flyway:validate");
        assertTrue(stdOut.contains("Validate failed. Found differences between applied migrations and available migrations"));
    }

    @Test
    public void settings() throws Exception {
        String stdOut = runMaven(0, "settings", "clean", "flyway:init", "flyway:status", "-s", installDir + "/settings/settings.xml");
        assertTrue(stdOut.contains("<< Flyway Init >>"));
    }

    /**
     * Tests the use of settings.xml with a default server id.
     */
    @Test
    public void settingsDefault() throws Exception {
        String stdOut = runMaven(0, "settings-default", "clean", "flyway:init", "flyway:status", "-s", installDir + "/settings-default/settings.xml");
        assertTrue(stdOut.contains("<< Flyway Init >>"));
    }

    @Test
    public void locationsElements() throws Exception {
        String stdOut = runMaven(0, "locations-elements", "clean", "compile", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    @Test
    public void locationsProperty() throws Exception {
        String stdOut = runMaven(0, "locations-property", "clean", "compile", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    /**
     * Runs Maven in this directory with these extra arguments.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param dir                The directory below src/test/resources to run maven in.
     * @param extraArgs          The extra arguments (if any) for Maven.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    private String runMaven(int expectedReturnCode, String dir, String... extraArgs) throws Exception {
        String m2Home = System.getenv("M2_HOME");
        String flywayVersion = System.getProperty("flywayVersion", getPomVersion());

        String extension = "";
        if (System.getProperty("os.name").startsWith("Windows")) {
            extension = ".bat";
        }

        List<String> args = new ArrayList<String>();
        args.add(m2Home + "/bin/mvn" + extension);
        args.add("-Dflyway.version=" + flywayVersion);
        //args.add("-X");
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(installDir + "/" + dir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
    }

    /**
     * Retrieves the version embedded in the project pom. Useful for running these tests in IntelliJ.
     *
     * @return The POM version.
     */
    private String getPomVersion() {
        try {
            File pom = new File("pom.xml");
            if (!pom.exists()) {
                return "unknown";
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(pom);
            return xPath.evaluate("/project/version", document);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read POM version", e);
        }
    }
}
