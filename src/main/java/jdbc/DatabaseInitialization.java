package jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseInitialization {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseInitialization.class);

    public static void createNewDatabase(String path) {
        final String url = "jdbc:sqlite:" + path;

        final String methodsDictionary = "create table methodsDictionary (\n" +
                "  id integer not null primary key,\n" +
                "  fullSignature varchar(1024) unique,\n" +
                "  startOffset integer not null\n" +
                ");";

        final String methodsChangeLog = "CREATE TABLE methodsChangeLog (\n" +
                "  dtChanged timestamp not null,\n" +
                "  authorName varchar(512) not null,\n" +
                "  branchName varchar(512) not null,\n" +
                "  signatureId integer not null,\n" +
                "  CONSTRAINT fk_sig_id\n" +
                "    FOREIGN KEY (signatureId)\n" +
                "    REFERENCES methodsDictionary(id)\n" +
                ");";

        final String statsTable = "create table stats\n" +
                "(\n" +
                "  dtDateTime   timestamp not null,\n" +
                "  discrType    integer   not null,\n" +
                "  signatureId  integer   not null,\n" +
                "  changesCount integer   not null,\n" +
                "  unique (dtDateTime, discrType, signatureId)\n" +
                ");";

        final String statsView = "create view statisticsView as\n" +
                "select (\n" +
                "        dtDateTime,\n" +
                "        discrType,\n" +
                "        fullSignature,\n" +
                "        changesCount,\n" +
                "        startOffset\n" +
                "         )\n" +
                "from stats\n" +
                "       join methodsDictionary on signatureId = id;";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                final DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                final Statement statement = conn.createStatement();
                statement.execute(methodsDictionary);
                statement.execute(methodsChangeLog);
                statement.execute(statsTable);
                statement.execute(statsView);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}