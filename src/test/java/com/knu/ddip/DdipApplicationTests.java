package com.knu.ddip;

import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
class DdipApplicationTests {

    @Test
    void contextLoads() {
    }

}
