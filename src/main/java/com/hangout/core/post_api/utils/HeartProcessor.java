package com.hangout.core.post_api.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.hangout.core.post_api.dto.ActionType;
import com.hangout.core.post_api.dto.event.HeartEvent;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeartProcessor {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @WithSpan(value = "flush events to db in one transaction")
    public void processPostEvents(UUID postId, List<HeartEvent> eventsForPost) {
        // split into adds and removes
        List<BigInteger> adds = new ArrayList<>();
        List<BigInteger> removes = new ArrayList<>();
        for (HeartEvent ev : eventsForPost) {
            if (ev.actionType() == ActionType.ADD)
                adds.add(ev.userId());
            else
                removes.add(ev.userId());
        }
        // nothing to do?
        if (adds.isEmpty() && removes.isEmpty()) {
            return;
        }
        // Build a SQL statement with VALUES for inserts and for deletes.
        // We'll use parameter placeholders to avoid SQL injection.
        // Example final SQL (Postgres):
        //
        // WITH ins AS (
        // INSERT INTO heart (post_id, user_id, created_at)
        // VALUES (?, ?), (?, ?), ...
        // ON CONFLICT (post_id, user_id) DO NOTHING
        // RETURNING 1
        // ), del AS (
        // DELETE FROM heart
        // WHERE (post_id, user_id) IN ( (?, ?), (?, ?), ... )
        // RETURNING 1
        // )
        // UPDATE post
        // SET hearts = hearts + (SELECT COUNT(*) FROM ins) - (SELECT COUNT(*) FROM del)
        // WHERE post_id = ?;
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // Build ins VALUES
        if (!adds.isEmpty()) {
            sql.append("WITH ins AS ( INSERT INTO heart (post_id, user_id, created_at) VALUES ");
            for (int i = 0; i < adds.size(); i++) {
                if (i > 0)
                    sql.append(", ");
                sql.append("(?, ?, now())");
                params.add(postId);
                params.add(adds.get(i));
            }
            sql.append(" ON CONFLICT (post_id, user_id) DO NOTHING RETURNING 1 ), ");
        } else {
            sql.append("WITH ins AS ( SELECT 0 WHERE false ), ");
        }

        // Build del
        if (!removes.isEmpty()) {
            sql.append(" del AS ( DELETE FROM heart WHERE (post_id, user_id) IN (");
            for (int i = 0; i < removes.size(); i++) {
                if (i > 0)
                    sql.append(", ");
                sql.append("(?, ?)");
                params.add(postId);
                params.add(removes.get(i));
            }
            sql.append(") RETURNING 1 ) ");
        } else {
            sql.append(" del AS ( SELECT 0 WHERE false ) ");
        }

        // Final update
        sql.append(
                " UPDATE post SET hearts = hearts + (SELECT COUNT(*) FROM ins) - (SELECT COUNT(*) FROM del) WHERE post_id = ?;");
        params.add(postId);
        log.debug("Executing DML Statement: {}, with parameters: {}", sql.toString(), params.toString());
        int updatedRows = jdbcTemplate.update(sql.toString(), params.toArray());
        log.debug("Total number of rows updated by heart events flush: {}", updatedRows);
    }
}
