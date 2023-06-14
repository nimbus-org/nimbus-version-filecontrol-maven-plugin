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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * copy
 *
 * @goal copy
 * @phase generate-sources
 *
 */
public class CopyMojo extends AbstractMojo {

    /**
     * @parameter
     */
    private String version;

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
    private String fromFileExtention;

    /**
     * @parameter
     */
    private String toFileExtention = "java";

    /**
     * @parameter
     */
    private String encoding;

    /**
     * @parameter
     */
    private String checkTarget;

    /**
     * Execute.
     *
     * @throws MojoExecutionException predictable error
     * @throws MojoFailureException unpredictable error
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            getLog().info("copy start");

            if (version == null || "".equals(version)) {
                getLog().info("version is not found config.");
                version = System.getProperty("nimbus.version.filecontrol.target");
                if (version == null || "".equals(version)) {
                    throw new MojoExecutionException("target version is null or empty.");
                }
            }
            getLog().info("check target version=" + version);

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
            getLog().info("source copy fromDir=" + fromDir.getAbsolutePath());

            if (toDir == null) {
                getLog().error("toDir is null.");
                throw new MojoExecutionException("toDir is null.");
            } else if (!toDir.exists()) {
                if(toDir.mkdirs()) {
                    getLog().info("toDir is not exists. toDir created.");
                } else {
                    getLog().error("toDir is not exists. toDir could not create.");
                    throw new MojoExecutionException("toDir is not exists. toDir could not create.");
                }
            } else if (!toDir.isDirectory()) {
                getLog().error("toDir is not directory.");
                throw new MojoExecutionException("toDir is not directory.");
            }
            getLog().info("source copy toDir=" + toDir.getAbsolutePath());

            if (fromFileExtention == null) {
                getLog().error("fromFileExtention is null.");
                throw new MojoExecutionException("fromFileExtention is null.");
            } else if ("".equals(fromFileExtention)) {
                getLog().error("fromFileExtention is empty.");
                throw new MojoExecutionException("fromFileExtention is empty.");
            } else if(fromFileExtention.startsWith(".")) {
                fromFileExtention = fromFileExtention.substring(1);
            }
            getLog().info("source copy fromFileExtention=" + fromFileExtention);

            if (toFileExtention == null) {
                getLog().error("toFileExtention is null.");
                throw new MojoExecutionException("toFileExtention is null.");
            } else if ("".equals(toFileExtention)) {
                getLog().error("toFileExtention is empty.");
                throw new MojoExecutionException("toFileExtention is empty.");
            } else if(toFileExtention.startsWith(".")) {
                toFileExtention = toFileExtention.substring(1);
            }
            getLog().info("source copy toFileExtention=" + toFileExtention);

            if (checkTarget == null || "".equals(checkTarget)) {
                getLog().error("checkTarget is null or empty.");
                throw new MojoExecutionException("checkTarget is null or empty.");
            }
            getLog().info("checkTarget=" + checkTarget);

            if(encoding == null || "".equals(encoding)) {
                getLog().info("source copy encoding is not found config. read or write of the target file is system default encoding.");
            } else {
                getLog().info("source copy encoding=" + encoding);
            }

            FileUtility rFromDir = new FileUtility(fromDir);
            File[] copyTargetFiles = rFromDir.listAllTreeFiles("**/.*." + fromFileExtention);
            for(File copyTargetFile : copyTargetFiles) {
                checkAndCopy(copyTargetFile);
            }
        } catch (Throwable th) {
            getLog().error(th.getMessage());
            throw new MojoFailureException("copy failed");
        }
    }

    private void checkAndCopy(File file) throws Exception {

        Reader reader = null;
        BufferedReader br = null;
        try {
            boolean isCopyTarget = true;
            boolean isVersionCheckTargetExists = false;
            reader = encoding == null ? new InputStreamReader(new FileInputStream(file)) : new InputStreamReader(new FileInputStream(file), encoding);
            br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null) {
                if(line.indexOf(checkTarget) != -1) {
                    isVersionCheckTargetExists = true;
                    String[] conditions = line.split(checkTarget);
                    if(conditions.length != 1 && conditions.length != 2) {
                        throw new Exception("Version condition is invalid. condition=" + line);
                    }
                    int low_ver = Integer.MIN_VALUE;
                    String low_con = null;
                    int high_ver = Integer.MAX_VALUE;
                    String high_con = null;
                    int intVersion = Integer.parseInt(version);
                    StringBuilder nsb = new StringBuilder();
                    StringBuilder csb = new StringBuilder();
                    char[] chars = conditions[0].toCharArray();
                    for(char c : chars) {
                        switch(c){
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                nsb.append(c);
                                break;
                            case '<':
                            case '=':
                                csb.append(c);
                                break;
                        }
                    }
                    if(nsb.length() != 0 && csb.length() != 0) {
                        low_ver = Integer.parseInt(nsb.toString());
                        low_con = csb.toString();
                    }
                    if(conditions.length == 2) {
                        nsb = new StringBuilder();
                        csb = new StringBuilder();
                        chars = conditions[1].toCharArray();
                        for(char c : chars) {
                            switch(c){
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    nsb.append(c);
                                    break;
                                case '<':
                                case '=':
                                    csb.append(c);
                                    break;
                            }
                        }
                        if(nsb.length() != 0 && csb.length() != 0) {
                            high_ver = Integer.parseInt(nsb.toString());
                            high_con = csb.toString();
                        }
                    }
                    if(low_con != null && high_con != null) {
                        if("<=".equals(low_con)) {
                            isCopyTarget = (low_ver <= intVersion) && isCopyTarget;
                        } else if("<".equals(low_con)) {
                            isCopyTarget = (low_ver < intVersion) && isCopyTarget;
                        } else if("=".equals(low_con)) {
                            isCopyTarget = (low_ver == intVersion) && isCopyTarget;
                        }
                        if("<=".equals(high_con)) {
                            isCopyTarget = (intVersion <= high_ver) && isCopyTarget;
                        } else if("<".equals(high_con)) {
                            isCopyTarget = (intVersion < high_ver) && isCopyTarget;
                        } else if("=".equals(high_con)) {
                            isCopyTarget = (intVersion == high_ver) && isCopyTarget;
                        }
                    } else if(low_con != null) {
                        if("<=".equals(low_con)) {
                            isCopyTarget = (low_ver <= intVersion) && isCopyTarget;
                        } else if("<".equals(low_con)) {
                            isCopyTarget = (low_ver < intVersion) && isCopyTarget;
                        } else if("=".equals(low_con)) {
                            isCopyTarget = (low_ver == intVersion) && isCopyTarget;
                        }
                    } else if(high_con != null) {
                        if("<=".equals(high_con)) {
                            isCopyTarget = (intVersion <= high_ver) && isCopyTarget;
                        } else if("<".equals(high_con)) {
                            isCopyTarget = (intVersion < high_ver) && isCopyTarget;
                        } else if("=".equals(high_con)) {
                            isCopyTarget = (intVersion == high_ver) && isCopyTarget;
                        }
                    } else {
                        throw new Exception("Version condition is invalid. condition=" + line);
                    }
                }

            }
            if(isVersionCheckTargetExists && isCopyTarget) {
                String tmpFileName = file.getAbsolutePath().substring(fromDir.getAbsolutePath().length());
                File toFile = new File(toDir.getAbsolutePath() + tmpFileName.substring(0, tmpFileName.lastIndexOf(".") + 1) + toFileExtention);
                FileUtility.dataCopy(file, toFile);
                getLog().info("File copy from=" + file.getAbsolutePath() + " to=" + toFile.getAbsolutePath());
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


    }

}