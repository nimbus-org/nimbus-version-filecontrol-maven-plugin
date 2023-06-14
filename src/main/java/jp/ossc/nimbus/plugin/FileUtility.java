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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileUtility extends File implements Serializable {
    
    private static final long serialVersionUID = 4549749658684567046L;
    
    /**
     * 検索種別：ファイルのみ検索する。<p>
     * デフォルト値。<br>
     */
    public static final int SEARCH_TYPE_FILE = 0;
    
    /**
     * 検索種別：ディレクトリのみ検索する。<p>
     */
    public static final int SEARCH_TYPE_DIR = 1;
    
    /**
     * 検索種別：ファイルとディレクトリの両方を検索する。<p>
     */
    public static final int SEARCH_TYPE_ALL = 2;
    
    private static final String REGEX_ESCAPE_ESCAPE = Character.toString((char) 0x00);
    
    /**
     * 指定されたパスのファイルインスタンスを生成する。<p>
     *
     * @param pathname パス
     */
    public FileUtility(String pathname) {
        super(pathname);
    }
    
    /**
     * 指定されたファイルのファイルインスタンスを生成する。<p>
     *
     * @param file ファイル
     */
    public FileUtility(File file) {
        super(file.getPath());
    }
    
    /**
     * このファイルが示すディレクトリ配下のサブディレクトリを含めた全てのファイルを、指定されたフィルタでフィルタリングした結果を取得する。<p>
     * このファイルがディレクトリを示さない場合には、nullを返す。
     *
     * @param filter フィルタ
     * @return ファイル配列
     */
    public File[] listAllTreeFiles(FilenameFilter filter) {
        return listAllTreeFiles(filter, SEARCH_TYPE_FILE);
    }
    
    /**
     * このファイルが示すディレクトリ配下のサブディレクトリを含めた全てのファイルを、指定されたフィルタでフィルタリングした結果を取得する。<p>
     * このファイルがディレクトリを示さない場合には、nullを返す。
     *
     * @param filter フィルタ
     * @param searchType 検索種別
     * @return ファイル配列
     * @see #SEARCH_TYPE_FILE
     * @see #SEARCH_TYPE_DIR
     * @see #SEARCH_TYPE_ALL
     */
    public File[] listAllTreeFiles(FilenameFilter filter, int searchType) {
        if (!isDirectory()) {
            return null;
        }
        final List dirList = new ArrayList();
        dirList.add(this);
        final FilenameFilter[] filters = new FilenameFilter[1];
        filters[0] = filter;
        final List fileList = filteringRecurciveSerach(dirList, filters, searchType);
        final File[] ret = new File[fileList.size()];
        for (int cnt = 0; cnt < ret.length; cnt++) {
            ret[cnt] = (File) fileList.get(cnt);
        }
        return ret;
    }
    
    /**
     * 指定されたディレクトリリストの各ディレクトリ配下のサブディレクトリを含めた全てのファイルを、指定されたフィルタでフィルタリングした結果を取得する。<p>
     *
     * @param dirList ディレクトリのリスト
     * @param filter フィルタ配列
     * @param searchType 検索種別
     * @return ファイルパス配列
     * @see #SEARCH_TYPE_FILE
     * @see #SEARCH_TYPE_DIR
     * @see #SEARCH_TYPE_ALL
     */
    protected List filteringRecurciveSerach(
        List dirList,
        FilenameFilter[] filter,
        int searchType
    ){
        final List fileList = new ArrayList();
        while (dirList.size() > 0) {
            File dir = (File) dirList.remove(0);
            File[] list = dir.listFiles();
            for (int cnt = 0; cnt < list.length; cnt++) {
                File tmp = list[cnt];
                final boolean isDir = tmp.isDirectory();
                final boolean isFile = tmp.isFile();
                if (!isDir && !isFile) {
                    continue;
                }
                if (isDir) {
                    dirList.add(tmp);
                }
                switch (searchType) {
                case SEARCH_TYPE_FILE:
                    if (isDir) {
                        continue;
                    }
                    break;
                case SEARCH_TYPE_DIR:
                    if (isFile) {
                        continue;
                    }
                    break;
                case SEARCH_TYPE_ALL:
                default:
                }
                boolean check = true;
                for (int fcnt = 0; fcnt < filter.length; fcnt++) {
                    if (!filter[fcnt].accept(tmp.getParentFile(), tmp.getName())) {
                        check = false;
                        break;
                    }
                }
                if (check) {
                    fileList.add(tmp);
                }
            }
        }
        return fileList;
    }
    
    /**
     * このディレクトリ配下で、指定された正規表現に一致するファイル配列を取得する。<p>
     *
     * @param regexPath パスの正規表現
     * @return ファイル配列
     * @see #listAllTreeFiles(String, int)
     * @see #SEARCH_TYPE_FILE
     * @see #SEARCH_TYPE_DIR
     * @see #SEARCH_TYPE_ALL
     */
    public File[] listAllTreeFiles(String regexPath) {
        return listAllTreeFiles(regexPath, SEARCH_TYPE_FILE);
    }
    
    /**
     * このディレクトリ配下で、指定された正規表現に一致するファイル配列を取得する。<p>
     * パスの正規表現には、通常の正規表現に加えて"**"という指定が可能である。<br>
     * "**"と指定された場合、途中の全てのディレクトリ構造を含む事を示す。<br>
     * 制限事項として、正規表現のエスケープ文字である"\"は、Windows OSのパスセパレータにもなっているため、正規表現として"\"を指定したい場合は、"\\"と指定する事。<br>
     *
     * @param regexPath パスの正規表現
     * @param searchType 検索種別
     * @return ファイル配列
     * @see #SEARCH_TYPE_FILE
     * @see #SEARCH_TYPE_DIR
     * @see #SEARCH_TYPE_ALL
     */
    public File[] listAllTreeFiles(String regexPath, int searchType) {
        regexPath = regexPath.replaceAll("\\\\\\\\", REGEX_ESCAPE_ESCAPE);
        final List result = filteringRecurciveSerachByRegEx(
            getPath().length() == 0
                 ? new File(regexPath) : new File(this, regexPath),
            searchType,
            new ArrayList()
        );
        return (File[]) result.toArray(new File[result.size()]);
    }
    
    private List filteringRecurciveSerachByRegEx(
        File file,
        int searchType,
        List result
    ){
        if (file.exists()) {
            switch (searchType) {
            case SEARCH_TYPE_FILE:
                if (file.isDirectory()) {
                    return result;
                }
                break;
            case SEARCH_TYPE_DIR:
                if (file.isFile()) {
                    return result;
                }
                break;
            case SEARCH_TYPE_ALL:
            default:
            }
            result.add(file);
            return result;
        }
        List pathList = new ArrayList();
        File f = file;
        String name = null;
        do {
            name = f.getName();
            f = f.getParentFile();
            pathList.add(0, name.length() == 0 ? "/" : name);
        } while (f != null);
        
        File allDir = null;
        for (int i = 0, imax = pathList.size(); i < imax; i++) {
            name = (String) pathList.get(i);
            f = new File(f, name);
            if ("**".equals(name)) {
                if (allDir == null) {
                    allDir = f.getParentFile();
                    if (allDir == null) {
                        allDir = new File(".");
                    }
                }
                if (i == imax - 1) {
                    name = ".*";
                } else {
                    continue;
                }
            }
            if (allDir != null) {
                FileUtility rootDir
                     = new FileUtility(allDir.getPath());
                if (i == imax - 1) {
                    File[] files = rootDir.listAllTreeFiles(
                        new FileFilter(
                            name.replaceAll(REGEX_ESCAPE_ESCAPE, "\\\\")
                        ),
                        searchType
                    );
                    if (files != null) {
                        for (int j = 0; j < files.length; j++) {
                            result.add(files[j]);
                        }
                    }
                } else {
                    File[] dirs = rootDir.listAllTreeFiles(
                        new FileFilter(
                            name.replaceAll(REGEX_ESCAPE_ESCAPE, "\\\\")
                        ),
                        FileUtility.SEARCH_TYPE_DIR
                    );
                    if (dirs != null) {
                        final StringBuilder buf = new StringBuilder();
                        for (int j = i + 1; j < imax; j++) {
                            buf.append((String) pathList.get(j));
                            if (j != imax - 1) {
                                buf.append('/');
                            }
                        }
                        final String path = buf.toString();
                        for (int j = 0; j < dirs.length; j++) {
                            dirs[j] = new File(dirs[j], path);
                            result = filteringRecurciveSerachByRegEx(
                                dirs[j],
                                searchType,
                                result
                            );
                        }
                    }
                    break;
                }
            } else if (!f.exists()) {
                File rootDir = f.getParentFile();
                if (rootDir == null) {
                    rootDir = new File(".");
                }
                if (i == imax - 1) {
                    File[] files = rootDir.listFiles(
                        new FileFilter(
                            name.replaceAll(REGEX_ESCAPE_ESCAPE, "\\\\")
                        )
                    );
                    if (files != null) {
                        for (int j = 0; j < files.length; j++) {
                            boolean isDir = files[j].isDirectory();
                            boolean isFile = files[j].isFile();
                            if (!isDir && !isFile) {
                                continue;
                            }
                            switch (searchType) {
                            case SEARCH_TYPE_FILE:
                                if (isDir) {
                                    continue;
                                }
                                break;
                            case SEARCH_TYPE_DIR:
                                if (isFile) {
                                    continue;
                                }
                                break;
                            case SEARCH_TYPE_ALL:
                            default:
                            }
                            result.add(files[j]);
                        }
                    }
                } else {
                    File[] dirs = rootDir.listFiles(
                        new FileFilter(
                            name.replaceAll(REGEX_ESCAPE_ESCAPE, "\\\\")
                        )
                    );
                    if (dirs != null) {
                        final StringBuilder buf = new StringBuilder();
                        for (int j = i + 1; j < imax; j++) {
                            buf.append((String) pathList.get(j));
                            if (j != imax - 1) {
                                buf.append('/');
                            }
                        }
                        final String path = buf.toString();
                        for (int j = 0; j < dirs.length; j++) {
                            if (!dirs[j].isDirectory()) {
                                continue;
                            }
                            dirs[j] = new File(dirs[j], path);
                            result = filteringRecurciveSerachByRegEx(
                                dirs[j],
                                searchType,
                                result
                            );
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    public static void dataCopy(File fromFile, File toFile) throws IOException {
        if(!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        FileInputStream fis = new FileInputStream(fromFile);
        FileOutputStream fos = new FileOutputStream(toFile);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } finally {
            if (fis != null)
                fis.close();
            if (fos != null)
                fos.close();
        }
    }

    /**
     * 指定されたファイル以下を再帰的に削除する。<p>
     *
     * @param file 削除するファイル
     * @param containsOwn 引数のfile自身も消す場合true
     * @return 全て削除できた場合true
     */
    public static boolean deleteAllTree(File file, boolean containsOwn) {
        if (!file.exists()) {
            return true;
        }
        boolean result = true;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                result &= deleteAllTree(files[i], true);
            }
            if(containsOwn){
                result &= file.delete();
            }
        } else if (file.isFile()) {
            if(containsOwn){
                result &= file.delete();
            }
        }
        return result;
    }
}
