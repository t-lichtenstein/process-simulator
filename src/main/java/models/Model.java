package models;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Model {

    static Lock indexLock = new ReentrantLock();

    public abstract void create() throws SQLException;

    public abstract void delete() throws SQLException;

}
