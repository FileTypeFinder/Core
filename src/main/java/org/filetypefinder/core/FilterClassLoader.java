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
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Yannick on 2/16/2016.
 */
public final class FilterClassLoader extends ClassLoader {
    private static final String FILE_TYPE_PKG;
    private static final Map<String, Filter> FILTER_ROOT;
    private static final Map<String, Filter> FILTER_CHILDREN;
    private static final FilterClassLoader OUR_INSTANCE;

    static {
        FILE_TYPE_PKG = "org.filetypefinder.filetype.";
        FILTER_CHILDREN = Collections.synchronizedMap(new HashMap<String, Filter>());
        FILTER_ROOT = Collections.synchronizedMap(new HashMap<String, Filter>());
        OUR_INSTANCE = new FilterClassLoader();
    }

    private FilterClassLoader() {

        URL resources = getClass().getResource("../filetype/");

        new File(getClass().getClassLoader().getResource("file/test.xml").getFile());


        if (resources != null) {

            File filetyFolder = new File(resources.getPath());

            File[] fileTypeClasses = filetyFolder.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".class");
                }
            });

            if (fileTypeClasses != null) {
                for (File classFile : fileTypeClasses) {
                    byte[] bytes = readFileToBytes(classFile);

                    String filename = classFile.getName().split(Pattern.quote("."))[0];
                    String binaryName = FILE_TYPE_PKG + filename;

                    try {
                        Class<?> clazz = defineClass(binaryName, bytes, 0, bytes.length);
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

    public static FilterClassLoader getInstance() {
        return OUR_INSTANCE;
    }

    /**
     * @param file - The file to be converted into byte[]
     * @return A byte[] containing the binary data of the file or Null if the file parameter is Null
     */
    private static byte[] readFileToBytes(File file) {
        byte[] bytes = null;

        if (file != null) {
            try {
                InputStream is = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                for (int i = is.read(); i != -1; i = is.read()) {
                    baos.write((byte) i);
                }
                baos.flush();
                bytes = baos.toByteArray();
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
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