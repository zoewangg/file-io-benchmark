/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.zoewang.fileiobenchmark;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class RandomInputStream extends InputStream {

    /** The requested amount of data contained in this random stream */
    protected final long lengthInBytes;

    /** The number of bytes of data remaining in this random stream */
    protected long remainingBytes;

    /**
     * Constructs a new InputStream, which will return the specified amount
     * of bytes of random ASCII characters.
     *
     * @param lengthInBytes The size in bytes of the total data returned by this
     * stream.
     */
    public RandomInputStream(long lengthInBytes) {
        this.lengthInBytes = lengthInBytes;
        this.remainingBytes = lengthInBytes;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Signal that we're out of data if we've hit our limit
        if (remainingBytes <= 0) {
            return -1;
        }
        int bytesToRead = len;
        if (bytesToRead > remainingBytes) {
            bytesToRead = (int) remainingBytes;
        }

        remainingBytes -= bytesToRead;
        for (int i = 0; i < bytesToRead; i++) {
            b[off + i] = (byte) 45;
        }

        return bytesToRead;
    }

    @Override
    public int read() throws IOException {
        // Signal that we're out of data if we've hit our limit
        if (remainingBytes <= 0) {
            return -1;
        }

        remainingBytes--;
        return 1;
    }

    public long getBytesRead() {
        return lengthInBytes - remainingBytes;
    }
}