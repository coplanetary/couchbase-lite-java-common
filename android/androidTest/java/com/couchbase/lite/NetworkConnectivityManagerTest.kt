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
package com.couchbase.lite

import com.couchbase.lite.internal.AndroidConnectivityManager
import com.couchbase.lite.internal.core.C4Replicator
import com.couchbase.lite.internal.replicator.AndroidConnectivityObserver
import com.couchbase.lite.internal.replicator.NetworkConnectivityManager
import com.couchbase.lite.internal.utils.Fn
import org.junit.Assert
import org.junit.Test


class NetworkConnectivityManagerTest : BaseTest() {

    class TestObserver : NetworkConnectivityManager.Observer {
        var changeCalls = 0
        override fun onConnectivityChanged(connected: Boolean) {
            changeCalls++
        }
    }

    class TestManager : NetworkConnectivityManager {
        val observers = mutableSetOf<NetworkConnectivityManager.Observer>()
        var conCalls = 0
        override fun registerObserver(observer: NetworkConnectivityManager.Observer) {
            observers.add(observer)
        }

        override fun unregisterObserver(observer: NetworkConnectivityManager.Observer) {
            observers.remove(observer)
        }

        override fun isConnected(): Boolean {
            conCalls++
            return true
        }
    }

    @Test
    fun testStartStopPre21() = testStartStop(AndroidConnectivityManager(19) { r -> r.run() })

    @Test
    fun testStartStop21to23() = testStartStop(AndroidConnectivityManager(22) { r -> r.run() })

    @Test
    fun testStartStop24to28() = testStartStop(AndroidConnectivityManager(26) { r -> r.run() })

    @Test
    fun testStartStopPost29() = testStartStop(AndroidConnectivityManager(29) { r -> r.run() })

    @Test
    fun testOffline() {
        val mgr = TestManager()

        val replFactory = object : Fn.Provider<C4Replicator?> {
            var calls = 0
            override fun get(): C4Replicator? {
                calls++
                return null
            }
        }

        val observer = AndroidConnectivityObserver(mgr, replFactory)

        // Now online: don't observe the network anymore, regardless of previous state
        mgr.observers.add(observer)
        observer.handleOffline(AbstractReplicator.ActivityLevel.CONNECTING, true)
        Assert.assertTrue(mgr.observers.isEmpty())

        // Now online: don't observe the network anymore, regardless of previous state
        mgr.observers.add(observer)
        observer.handleOffline(AbstractReplicator.ActivityLevel.OFFLINE, true)
        Assert.assertTrue(mgr.observers.isEmpty())
        Assert.assertEquals(0, replFactory.calls)

        // Now ofline but previously offline: no change
        mgr.observers.add(observer)
        observer.handleOffline(AbstractReplicator.ActivityLevel.OFFLINE, false)
        Assert.assertEquals(1, mgr.observers.size)
        Assert.assertEquals(0, replFactory.calls)

        // Now ofline but previously online: subscribe and try to tell the C4Replicator
        observer.handleOffline(AbstractReplicator.ActivityLevel.CONNECTING, false)
        Assert.assertEquals(1, mgr.observers.size)
        Assert.assertEquals(1, replFactory.calls)
    }

    fun testStartStop(mgr: AndroidConnectivityManager) {
        val observer = TestObserver()

        mgr.registerObserver(observer)
        Assert.assertTrue(mgr.isRunning)

        // this has to be sloppy because the registration might cause an immediate callback
        Assert.assertTrue(observer.changeCalls <= 1)
        mgr.connectivityChanged(true)
        mgr.connectivityChanged(false)
        mgr.connectivityChanged(true)
        Assert.assertTrue(observer.changeCalls >= 3)

        mgr.unregisterObserver(observer)
        Assert.assertFalse(mgr.isRunning)
    }
}
