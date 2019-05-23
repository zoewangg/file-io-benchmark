package com.zoewang.fileiobenchmark;

import static com.zoewang.fileiobenchmark.BenchmarkUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FileUtils;

public class WriteProcessorJava6Copy implements WriteProcessor {
    private static final int BUFFER_SIZE = (int) (2 * FileUtils.ONE_MB);
    private String destinationFile;
    private long position;
    private InputStream inputStream;

    WriteProcessorJava6Copy(String destinationFile, long position, InputStream inputStream) {
        this.destinationFile = destinationFile;
        this.position = position;
        this.inputStream = inputStream;
    }

    public void write() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(destinationFile, "rw")) {
            FileChannel channel = randomAccessFile.getChannel();
            channel.position(position);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            while ((bytesRead = inputStream.read(buffer)) > -1) {
                byteBuffer.put(buffer, 0, bytesRead);
                ((Buffer) byteBuffer).flip();

                while (byteBuffer.hasRemaining()) {
                    channel.write(byteBuffer);
                }
                ((Buffer) byteBuffer).flip();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(inputStream);
        }
    }
}
