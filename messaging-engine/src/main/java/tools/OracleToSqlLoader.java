package tools;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.Base64;

public class OracleToSqlLoader {

    static final char ESC = '\\';
    static final char US = 0x1F; // unit separator
    static final byte[] NEWLINE = "\n".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws Exception {
        // args: jdbcUrl user pass outFile sql
        if (args.length < 5) {
            System.err.println("Usage: <jdbcUrl> <user> <pass> <outFile> <sql>");
            System.exit(2);
        }
        String jdbcUrl = args[0], user = args[1], pass = args[2];
        Path out = Paths.get(args[3]);
        String sql = args[4];

        Properties p = new Properties();
        p.put("user", user);
        p.put("password", pass);
        p.put("oracle.net.CONNECT_TIMEOUT", "10000");
        p.put("oracle.jdbc.ReadTimeout", "600000");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, p);
             PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            ps.setFetchSize(10_000);

            Files.createDirectories(out.toAbsolutePath().getParent());
            try (OutputStream fos = Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                 BufferedOutputStream bos = new BufferedOutputStream(fos, 8 * 1024 * 1024)) {

                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int n = md.getColumnCount();

                    long rows = 0;
                    while (rs.next()) {
                        for (int i = 1; i <= n; i++) {
                            if (i > 1) bos.write((byte) US);

                            int t = md.getColumnType(i);
                            if (rs.getObject(i) == null) continue; // empty field

                            switch (t) {
                                case Types.BLOB, Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> {
                                    try (InputStream in = rs.getBinaryStream(i)) {
                                        writeBase64(in, bos);
                                    }
                                }
                                case Types.CLOB, Types.NCLOB, Types.LONGVARCHAR, Types.LONGNVARCHAR -> {
                                    try (Reader r = rs.getCharacterStream(i)) {
                                        writeEscapedText(r, bos);
                                    }
                                }
                                default -> {
                                    String s = rs.getString(i);
                                    if (s != null) writeEscapedText(new StringReader(s), bos);
                                }
                            }
                        }
                        bos.write(NEWLINE);

                        rows++;
                        if (rows % 1_000_000 == 0) {
                            bos.flush();
                            System.out.printf("exported %,d rows%n", rows);
                        }
                    }
                }
                bos.flush();
            }
        }
    }

    static void writeEscapedText(Reader r, OutputStream out) throws IOException {
        char[] buf = new char[8192];
        int n;
        while ((n = r.read(buf)) != -1) {
            for (int i = 0; i < n; i++) {
                char c = buf[i];
                if (c == ESC) {
                    out.write('\\'); out.write('\\');
                } else if (c == '\n') {
                    out.write('\\'); out.write('n');
                } else if (c == '\r') {
                    out.write('\\'); out.write('r');
                } else if (c == US) {
                    // very rare, but make it safe
                    out.write('\\'); out.write('u'); out.write('0'); out.write('0'); out.write('1'); out.write('F');
                } else {
                    // UTF-8 encode char
                    byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                    out.write(bytes);
                }
            }
        }
    }

    static void writeBase64(InputStream in, OutputStream out) throws IOException {
        Base64.Encoder enc = Base64.getEncoder();
        byte[] buf = new byte[8192];
        int n;

        ByteArrayOutputStream carry = new ByteArrayOutputStream(8192);

        while ((n = in.read(buf)) != -1) {
            carry.write(buf, 0, n);
            byte[] bytes = carry.toByteArray();
            int full = (bytes.length / 3) * 3;
            if (full == 0) continue;

            out.write(enc.encode(Arrays.copyOfRange(bytes, 0, full)));
            carry.reset();
            carry.write(bytes, full, bytes.length - full);
        }

        byte[] rem = carry.toByteArray();
        if (rem.length > 0) out.write(enc.encode(rem));
    }
}
