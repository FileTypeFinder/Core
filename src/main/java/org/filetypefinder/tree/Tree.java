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

package org.filetypefinder.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yannick on 2/24/2016.
 */
public class Tree {
    private TreeElement root;

    private List<TreeElement> leafs;

    public Tree(TreeElement root) {
        this.root = root;
        leafs = new ArrayList<TreeElement>();
    }

    public TreeElement getRoot() {
        return root;
    }

    /**
     * @param root - Add a root to the current tree
     */
    public void addLeaf(TreeElement root) {
        if (root != null) {
            leafs.add(root);
        }
    }

    /**
     * @return A list containing the leafs (with no children) or an empty list if there's no leafs
     */
    public List<TreeElement> getLeafs() {
        return leafs;
    }
}
