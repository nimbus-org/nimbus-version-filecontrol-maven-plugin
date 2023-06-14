/*
 * This software is distributed under following license based on modified BSD
 * style license.
 * ----------------------------------------------------------------------
 *
 * Copyright 2003 The Nimbus Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE NIMBUS PROJECT ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE NIMBUS PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of the Nimbus Project.
 */
package jp.ossc.nimbus.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import javax.script.ScriptException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * replace
 *
 * @goal replace
 * @phase generate-sources
 *
 */
public class ReplaceMojo extends AbstractMojo {

    /**
     * @parameter
     */
    private String version;

    /**
     * @parameter
     */
    private String[] checkVersions;

    /**
     * @parameter
     */
    private File fromDir;

    /**
     * @parameter
     */
    private File toDir;

    /**
     * @parameter
     */
    private String[] replaceTargetDirs;

    /**
     * @parameter
     */
    private String fromFileExtention;

    /**
     * @parameter
     */
    private String toFileExtention = "java";

    /**
     * @parameter
     */
    private String checkTargetPrefix;

    /**
     * @parameter
     */
    private String encoding;

    private static final String LINE_SEP = System.getProperty("line.separator");

    private static final String REPLACE_EMPTY_STR = "";

    private static final String REPLACE_START_STR = "/* **Version Difference Comment Start**";

    private static final String REPLACE_END_STR = "**Version Difference Comment End** */";

    /**
     * Execute.
     *
     * @throws MojoExecutionException predictable error
     * @throws MojoFailureException unpredictable error
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            getLog().info("replace start");

            if (version == null || "".equals(version)) {
                getLog().info("version is not found config.");
                version = System.getProperty("nimbus.version.filecontrol.target");
                if (version == null || "".equals(version)) {
                    throw new MojoExecutionException("target version is null or empty.");
                }
            }
            getLog().info("check target version=" + version);

            if (checkVersions == null || checkVersions.length == 0) {
                getLog().error("checkVersions is null or empty.");
                throw new MojoExecutionException("checkVersions is null or empty.");
            }
            getLog().info("checkVersions=" + Arrays.asList(checkVersions));

            if (fromDir == null) {
                getLog().error("fromDir is null.");
                throw new MojoExecutionException("fromDir is null.");
            } else if (!fromDir.exists()) {
                getLog().error("fromDir is not exists.");
                throw new MojoExecutionException("fromDir is not exists.");
            } else if (!fromDir.isDirectory()) {
                getLog().error("fromDir is not directory.");
                throw new MojoExecutionException("fromDir is not directory.");
            }
            getLog().info("source replace fromDir=" + fromDir.getAbsolutePath());

            if (toDir == null) {
                getLog().error("toDir is null.");
                throw new MojoExecutionException("toDir is null.");
            } else if (!toDir.exists()) {
                if (toDir.mkdirs()) {
                    getLog().info("toDir is not exists. toDir created.");
                } else {
                    getLog().error("toDir is not exists. toDir could not create.");
                    throw new MojoExecutionException("toDir is not exists. toDir could not create.");
                }
            } else if (!toDir.isDirectory()) {
                getLog().error("toDir is not directory.");
                throw new MojoExecutionException("toDir is not directory.");
            }
            getLog().info("source replace toDir=" + toDir.getAbsolutePath());

            if (replaceTargetDirs == null) {
                getLog().error("replaceTargetDirs is null.");
                throw new MojoExecutionException("replaceTargetDirs is null.");
            } else if (replaceTargetDirs.length == 0) {
                getLog().error("replaceTargetDirs size is 0.");
                throw new MojoExecutionException("replaceTargetDirs size is 0.");
            }
            for (String replaceTargetDir : replaceTargetDirs) {
                getLog().info("source replace replaceTargetDir=" + replaceTargetDir);
            }

            if (fromFileExtention == null) {
                getLog().error("fromFileExtention is null.");
                throw new MojoExecutionException("fromFileExtention is null.");
            } else if ("".equals(fromFileExtention)) {
                getLog().error("fromFileExtention is empty.");
                throw new MojoExecutionException("fromFileExtention is empty.");
            } else if (fromFileExtention.startsWith(".")) {
                fromFileExtention = fromFileExtention.substring(1);
            }
            getLog().info("source replace fromFileExtention=" + fromFileExtention);

            if (toFileExtention == null) {
                getLog().error("toFileExtention is null.");
                throw new MojoExecutionException("toFileExtention is null.");
            } else if ("".equals(toFileExtention)) {
                getLog().error("toFileExtention is empty.");
                throw new MojoExecutionException("toFileExtention is empty.");
            } else if (toFileExtention.startsWith(".")) {
                toFileExtention = toFileExtention.substring(1);
            }
            getLog().info("source replace toFileExtention=" + toFileExtention);

            if (checkTargetPrefix == null || "".equals(checkTargetPrefix)) {
                getLog().error("checkTargetPrefix is null or empty.");
                throw new MojoExecutionException("checkTargetPrefix is null or empty.");
            }
            getLog().info("checkTargetPrefix=" + checkTargetPrefix);

            if (encoding == null || "".equals(encoding)) {
                getLog().info("source replace encoding is not found config. read or write of the target file is system default encoding.");
            } else {
                getLog().info("source replace encoding=" + encoding);
            }

            FileUtility rFromDir = new FileUtility(fromDir);
            for (String replaceTargetDirName : replaceTargetDirs) {
                File[] targetFiles = rFromDir.listAllTreeFiles(replaceTargetDirName + "/.*\\\\." + fromFileExtention);
                if (targetFiles != null && targetFiles.length > 0) {
                    for (File targetFile : targetFiles) {
                        String tmpFileName = targetFile.getAbsolutePath().substring(fromDir.getAbsolutePath().length());
                        File toFile = new File(
                                toDir.getAbsolutePath() + tmpFileName.substring(0, tmpFileName.lastIndexOf(fromFileExtention)) + toFileExtention);
                        FileUtility.dataCopy(targetFile, toFile);
                        replace(toFile);
                        getLog().info("File copy from=" + targetFile.getAbsolutePath() + " to=" + toFile.getAbsolutePath());
                    }
                }
            }
        } catch (ScriptException ex) {
            getLog().error(ex.getMessage());
            throw new MojoExecutionException("replace failed.", ex);
        } catch (Throwable th) {
            getLog().error(th.getMessage());
            throw new MojoFailureException("replace failed");
        }
    }

    private void replace(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        Reader reader = null;
        BufferedReader br = null;
        Writer writer = null;
        PrintWriter pw = null;
        try {
            reader = encoding == null ? new InputStreamReader(new FileInputStream(file)) : new InputStreamReader(new FileInputStream(file), encoding);
            br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null) {
                for (String checkVersion : checkVersions) {
                    int compareTo = Integer.parseInt(version) - Integer.parseInt(checkVersion);
                    if (compareTo == 0) {
                        line = line.replaceAll("@START=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@START>" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END>" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START>=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END>=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@START<" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END<" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START<=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END<=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                    } else if (compareTo > 0) {
                        line = line.replaceAll("@START=" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END=" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START>" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END>" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@START>=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END>=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@START<" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END<" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START<=" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END<=" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                    } else {
                        line = line.replaceAll("@START=" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END=" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START>" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END>" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START>=" + checkTargetPrefix + checkVersion + "@", REPLACE_START_STR);
                        line = line.replaceAll("@END>=" + checkTargetPrefix + checkVersion + "@", REPLACE_END_STR);
                        line = line.replaceAll("@START<" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END<" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@START<=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                        line = line.replaceAll("@END<=" + checkTargetPrefix + checkVersion + "@", REPLACE_EMPTY_STR);
                    }
                }
                sb.append(line);
                sb.append(LINE_SEP);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
        try {
            writer = encoding == null ? new OutputStreamWriter(new FileOutputStream(file)) : new OutputStreamWriter(new FileOutputStream(file), encoding);
            pw = new PrintWriter(new BufferedWriter(writer));
            pw.print(sb.toString());
            pw.flush();
        } finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (Exception e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
    }
}