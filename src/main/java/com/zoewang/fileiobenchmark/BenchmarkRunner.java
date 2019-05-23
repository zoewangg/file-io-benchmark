package com.zoewang.fileiobenchmark;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

public class BenchmarkRunner {

    public static void main(String... args) throws ParseException, IOException {

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addRequiredOption("p", "filePath", true, "Destination file path to be written");
        options.addRequiredOption("s", "sizePerThread", true, "Size per thread to write in MB");
        options.addRequiredOption("n", "threadNum", true, "The number of thread");
        options.addOption("a", "preAllocate", true, "Whether to pre-allocate file");
        options.addOption("c", "cleanUp", true, "Whether to delete file on exit");
        options.addOption("ts", "totalSize", true, "total size of the file");

        CommandLine cmd = parser.parse(options, args);

        String filePath = cmd.getOptionValue("p");
        boolean isPreAllocate = Boolean.parseBoolean(cmd.getOptionValue("a", "true"));
        boolean isCleanUp = Boolean.parseBoolean(cmd.getOptionValue("c", "true"));
        long sizePerThread = Long.parseLong(cmd.getOptionValue("s"));
        int threadNum = Integer.parseInt(cmd.getOptionValue("n"));
        long defaultTotalSize = sizePerThread * (long) threadNum;
        long totalSize = Long.parseLong(cmd.getOptionValue("ts", Long.toString(defaultTotalSize)));
        long totalSizeInByte = totalSize * FileUtils.ONE_MB;

        if (isPreAllocate) {
            preAllocate(filePath, totalSizeInByte);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        long position = 0;

        System.out.println("Starting to test");
        System.out.println("file path " + filePath);
        System.out.println("thread number " + threadNum);
        System.out.println("sizePerThread " + sizePerThread);
        System.out.println("Total size " + totalSize + " MB");

        long start = System.nanoTime();

        long runs = totalSize / sizePerThread;
        long remainingBytes = totalSize % sizePerThread;
        System.out.println("How many runs " + runs);

        for (int i = 0; i < runs; i++) {
            long bytesToWrite = sizePerThread * FileUtils.ONE_MB;
            WriteProcessorJava6Copy writeFile = new WriteProcessorJava6Copy(filePath, position,
                                                                            new RandomInputStream(bytesToWrite));
            futures.add(CompletableFuture.runAsync(writeFile::write, executor));
            position += bytesToWrite;
        }

        // Write the remaining
        WriteProcessorJava6Copy writeFile = new WriteProcessorJava6Copy(filePath, position,
                                                                        new RandomInputStream(remainingBytes));
        futures.add(CompletableFuture.runAsync(writeFile::write, executor));

        CompletableFuture[] futuresArray = futures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(futuresArray).join();

        long end = System.nanoTime();
        long timeElapsed = end - start;

        double seconds = timeElapsed / 1E9;
        System.out.println("Takes " + seconds + " seconds");
        executor.shutdown();

        if (isCleanUp) {
            cleanUp(filePath);
        }
    }

    private static void cleanUp(String filePath) throws IOException {
        try {
            if (!"/dev/null".equals(filePath)) {
                Files.deleteIfExists(new File(filePath).toPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void preAllocate(String filePath, long size) throws IOException {
        System.out.println("Pre allocating file");
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.setLength(size);
        }
    }
}
