/*
 *    Copyright 2014 - 2016 Yannick Watier
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.filetypefinder.core;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Yannick on 2/16/2016.
 */
public final class FilterClassLoader extends ClassLoader {
    private static final Map<String, Filter> FILTER_ROOT;
    private static final Map<String, Filter> FILTER_CHILDREN;
    private static final FilterClassLoader OUR_INSTANCE;
    private static final FileFilter CLASS_FILE_FILTER;

    static {
        FILTER_CHILDREN = Collections.synchronizedMap(new HashMap<String, Filter>());
        FILTER_ROOT = Collections.synchronizedMap(new HashMap<String, Filter>());
        CLASS_FILE_FILTER = new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.getParent().endsWith("filters") && pathname.getName().endsWith(".class")) || pathname.isDirectory();
            }
        };

        OUR_INSTANCE = new FilterClassLoader();
    }

    private FilterClassLoader() {

        Map<String, byte[]> rawClassMap = new HashMap<String, byte[]>();

        CodeSource src = FilterClassLoader.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL location = src.getLocation();
            try {
                ZipInputStream zip = new ZipInputStream(location.openStream());

                ZipEntry nextEntry = zip.getNextEntry();

                if (nextEntry != null) { //Executed from a jar
                    while (nextEntry != null) {
                        if (!nextEntry.isDirectory()) {
                            addFiltersToMap(nextEntry.getName(), zip, rawClassMap);
                        }

                        nextEntry = zip.getNextEntry();
                    }
                } else { //Not a jar
                    try {
                        File dir = new File(location.toURI());

                        List<File> fileByType = findFileByType(dir, CLASS_FILE_FILTER);
                        if (fileByType != null) {
                            for (File file : fileByType) {
                                addFiltersToMap(file.getPath(), new FileInputStream(file), rawClassMap);
                            }
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!rawClassMap.isEmpty()) {
            for (Map.Entry<String, byte[]> entry : rawClassMap.entrySet()) {
                Pattern pattern = Pattern.compile("(?<=\\/)\\w*\\.\\w*$"); //Extract the filename
                Matcher matcher = pattern.matcher(entry.getKey());

                byte[] data = entry.getValue();

                if (matcher.find()) {

                    String filename = matcher.group(0).split("\\.")[0];
                    try {
                        Class<?> clazz = defineClass(null, data, 0, data.length);
                        Filter value = (Filter) clazz.newInstance();

                        FilterProperties annotation = clazz.getAnnotation(FilterProperties.class);

                        if (annotation != null) {
                            String parent = annotation.parent();

                            if (!"".equals(parent)) {
                                FILTER_CHILDREN.put(filename, value);
                            } else {
                                FILTER_ROOT.put(filename, value);
                            }
                        } else {
                            FILTER_ROOT.put(filename, value);
                        }
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    //Check if the parent exist, if not, put the element into the root map
                    for (Filter filterChild : new ArrayList<Filter>(FILTER_CHILDREN.values())) {
                        FilterProperties annotation = filterChild.getClass().getAnnotation(FilterProperties.class);

                        String parent = annotation.parent();

                        if (!FILTER_CHILDREN.containsKey(parent)) { //Check in the current child list
                            if (!FILTER_ROOT.containsKey(parent)) {
                                String simpleName = filterChild.getClass().getSimpleName();
                                FILTER_ROOT.put(simpleName, filterChild);
                                FILTER_CHILDREN.remove(simpleName);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * @param pathname - The root path
     * @param filter   - The file filter
     * @return A List<File> containing the file(s)
     */
    private List<File> findFileByType(File pathname, FileFilter filter) {
        if (pathname.isDirectory()) {
            List<File> files = new ArrayList<File>();
            for (File f : pathname.listFiles(filter)) {
                files.addAll(findFileByType(f, filter));
            }
            return files;
        } else {
            return Collections.singletonList(pathname);
        }
    }

    /**
     * @param name        - The name of the current file
     * @param is          - The InputStream to read from
     * @param rawClassMap - The map to add the items
     */
    private void addFiltersToMap(String name, InputStream is, Map<String, byte[]> rawClassMap) {

        if (is == null || rawClassMap == null || name == null) {
            return;
        }

        name = name.replaceAll(Pattern.quote(File.separator),"/");

        if (name.matches(".*(\\/?)filters\\/\\w*\\.class")) {
            byte[] bytes = readInputStreamToBytes(is);

            if (bytes != null && bytes.length > 0) {
                rawClassMap.put(name, bytes);
            }
        }
    }

    public static FilterClassLoader getInstance() {
        return OUR_INSTANCE;
    }

    /**
     * @param is - The InputStream to be converted into byte[]
     * @return A byte[] containing the binary data of the InputStream or Null if the file parameter is Null
     */
    private static byte[] readInputStreamToBytes(InputStream is) {
        if (is == null) {
            return null;
        }

        ByteArrayOutputStream value = new ByteArrayOutputStream();

        int nRead;
        byte[] buffer = new byte[8192];

        try {
            while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {
                value.write(buffer, 0, nRead);
            }

            value.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value.toByteArray();
    }

    /**
     * @param name - The name of the filter
     * @return The value mapped to the name parameter or null if the key is not mapped
     */
    public Filter getFiltersByName(String name) {
        return FILTER_CHILDREN.get(name);
    }

    /**
     * @return A Collection containing the root filters (with no parents), null if there's no root filters
     */
    public Collection<Filter> getRootFilters() {
        return FILTER_ROOT.values();
    }
}