package org.example;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionPool implements IConnectionPool {

    @Setter
    @Getter
    private static IConnectionInfrastructure _connectionInfrastructure;
    @Setter
    @Getter
    private static int _maxConnectionsNumber = 40;

    final static Object key = new Object();
    final static Object pool_key = new Object();
    private static ConnectionPool INSTANCE = null;

    Queue<Connection> connections = new LinkedList<Connection>();


    private ConnectionPool() {
        /*try {
            Class.forName("org.sqlite.JDBC");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }*/

        System.out.println("Connection established: " + _connectionInfrastructure.getConnectionKind());

        for (int i = 0; i < _maxConnectionsNumber; i++) {
            Connection conn = null;
            try {
                conn = _connectionInfrastructure.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            connections.add(conn);
        }
    }

    @Override
    public Connection getConnection() throws InterruptedException {
        synchronized (pool_key) {
            while (connections.size() == 0) {
                pool_key.wait();
                // return here after notify
            }

            Connection conn = connections.remove();
            return conn;
        }
    }

    @Override
    public void returnConnection(Connection conn) {
        synchronized (pool_key) {
            connections.add(conn);
            pool_key.notifyAll();
        }
    }

    @Override
    public void close() {
        synchronized (pool_key) {
            for (Connection conn : connections) {
                try {
                    conn.close();
                }
                catch (Exception ex) {

                }
            }
            while (connections.size() > 0) {
                try {
                    connections.remove();
                }
                catch (Exception ex) {

                }
            }
        }
    }

    public static ConnectionPool getInstance() {
        if (INSTANCE == null ){
            synchronized (key) {
                if (INSTANCE == null) {
                    INSTANCE = new ConnectionPool();
                }
            }
        }

        return INSTANCE;
    }

}
