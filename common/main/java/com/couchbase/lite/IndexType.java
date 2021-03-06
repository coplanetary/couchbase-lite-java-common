//
// Copyright (c) 2020, 2017 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.couchbase.lite;

/**
 * Types of database indexes.
 */
@SuppressWarnings("PMD.FieldNamingConventions")
public enum IndexType {
    /**
     * Regular index of property values.
     */
    Value(0),
    /**
     * Full-text index.
     */
    FullText(1),
    /**
     * Index of prediction() results (Enterprise Edition only)
     */
    Predictive(3);

    private final int value;

    IndexType(int value) { this.value = value; }

    int getValue() { return value; }
}
