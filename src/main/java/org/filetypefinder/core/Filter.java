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

/**
 * Created by Yannick on 2/22/2016.
 */
public interface Filter {
    /**
     * @param bytes - The file binary to be analysed
     * @return True if the requestedMediaType is inside the list, False if not or Null if there's a null parameter
     */
    boolean detect(byte[] bytes);
}
