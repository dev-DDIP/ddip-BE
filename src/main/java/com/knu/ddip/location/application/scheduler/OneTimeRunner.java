package com.knu.ddip.location.application.scheduler;

public interface OneTimeRunner {
    void runOnce(String lockName, Runnable task);
}

