package com.pastclimate;

import java.time.Duration;

/** Waits between retries. Package-private so tests can replace it. */
interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
