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

import org.filetypefinder.core.Filter;
import org.filetypefinder.core.FilterProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Yannick on 2/24/2016.
 */
public class TreeElement {
    private final List<TreeElement> child;
    private String[] strChild;
    private String strParent;
    private TreeElement parent;
    private Filter current;
    private String mimeType;

    public TreeElement(Filter current) {
        child = new ArrayList<TreeElement>();
        this.current = current;

        if (current != null) {
            FilterProperties annotation = current.getClass().getAnnotation(FilterProperties.class);
            if (annotation != null) {
                strParent = annotation.parent();
                strChild = annotation.childs();
                mimeType = annotation.mimeType();
            }
        }
    }

    /**
     * @param elements - Add one or more children to the TreeElement
     */
    public void addChildren(TreeElement... elements) {
        if (elements != null && elements.length > 0) {
            child.addAll(Arrays.asList(elements));
        }
    }

    /**
     * @param elements - Set the current parent of the TreeElement
     */
    public void addParents(TreeElement elements) {
        parent = elements;
    }

    /**
     * @return - A String[] containing the child names (extracted from the enum)
     */
    public String[] getStrChild() {
        return (strChild != null) ? strChild.clone() : null;
    }

    /**
     * @return - The parent of the TreeElement
     */
    public TreeElement getParent() {
        return parent;
    }

    /**
     * @return - The current filter of the TreeElement
     */
    public Filter getCurrent() {
        return current;
    }

    /**
     * @return - The current media / mime type of the TreeElement
     */
    public String getMimeType() {
        return mimeType;
    }
}
