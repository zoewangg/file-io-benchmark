package com.zoewang.fileiobenchmark;

import java.io.Closeable;
import java.io.IOException;

public final class BenchmarkUtils {

    private BenchmarkUtils() {
    }

    public static void closeQuietly(Closeable is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
