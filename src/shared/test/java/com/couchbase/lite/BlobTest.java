//
// BlobTest.java
//
// Copyright (c) 2017 Couchbase, Inc All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.couchbase.lite.utils.IOUtils;

import static com.couchbase.lite.utils.TestUtils.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


// There are other blob tests over in DocumentTest
public class BlobTest extends BaseTest {
    final static String kBlobTestBlob1 = "i'm blob";
    final static String kBlobTestBlob2 = "i'm blob2";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testEquals() throws CouchbaseLiteException {

        byte[] content1a = kBlobTestBlob1.getBytes();
        byte[] content1b = kBlobTestBlob1.getBytes();
        byte[] content2a = kBlobTestBlob2.getBytes();

        // store blob
        Blob data1a = new Blob("text/plain", content1a);
        Blob data1b = new Blob("text/plain", content1b);
        Blob data1c = new Blob("text/plain", content1a); // not store in db
        Blob data2a = new Blob("text/plain", content2a);

        assertEquals(data1a, data1b);
        assertEquals(data1b, data1a);
        assertNotEquals(data1a, data2a);
        assertNotEquals(data1b, data2a);
        assertNotEquals(data2a, data1a);
        assertNotEquals(data2a, data1b);

        MutableDocument mDoc = new MutableDocument();
        mDoc.setBlob("blob1a", data1a);
        mDoc.setBlob("blob1b", data1b);
        mDoc.setBlob("blob2a", data2a);
        Document doc = save(mDoc);

        Blob blob1a = doc.getBlob("blob1a");
        Blob blob1b = doc.getBlob("blob1b");
        Blob blob2a = doc.getBlob("blob2a");

        assertEquals(blob1a, blob1b);
        assertEquals(blob1b, blob1a);
        assertNotEquals(blob1a, blob2a);
        assertNotEquals(blob1b, blob2a);
        assertNotEquals(blob2a, blob1a);
        assertNotEquals(blob2a, blob1b);

        assertEquals(blob1a, data1c);
        assertEquals(data1c, blob1a);
    }

    @Test
    public void testHashCode() throws CouchbaseLiteException {
        byte[] content1a = kBlobTestBlob1.getBytes();
        byte[] content1b = kBlobTestBlob1.getBytes();
        byte[] content2a = kBlobTestBlob2.getBytes();

        // store blob
        Blob data1a = new Blob("text/plain", content1a);
        Blob data1b = new Blob("text/plain", content1b);
        Blob data1c = new Blob("text/plain", content1a); // not store in db
        Blob data2a = new Blob("text/plain", content2a);

        assertEquals(data1a.hashCode(), data1b.hashCode());
        assertEquals(data1b.hashCode(), data1a.hashCode());
        assertNotEquals(data1a.hashCode(), data2a.hashCode());
        assertNotEquals(data1b.hashCode(), data2a.hashCode());
        assertNotEquals(data2a.hashCode(), data1a.hashCode());
        assertNotEquals(data2a.hashCode(), data1b.hashCode());

        MutableDocument mDoc = new MutableDocument();
        mDoc.setBlob("blob1a", data1a);
        mDoc.setBlob("blob1b", data1b);
        mDoc.setBlob("blob2a", data2a);
        Document doc = save(mDoc);

        Blob blob1a = doc.getBlob("blob1a");
        Blob blob1b = doc.getBlob("blob1b");
        Blob blob2a = doc.getBlob("blob2a");

        assertEquals(blob1a.hashCode(), blob1b.hashCode());
        assertEquals(blob1b.hashCode(), blob1a.hashCode());
        assertNotEquals(blob1a.hashCode(), blob2a.hashCode());
        assertNotEquals(blob1b.hashCode(), blob2a.hashCode());
        assertNotEquals(blob2a.hashCode(), blob1a.hashCode());
        assertNotEquals(blob2a.hashCode(), blob1b.hashCode());

        assertEquals(blob1a.hashCode(), data1c.hashCode());
        assertEquals(data1c.hashCode(), blob1a.hashCode());
    }

    @Test
    public void testGetContent() throws IOException, CouchbaseLiteException {
        byte[] bytes;

        InputStream is = getAsset("attachment.png");
        try {
            bytes = IOUtils.toByteArray(is);
        }
        finally {
            is.close();
        }

        Blob blob = new Blob("image/png", bytes);
        MutableDocument mDoc = new MutableDocument("doc1");
        mDoc.setBlob("blob", blob);
        Document doc = save(mDoc);
        Blob savedBlob = doc.getBlob("blob");
        assertNotNull(savedBlob);
        assertEquals("image/png", savedBlob.getContentType());
        byte[] content = blob.getContent();
        assertTrue(Arrays.equals(content, bytes));
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1438
    @Test
    public void testGetContent6MBFile() throws IOException, CouchbaseLiteException {
        byte[] bytes;

        InputStream is = getAsset("iTunesMusicLibrary.json");
        try {
            bytes = IOUtils.toByteArray(is);
        }
        finally {
            is.close();
        }

        Blob blob = new Blob("application/json", bytes);
        MutableDocument mDoc = new MutableDocument("doc1");
        mDoc.setBlob("blob", blob);
        Document doc = save(mDoc);
        Blob savedBlob = doc.getBlob("blob");
        assertNotNull(savedBlob);
        assertEquals("application/json", savedBlob.getContentType());
        byte[] content = blob.getContent();
        assertTrue(Arrays.equals(content, bytes));
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1611
    @Test
    public void testGetNonCachedContent6MBFile() throws IOException, CouchbaseLiteException {
        byte[] bytes;

        InputStream is = getAsset("iTunesMusicLibrary.json");
        try {
            bytes = IOUtils.toByteArray(is);
        }
        finally {
            is.close();
        }

        Blob blob = new Blob("application/json", bytes);
        MutableDocument mDoc = new MutableDocument("doc1");
        mDoc.setBlob("blob", blob);
        Document doc = save(mDoc);

        // Reload the doc from the database to make sure to "bust the cache" for the blob
        // cached in the doc object
        Document reloadedDoc = db.getDocument(doc.getId());
        Blob savedBlob = reloadedDoc.getBlob("blob");
        byte[] content = savedBlob.getContent();
        assertTrue(Arrays.equals(content, bytes));

    }

    @Test
    public void testBlobFromFileURL() throws Exception {
        String contentType = "image/png";
        Blob blob = null;
        URL url = null;
        File path = tempFolder.newFile("attachment.png");
        InputStream is = getAsset("attachment.png");

        try {
            byte[] bytes = IOUtils.toByteArray(is);
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytes);
            fos.close();

            blob = new Blob(contentType, path.toURI().toURL());
        }
        catch (Exception e) {
            fail("Failed when writing to tempFile " + e);
        }
        finally {
            is.close();
        }

        byte[] bytes = IOUtils.toByteArray(path);
        byte[] content = blob.getContent();
        assertTrue(Arrays.equals(content, bytes));

        assertThrows(IllegalArgumentException.class, () -> new Blob(null, url));

        assertThrows(IllegalArgumentException.class, () -> new Blob(contentType, (URL) null));

        assertThrows(IllegalArgumentException.class, () -> new Blob(contentType, new URL("http://java.sun.com")));
    }

    @Test
    public void testBlobReadFunctions() throws Exception {
        byte[] bytes;

        InputStream is = getAsset("iTunesMusicLibrary.json");
        try {
            bytes = IOUtils.toByteArray(is);
        }
        finally {
            is.close();
        }

        Blob blob = new Blob("application/json", bytes);
        try {
            // The "iTunesMusicLibrary.json has different size on Windowss
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                assertEquals(blob.toString(), "Blob[application/json; 6572 KB]");
            }
            else {
                assertEquals(blob.toString(), "Blob[application/json; 6560 KB]");
            }
            assertEquals(blob.getContentStream().read(), bytes[0]);

            blob = new Blob("application/json", bytes);
            byte[] bytesReadFromBlob = new byte[bytes.length];
            blob.getContentStream().read(bytesReadFromBlob, 0, bytes.length);
            assertTrue(Arrays.equals(bytesReadFromBlob, bytes));

            blob = new Blob("application/json", bytes);
            InputStream iStream = blob.getContentStream();
            iStream.skip(2);
            assertEquals(iStream.read(), bytes[2]);
        }
        catch (Exception e) {
            fail("Failed when reading the blobs " + e);
        }
    }

    @Test
    public void testReadBlobStream() throws Exception {
        byte[] bytes;
        InputStream is = getAsset("attachment.png");
        try { bytes = IOUtils.toByteArray(is); }
        finally { is.close(); }

        Blob blob = new Blob("image/png", bytes);
        MutableDocument mDoc = new MutableDocument("doc1");
        mDoc.setBlob("blob", blob);
        Document doc = save(mDoc);

        Blob savedBlob = doc.getBlob("blob");
        assertNotNull(savedBlob);
        assertEquals("image/png", savedBlob.getContentType());

        int len = 0;
        final byte[] buffer = new byte[1024];

        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            in = savedBlob.getContentStream();
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            byte[] readBytes = out.toByteArray();
            assertTrue(Arrays.equals(bytes, readBytes));
        }
        finally {
            if (out != null) { try { out.close(); } catch (Exception e) { } }
            if (in != null) { try { in.close(); } catch (Exception e) { } }
        }
    }

    @Test
    public void testBlobConstructorsWithEmptyArgs() throws Exception {
        byte[] bytes;
        String contentType = "image/png";

        InputStream is = getAsset("attachment.png");
        try { bytes = IOUtils.toByteArray(is); }
        finally { is.close(); }

        assertThrows(IllegalArgumentException.class, () -> new Blob(null, bytes));

        assertThrows(IllegalArgumentException.class, () -> new Blob(contentType, (byte[]) null));

        assertThrows(IllegalArgumentException.class, () -> new Blob(null, is));

        assertThrows(IllegalArgumentException.class, () -> new Blob(contentType, (InputStream) null));

        assertThrows(IllegalArgumentException.class, () -> new Blob(contentType, (InputStream) null));
    }
}
