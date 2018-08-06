/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.felix.maven.osgicheck.impl.mddocgen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

abstract class AbstractMarkdownMojo extends AbstractMojo {

    @Parameter(defaultValue = "${user.name}", required = true, readonly = true)
    private String currentUser;

    @Parameter
    private String[] exclude;

    protected abstract String getIncludes();

    protected abstract File getSourceDir();

    protected abstract File getTargetDir();

    protected abstract void handle(Collection<File> found) throws MojoExecutionException, MojoFailureException;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!getSourceDir().exists()) {
            throw new MojoFailureException(getSourceDir() + " does not exist, please check MOJO configuration.");
        }

        if (!getTargetDir().exists()) {
            getTargetDir().mkdirs();
        }

        String excludes = null;
        if (exclude != null && exclude.length != 0) {
            excludes = StringUtils.join(exclude, ",");
        } else {
            excludes = null;
        }

        Collection<File> found = null;
        try {
            found = FileUtils.getFiles(getSourceDir(), getIncludes(), excludes);
        } catch (IOException e) {
            throw new MojoExecutionException("An error occurred while scanning directory '"
                                             + getSourceDir()
                                             + "', please check if it exists and the current user '"
                                             + currentUser
                                             + "' has enough permissions for reading");
        }

        if (found == null || found.isEmpty()) {
            getLog().warn("No " + getIncludes() + " file found in " + getSourceDir());
            return;
        }

        handle(found);
    }

    protected static void append(String title, File target, String format, Object...args) throws MojoExecutionException {
        boolean writeTitle = !target.exists();

        PrintWriter out = null;
        try {
            out = newPrintWriter(target);

            if (writeTitle) {
                out.println(title);
                out.println();
            }

            out.printf(format, args);
        } catch (IOException e) {
            throw new MojoExecutionException("An error occurred while appending data to file: " + target, e);
        } finally {
            IOUtil.close(out);
        }
    }

    protected static PrintWriter newPrintWriter(File target) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(target, true)));
    }

    protected static final class ClassName {

        private final String packageName;

        private final String packagePath;

        private final String simpleName;

        public static ClassName get(String qualifiedName) {
            int sep = qualifiedName.lastIndexOf('.');
            String packageName = qualifiedName.substring(0, sep);
            String packagePath = packageName.replace('.', '/');
            String simpleName = qualifiedName.substring(sep + 1);
            return new ClassName(packageName, packagePath, simpleName);
        }

        ClassName(String packageName,
                  String packagePath,
                  String simpleName) {
            this.packageName = packageName;
            this.packagePath = packagePath;
            this.simpleName = simpleName;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getPackagePath() {
            return packagePath;
        }

        public String getSimpleName() {
            return simpleName;
        }

    }

}
