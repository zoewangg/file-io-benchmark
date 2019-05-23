package com.zoewang.fileiobenchmark;

import static com.zoewang.fileiobenchmark.BenchmarkUtils.closeQuietly;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class WriteProcessorTransferFrom implements WriteProcessor {
    private String destinationFile;
    private long position;
    private InputStream inputStream;

    WriteProcessorTransferFrom(String destinationFile, long position, InputStream inputStream) {
        this.destinationFile = destinationFile;
        this.position = position;
        this.inputStream = inputStream;
    }

    public void write() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(destinationFile, "rw")) {
            FileChannel writeChannel = randomAccessFile.getChannel();
            writeChannel.position(position);
            ReadableByteChannel readChannel;
            readChannel = Channels.newChannel(inputStream);
            writeChannel.transferFrom(readChannel, position, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(inputStream);
        }
    }

}
