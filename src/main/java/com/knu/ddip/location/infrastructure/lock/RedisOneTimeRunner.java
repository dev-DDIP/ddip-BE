package com.knu.ddip.location.infrastructure.lock;

import com.knu.ddip.location.application.scheduler.OneTimeRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOneTimeRunner implements OneTimeRunner {

    private final RedissonClient redisson;

    @Override
    public void runOnce(String lockName, Runnable task) {
        RLock lock = redisson.getLock(lockName);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(0, 30, TimeUnit.MINUTES);
            if (!acquired) {
                return;
            }
            task.run();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for lock {}", lockName, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
