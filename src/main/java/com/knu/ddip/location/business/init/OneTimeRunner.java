package com.knu.ddip.location.application.init;

public interface OneTimeRunner {
    void runOnce(String lockName, Runnable task);
}

