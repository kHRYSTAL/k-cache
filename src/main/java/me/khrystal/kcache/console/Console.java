package me.khrystal.kcache.console;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kHRYSTAL on 18/12/28.
 */
public class Console {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void println(String string) {
        executorService.execute(() -> System.out.println(string));
    }
}
