package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.location.application.service.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final LocationJpaRepository locationJpaRepository;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void deleteAll() {
        locationJpaRepository.deleteAllInBatch();
    }

    @Override
    public void saveAll(List<String> cellIds) {
        String sql = """
                INSERT INTO locations (cell_id) VALUES (?)
                """;

        jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        String cellId = cellIds.get(i);
                        ps.setString(1, cellId);
                    }

                    @Override
                    public int getBatchSize() {
                        return cellIds.size();
                    }
                });
    }
}
