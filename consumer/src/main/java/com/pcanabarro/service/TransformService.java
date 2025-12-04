package com.pcanabarro.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class TransformService {

    private static final Map<String, List<String>> DATE_FIELDS = Map.of(
            "salary", List.of("effective_from"),
            "employee", List.of("hired_at")
    );

    public String transform(String jsonString) {
        JSONObject json = new JSONObject(jsonString);

        String op = json.getString("op");
        String table = json.getJSONObject("source").getString("table");

        if (op.equals("d")) {
            JSONObject before = json.getJSONObject("before");
            return "DELETE FROM " + table + " WHERE id=" + before.getInt("id");
        }

        JSONObject after = json.getJSONObject("after");
        fixDates(after, table);

        return buildSql(after, table, op);
    }

    private void fixDates(JSONObject after, String table) {
        List<String> fields = DATE_FIELDS.get(table);
        if (fields == null) return;

        for (String f : fields) {
            if (!after.has(f)) continue;

            Object v = after.get(f);
            if (v instanceof Number n) {
                LocalDate d = LocalDate.ofEpochDay(n.intValue());
                after.put(f, d.toString());
            }
        }
    }

    private String esc(String v) { return v.replace("'", "''"); }

    private String buildSql(JSONObject after, String table, String op) {
        switch (table) {

            case "job_position" -> {
                int id = after.getInt("id");
                String title = esc(after.getString("title"));
                String dept = esc(after.getString("department"));
                String createdAt = after.getString("created_at");

                return switch (op) {
                    case "c" -> "INSERT INTO job_position VALUES (" + id +
                            ", '" + title + "', '" + dept + "', '" + createdAt + "')";
                    case "u" -> "UPDATE job_position SET title='" + title +
                            "', department='" + dept +
                            "', created_at='" + createdAt +
                            "' WHERE id=" + id;
                    default -> null;
                };
            }

            case "employee" -> {
                int id = after.getInt("id");
                String name = esc(after.getString("name"));
                String email = esc(after.getString("email"));
                int pos = after.getInt("job_position_id");
                String hiredAt = after.getString("hired_at");

                return switch (op) {
                    case "c" ->
                            "INSERT INTO employee VALUES (" + id +
                                    ", '" + name + "', '" + email + "', " + pos +
                                    ", '" + hiredAt + "')";
                    case "u" ->
                            "UPDATE employee SET name='" + name +
                                    "', email='" + email +
                                    "', job_position_id=" + pos +
                                    ", hired_at='" + hiredAt + "' WHERE id=" + id;
                    default -> null;
                };
            }

            case "salary" -> {
                int id = after.getInt("id");
                int emp = after.getInt("employee_id");
                String amount = after.getString("amount");
                String eff = after.getString("effective_from");

                return switch (op) {
                    case "c" ->
                            "INSERT INTO salary VALUES (" + id +
                                    ", " + emp + ", '" + amount + "', '" + eff + "')";
                    case "u" ->
                            "UPDATE salary SET employee_id=" + emp +
                                    ", amount='" + amount +
                                    "', effective_from='" + eff + "' WHERE id=" + id;
                    default -> null;
                };
            }
            default -> {
                return null;
            }
        }
    }
}
