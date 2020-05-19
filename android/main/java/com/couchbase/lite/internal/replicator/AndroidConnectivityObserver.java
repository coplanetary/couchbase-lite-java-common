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
package com.couchbase.lite.internal.replicator;

import android.support.annotation.NonNull;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.internal.core.C4Replicator;
import com.couchbase.lite.utils.Fn;


public class AndroidConnectivityObserver implements NetworkConnectivityManager.Observer {
    @NonNull
    private final NetworkConnectivityManager connectivityManager;
    @NonNull
    private final Fn.Provider<C4Replicator> replFactory;

    public AndroidConnectivityObserver(
        @NonNull NetworkConnectivityManager connectivityManager,
        @NonNull Fn.Provider<C4Replicator> replFactory) {
        this.connectivityManager = connectivityManager;
        this.replFactory = replFactory;
    }

    @Override
    public void onConnectivityChanged(boolean connected) {
        final C4Replicator c4Repl = replFactory.get();
        if (c4Repl == null) { return; }

        c4Repl.setHostReachable(connected);
    }

    public void handleOffline(@NonNull AbstractReplicator.ActivityLevel prevLevel, boolean nowOnline) {
        if (nowOnline) {
            connectivityManager.unregisterObserver(this);
            return;
        }

        if (prevLevel.equals(AbstractReplicator.ActivityLevel.OFFLINE)) { return; }

        connectivityManager.registerObserver(this);

        onConnectivityChanged(connectivityManager.isConnected());
    }
}
