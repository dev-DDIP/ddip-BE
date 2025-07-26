package com.knu.ddip.DBTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@SpringBootTest
public class MysqlConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void mysqlConnectionTest() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            Assertions.assertFalse(connection.isClosed(), "MySQL 연결 실패");
            System.out.println("✅ MySQL 연결 성공: " + connection.getMetaData().getURL());
        }
    }
}
