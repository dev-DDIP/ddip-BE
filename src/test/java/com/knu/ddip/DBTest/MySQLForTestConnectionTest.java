package com.knu.ddip.DBTest;

import com.knu.ddip.config.MySQLTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(MySQLTestContainerConfig.class)
@Import(MySQLTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MySQLForTestConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(1)).isTrue();

            String url = connection.getMetaData().getURL();
            System.out.println("Database URL: " + url);
            assertThat(url).contains("jdbc:mysql://");
        }
    }
}
