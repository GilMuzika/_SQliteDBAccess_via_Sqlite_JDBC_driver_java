package org.example;

import auxiliaryClasses.Action2;

import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class MySQLDBAccess<T> {

    private AtomicReference<Connection> _connection;
    private AtomicReference<Class<T>> _currentClass;
    private AtomicReference<String> _tableName;

    public MySQLDBAccess(Connection connection, Class<T> genericClass) throws ClassNotFoundException, SQLException {
        synchronized (this) {
            Class.forName("org.sqlite.JDBC");
            _connection = new AtomicReference<>(connection);
            _currentClass = new AtomicReference<Class<T>>(genericClass);

            _tableName = new AtomicReference<>(this.getTableName());
        }

    }

    public Connection returnConnection() {
        return _connection.getAndSet(null);
    }

    private String buildCreateTableQuery() {
        HashMap<Class, String> javaTosQLtypes = new HashMap<>();
        //org.sqlite.jdbc4.JDBC4Connection
        //org.postgresql.jdbc.PgConnection
        if(_connection.get().getClass().getName().equals("org.postgresql.jdbc.PgConnection")) {
            javaTosQLtypes.put(int.class, "integer");
            javaTosQLtypes.put(String.class, "varchar(255)");
            javaTosQLtypes.put(Float.class, "float8");
        } else if(_connection.get().getClass().getName().equals("org.sqlite.jdbc4.JDBC4Connection")) {
            javaTosQLtypes.put(int.class, "INTEGER");
            javaTosQLtypes.put(String.class, "TEXT");
            javaTosQLtypes.put(Float.class, "FLOAT");
        }

        Field[] declaredFields = _currentClass.get().getDeclaredFields();
        String sql = "CREATE TABLE "+ _tableName +" (";
        int count = 0;
        for(Field field : declaredFields) {
            String comma = ",";
            String primaryKey = "";
            if(count == declaredFields.length - 1)
                comma = "";
            DBField dbField = field.getAnnotation(DBField.class);
            String columnName = null;
            String columnType = null;
            if(dbField != null && !dbField.ignore()) {
                columnName = dbField.column_name();
                columnType = javaTosQLtypes.get(dbField.type());
                if(dbField.isPrimaryKey())
                    primaryKey = " PRIMARY KEY";
            }
            if(dbField == null) {
                columnName = field.getName();
                    if(columnName.equals("id"))
                        primaryKey = " PRIMARY KEY";
                columnType = javaTosQLtypes.get(field.getType());
            }

            sql += String.format("%s %s%s%s", columnName, columnType, primaryKey, comma);

            count++;
        }
        sql += ");";
        return  sql;
    }
    public int CreateTable() throws SQLException {
        synchronized (this) {
            //String sql = "CREATE TABLE (id integer, firstname varchar(255), lastname varchar(255), weight float8, address varchar(255), email varchar(255));";
            String sql = buildCreateTableQuery();
            PreparedStatement st = _connection.get().prepareStatement(sql);
            return st.executeUpdate();
        }
    }

    public ArrayList<T> getAll() {
        synchronized (this) {

            ArrayList<T> result = new ArrayList<>();

            try {

                Statement statement = _connection.get().createStatement();
                statement.setQueryTimeout(30);

                String sql = "select * from " + _tableName.get();
                ResultSet rs = statement.executeQuery(sql);

                HashMap<Class, IGetField> getFromDB = new HashMap<>();
                //The Value, that is class, must be consistent with the annotations in the T object,
                //if there are no annotations, must be consistent with getClass() of the specified field
                getFromDB.put(int.class, (String column) -> rs.getInt(column));
                getFromDB.put(String.class, (String column) -> rs.getString(column));
                getFromDB.put(Float.class, (String column) -> rs.getFloat(column));

                while (rs.next()) {
                    Constructor<T> ctor = _currentClass.get().getDeclaredConstructor();
                    T item = ctor.newInstance();

                    for (var field : _currentClass.get().getDeclaredFields()) {
                    /* In the case of annotated "poco" class, we have one additional filed for the table name,
                       which is the backing filed for the "@DBFieldTableName" annotation.
                       This would not case problem if the code beforehand wouldn't take into account also the unannotated
                       class case. But because the unannotated "poco" case taken into account, when "getAnnotation(DBField.class)"
                       of tje Field object returns null, the column name inferred from the name of the field itself, going around the "DBField"
                       object, which is null now. But, this approach is good only in the case of an unannotated poco, all the wields of which
                       represent columns in the table of the DB.
                       In the annotated case, the annotation "@DBFieldTableName" also
                       must have its backing field, which represents not a column of the table, but the table name itself.
                       So, when all the fields of the object scanned to get the names of hte columns, the assumption that they all represent
                       names of the columns, is wrong, and this field interferes with the column names inferring process, causing an error.

                       So, to prevent this situation,
                       when scanning all the declared fields of the class to infer the column names,
                       being interested only in the fields that are backing fields of "DBField" annotations,
                       we need to check ach one of them is it a backing field of "@DBFieldTableName" annotation.
                       If it is, we need to skip it (in this code, by skipping the iteration with the operator "continue")
                    */
                        if (field.getAnnotation(DBFieldTableName.class) != null) {
                            continue;
                        }

                        DBField dbField = field.getAnnotation(DBField.class);
                        Class fieldType = null;
                        String columnName = null;
                        if (dbField != null) { //For annotated poco
                            fieldType = dbField.type();
                            columnName = dbField.column_name();
                        } else { //for unnannotaded
                            fieldType = field.getType();
                            columnName = field.getName();

                        }
                        field.set(item, getFromDB.get(fieldType).getData(columnName));
                    }
                    result.add(item);
                }

            } catch (SQLException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException ex) {
                System.out.println(ex);
            }


            return result;
        }
    }
    public void update(T obj,int id) throws SQLException, IllegalAccessException {
        synchronized (this) {
            Statement statement = _connection.get().createStatement();
            statement.setQueryTimeout(30);
            String query = "UPDATE " + _tableName.get() + "\n" +
                    "SET ";

            for (var field : obj.getClass().getDeclaredFields()) {
                DBField dbField = field.getAnnotation(DBField.class);
                if (!field.getName().equals("TABLE_NAME") && !field.getName().equals("id") && !dbField.isPrimaryKey()) {
                    if (field.get(obj).getClass().equals(String.class)) {
                        query += field.getName() + " = '" + field.get(obj) + "',";
                    } else {
                        query += field.getName() + " = " + field.get(obj) + ",";
                    }
                }
            }
            query = query.substring(0, query.lastIndexOf(","));
            query += "\nWHERE id =" + String.valueOf(id);
            System.out.println(query);
            statement.executeUpdate(query);
            System.out.println("The table has been updated");
        }
    }
    public void deleteTable() throws SQLException {
        synchronized (this) {
            Statement statement = _connection.get().createStatement();
            statement.setQueryTimeout(30);
            statement.execute("drop table " + _tableName.get());
            System.out.println("The table has been deleted");
        }
    }

    public void insert(T dataObj) throws SQLException {
        synchronized (this) {
            String sql = null;
            HashMap<String, ArrayList<Field>> dataMap = buildInsertSqlQuery(dataObj);
            for (Map.Entry<String, ArrayList<Field>> e : dataMap.entrySet()) {
                sql = e.getKey();
            }
            ArrayList<Field> dataList = dataMap.get(sql);
            PreparedStatement ps = _connection.get().prepareStatement(sql);
            HashMap<Class, Action2<Integer, Object>> statementMethodsAssociation = new HashMap<>();
            statementMethodsAssociation.put(int.class, (p1, p2) -> ps.setInt(p1, (int) p2));
            statementMethodsAssociation.put(String.class, (p1, p2) -> ps.setString(p1, (String) p2));
            statementMethodsAssociation.put(Float.class, (p1, p2) -> ps.setFloat(p1, (Float) p2));

            int count = 1;
            for (Field f : dataList) {
                try {
                    var value = f.get(dataObj);
                    if(f.getName().equals("id")) {
                        int countRows = this.countRows();
                        value = countRows + 1;
                    }
                    statementMethodsAssociation.get(f.getType()).act(count, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                count++;
            }

            int updated = ps.executeUpdate();
            if (updated != 0)
                System.out.printf("%s: The data is successfully inserted\n", Thread.currentThread().getName());
        }
    }
    private HashMap<String, ArrayList<Field>> buildInsertSqlQuery(T obj) {
        String query = "INSERT INTO "+ _tableName.get() +" (";
        Field[] declaredFieldsArray  = obj.getClass().getDeclaredFields();
        ArrayList<Field> dataFieldsList = new ArrayList<>();
        ArrayList<Integer> ignoredFieldsPlaces = new ArrayList<>();
        int count = 0;
        for(Field field: declaredFieldsArray) {
            DBField dbField = field.getAnnotation(DBField.class);
            DBFieldTableName dbTableNameField = field.getAnnotation(DBFieldTableName.class);

            if((dbField != null && dbField.ignore()) || (dbTableNameField != null && dbTableNameField.ignore()))
                ignoredFieldsPlaces.add(count);

            if(field.getName().equals("TABLE_NAME")) {
                if(!ignoredFieldsPlaces.contains(count))
                    ignoredFieldsPlaces.add(count);
            }

            if (!ignoredFieldsPlaces.contains(count)){
                query += field.getName() + ",";
                dataFieldsList.add(field);
            }
            count++;
        }
        query = query.substring(0,query.lastIndexOf(","));
        query+= ") ";
        query+="VALUES (";

        int declaredFieldsArrayLength = declaredFieldsArray.length;
        String comma = ",";
        String placeholder = "?";
        for(int i = 0; i < declaredFieldsArrayLength; i++) {
            if(ignoredFieldsPlaces.contains(i))
                continue;
            /*if(i == 0)
                placeholder = "default";
            else
                placeholder = "?";*/
            if(i == declaredFieldsArrayLength - 1)
                comma = "";
            query += placeholder + comma +" ";
        }


        query += ");";
        HashMap<String, ArrayList<Field>> map = new HashMap<>();
        map.put(query, dataFieldsList);
        return map;
    }

    public boolean deleteById(int id) throws SQLException {
        synchronized (this) {
            String sql = String.format("DELETE FROM %s WHERE id = %s", _tableName.get(), id);
            PreparedStatement ps = _connection.get().prepareStatement(sql);
            int isDeleted = ps.executeUpdate();
            return isDeleted == 1 ? true : false;
        }
    }
    public boolean clear() throws SQLException {
        synchronized (this) {
            String sql = String.format("DELETE FROM %s", _tableName.get());
            PreparedStatement ps = _connection.get().prepareStatement(sql);
            int isDeleted = ps.executeUpdate();
            return isDeleted == 1 ? true : false;
        }
    }
    public boolean deleteLast() throws SQLException {
        return deleteById(countRows());

    }

    public int countRows() throws SQLException {
        synchronized (this) {
            String sql = String.format("SELECT COUNT (*) AS totalCount from %s", _tableName.get());
            PreparedStatement ps = _connection.get().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int val = -1;
            if(rs.next()) {
                val = rs.getInt("totalCount");
            }
            return val;
        }
    }
    private String getTableName() {
        String tableName = null;
        for(Field field: _currentClass.get().getDeclaredFields()) {
            DBFieldTableName dbTableNameField = field.getAnnotation(DBFieldTableName.class);
            if(dbTableNameField != null){
                tableName = dbTableNameField.table_name();
                break;
            }
        }
        if(tableName == null)
            tableName = _currentClass.get().getSimpleName().toLowerCase();

        return tableName;
    }

}
