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

import org.filetypefinder.tree.Tree;
import org.filetypefinder.tree.TreeElement;

import java.util.*;

/**
 * Created by Yannick on 2/23/2016.
 */

public final class DetectionService {
    private static final FilterClassLoader FILTER_CLASS_LOADER;
    private static final DetectionService OUR_INSTANCE;
    private static final List<Tree> MAPPED_FILTERS;

    static {
        FILTER_CLASS_LOADER = FilterClassLoader.getInstance();
        MAPPED_FILTERS = Collections.synchronizedList(new ArrayList<Tree>());
        OUR_INSTANCE = new DetectionService();
    }

    private DetectionService() {

        //Create the tree(s) from the root(s)
        for (Filter filter : FILTER_CLASS_LOADER.getRootFilters()) {
            Tree tree = new Tree(new TreeElement(filter));

            buildTree(tree, tree.getRoot());
            MAPPED_FILTERS.add(tree);
        }
    }

    public static DetectionService getInstance() {
        return OUR_INSTANCE;
    }

    /**
     * @param tree              - The tree to be analysed
     * @param currentRootFilter - The TreeElement to be treated
     */
    private void buildTree(Tree tree, TreeElement currentRootFilter) {

        if (currentRootFilter == null) {
            return;
        }

        String[] children = currentRootFilter.getStrChild();

        if (children == null || children.length == 0) {
            tree.addLeaf(currentRootFilter);
            return;
        }

        for (String child : children) {
            Filter childrenFiltersByName = FILTER_CLASS_LOADER.getFiltersByName(child);

            if (childrenFiltersByName != null) {

                TreeElement treeElement = new TreeElement(childrenFiltersByName);
                treeElement.addParents(currentRootFilter);

                currentRootFilter.addChildren(treeElement);
                buildTree(tree, treeElement);
            }
        }
    }

    /**
     * @param bytes              - The file binary to be analysed
     * @param requestedMediaType - The media type to be validated against the file binary
     * @return True if the requestedMediaType is inside the list, False if not or Null if there's a null parameter
     */
    public Boolean detect(byte[] bytes, String requestedMediaType) {
        if (bytes == null || requestedMediaType == null) {
            return null;
        }

        Set<String> detectedElements = new HashSet<String>();

        if (bytes.length > 0) {
            for (Tree tree : MAPPED_FILTERS) {
                for (TreeElement leaf : tree.getLeafs()) {
                    findFilter(leaf, detectedElements, bytes);
                }
            }
        }

        return detectedElements.contains(requestedMediaType);
    }

    /**
     * @param element - The TreeElement to be treated
     * @param set     - A set of String that will be filled with the media type
     * @param bytes   - The file binary to be analysed
     */
    private void findFilter(TreeElement element, Set<String> set, byte[] bytes) {

        if (element == null || bytes == null || set == null) {
            return;
        }

        Filter currentFilter = element.getCurrent();

        if (currentFilter != null) {
            if (currentFilter.detect(bytes)) {
                set.add(element.getMimeType());
            }
        }

        findFilter(element.getParent(), set, bytes);
    }
}
