
//
// Copyright (c) 2020 Couchbase, Inc All rights reserved.
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
package com.couchbase.lite.internal;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import com.couchbase.lite.LiteCoreException;
import com.couchbase.lite.internal.core.C4BlobStore;
import com.couchbase.lite.internal.core.C4Database;
import com.couchbase.lite.internal.support.Log;
import com.couchbase.lite.internal.utils.Preconditions;


public class SimpleDatabase {

    @NonNull
    protected final C4Database c4Database;

    // Main database lock object for thread-safety
    @NonNull
    protected final Object lock = new Object();

    @NonNull
    protected final AtomicBoolean isOpen = new AtomicBoolean(true);

    public SimpleDatabase(long c4DbHandle) { this(new C4Database(c4DbHandle)); }

    protected SimpleDatabase(C4Database c4Db) { c4Database = Preconditions.assertNotNull(c4Db, "c4Database"); }

    @NonNull
    public C4BlobStore getBlobStore() throws LiteCoreException { return c4Database.getBlobStore(); }

    // When seizing multiple locks, always seize this lock first.
    @NonNull
    public Object getLock() { return lock; }

    protected void mustBeOpen() {
        if (!isOpen.get()) { throw new IllegalStateException(Log.lookupStandardMessage("DBClosed")); }
    }
}
