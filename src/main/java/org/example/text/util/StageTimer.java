package org.example.text.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class StageTimer extends Timer implements Closeable {
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (IOException ignored) {
            }
        }));
    }

    public void addTask(Runnable callback, Long intervalSeconde) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                callback.run();
            }
        };
        schedule(task, 0L, intervalSeconde);
    }

    public interface Runnable {
        void run();
    }

    @Override
    public void close() throws IOException {
        this.cancel();
    }
}
