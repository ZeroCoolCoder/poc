package tools;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class PostgresTableDump {

    public static void main(String[] args) throws Exception {
        String jdbcUrl = "jdbc:postgresql://host:5432/dbname";
        String username = "your_user";
        String password = "your_password";

        String schema = "public";
        String table = "your_table";
        String outputFile = "/data/export/your_table.dat";

        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);

        // Good defaults for large streaming export
        props.setProperty("reWriteBatchedInserts", "true");
        props.setProperty("tcpKeepAlive", "true");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
             OutputStream out = new BufferedOutputStream(
                     new FileOutputStream(outputFile),
                     16 * 1024 * 1024)) {

            PGConnection pgConnection = conn.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();

            String sql = """
                COPY (
                    SELECT *
                    FROM %s.%s
                )
                TO STDOUT
                WITH (
                    FORMAT csv,
                    DELIMITER '|',
                    QUOTE '"',
                    ESCAPE '"',
                    NULL '\\N',
                    HEADER false,
                    ENCODING 'UTF8'
                )
                """.formatted(schema, table);

            long start = System.nanoTime();
            long rows = copyManager.copyOut(sql, out);
            out.flush();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            double rowsPerSec = elapsedMs == 0 ? rows : (rows * 1000.0 / elapsedMs);

            System.out.println("Export complete");
            System.out.println("Rows exported: " + rows);
            System.out.println("Elapsed ms   : " + elapsedMs);
            System.out.println("Rows/sec     : " + String.format("%.2f", rowsPerSec));
            System.out.println("File         : " + outputFile);
        }
    }
}