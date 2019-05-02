package db.dao;

import db.entities.MethodChangeLogEntity;
import db.entities.MethodDictionaryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;
import state.MethodInfo;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodsDictionaryDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private final String url;

    public MethodsDictionaryDAO(String url) {
        this.url = url;
    }

    public int addToDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "insert into methodsDictionary(fullSignature, startOffset) values(?,?)";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                entities.forEach(y -> {
                    try {
                        statement.setString(1, y.getFullMethodSignature());
                        statement.setInt(2, y.getStartOffset());
                        statement.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        logger.error("Sql exception occured while trying to prepare insert statements to methodsDictionary table", e);
                    }
                });
                updatedObjectsCount.set(
                        Arrays.stream(statement.executeBatch())
                                .reduce((a, b) -> a + b).orElse(0));
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to execute batch insert to methodsDictionary table", e);
            }
        });

        return updatedObjectsCount.get();
    }

    public int addToDictionary(MethodDictionaryEntity entity) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final String sql = "insert or ignore into methodsDictionary(fullSignature, startOffset, fileName) values(?,?,?)";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, entity.getFullMethodSignature());
                statement.setInt(2, entity.getStartOffset());
                statement.setString(3, entity.getFileName());
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Sql exception occured while trying to insert new entry to methodsDictionary table", e);
            }
        });
        try {
            connectionOpt.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedObjectsCount.get();
    }

/*    public int removeFromDictionary(List<String> names) {
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                names.forEach(y -> {
                    try {
                        statement.setString(1, y);
                        statement.addBatch();
                    } catch (SQLException e) {
                        logger.error("Sql exception occured while trying to prepare delete statements for methodsDictionary table", e);
                    }
                });
                updatedObjectsCount.set(
                        Arrays.stream(statement.executeBatch())
                                .reduce((a, b) -> a + b).orElse(0));
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to execute batch delete from methodsDictionary table", e);
            }
        });

        return updatedObjectsCount.get();
    }*/

    public int removeFromDictionary(String name) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, name);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Sql exception occured while trying to delete entry from methodsDictionary table", e);
            }
        });
        try {
            connectionOpt.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedObjectsCount.get();
    }

/*    public int updateDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "update methodsDictionary set fullSignature = ?, "
                + "startOffset = ? "
                + "where id = ?";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                entities.forEach(y -> {
                    try {
                        statement.setString(1, y.getFullMethodSignature());
                        statement.setInt(2, y.getStartOffset());
                        statement.setInt(3, y.getId());
                        statement.addBatch();
                    } catch (SQLException e) {
                        logger.error("Sql exception occured while trying to prepare update statements for methodsDictionary table", e);
                    }
                });
                updatedObjectsCount.set(
                        Arrays.stream(statement.executeBatch())
                                .reduce((a, b) -> a + b).orElse(0));
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to execute batch update to methodsDictionary table", e);
            }
        });

        return updatedObjectsCount.get();
    }*/

    public int findIdBySignatureName(String fullSignature) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final String selectQuery = "SELECT id from methodsDictionary where fullSignature = ?";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger methodId = new AtomicInteger(-1);
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement pstmt = connectionOpt.get().prepareStatement(selectQuery)) {
                pstmt.setString(1, fullSignature);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    methodId.set(resultSet.getInt(1));
                }
                resultSet = null;
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Exception occured while trying to get method id by signature name");
                System.out.println(e.getMessage());
            }
        });
        return methodId.get();
    }

    public List<MethodChangeLogEntity> buildChangelogs(List<MethodInfo> changes) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final String questionMarks = String.join(", ", Collections.nCopies(changes.size(), "?"));
        final String sql = "select id from methodsDictionary where fullSignature in (" + questionMarks + ")";

        final List<MethodChangeLogEntity> entities = new LinkedList<>();
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger counter = new AtomicInteger();
        final Iterator<MethodInfo> iter = changes.iterator();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                changes.forEach(y -> {
                    try {
                        statement.setString(counter.incrementAndGet(), y.getMethodFullName());
                    } catch (SQLException e) {
                        logger.error("Sql exception occured while trying to prepare update statements for methodsDictionary table", e);
                    }
                });
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    MethodInfo info = iter.next();
                    entities.add(new MethodChangeLogEntity(
                            info.getTimeChangeMade(),
                            info.getAuthorInfo(),
                            info.getBranchName(),
                            resultSet.getInt(1)
                    ));
                }
                resultSet = null;
            } catch (SQLException e) {
                e.printStackTrace();
                //logger.error("Sql exception occured while trying to execute batch update to methodsDictionary table", e);
            }
        });
        try {
            connectionOpt.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entities;
    }

    public void dumbUpsertOfNotChangedMethodEntries(MethodDictionaryEntity entity) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String sql = "insert into methodsDictionary(fullSignature, startOffset, fileName)  values (?, ?, ?)\n" +
                "\n" +
                "on conflict(fullSignature) do update set startOffset=?";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, entity.getFullMethodSignature());
                statement.setInt(2, entity.getStartOffset());
                statement.setString(3, entity.getFileName());
                statement.setInt(4, entity.getStartOffset());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                //logger.error("Sql exception occured while trying to update methodsDictionary table", e);
            }
        });
    }

    public int updateBySignature(String oldSignature, MethodDictionaryEntity entity) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String sql = "update methodsDictionary set fullSignature = ?, fileName = ?, "
                + "startOffset = ? "
                + "where fullSignature = ?";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, entity.getFullMethodSignature());
                statement.setInt(2, entity.getStartOffset());
                statement.setString(3, oldSignature);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                //logger.error("Sql exception occured while trying to update methodsDictionary table", e);
            }
        });
        try {
            connectionOpt.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedObjectsCount.get();
    }
}