//package name here

package com.example.nzar.toyotarfid;

import java.io.UnsupportedEncodingException;
import java.sql.*;

/**
 * Created by cravers on 6/29/2017.
 */

public class DatabaseConnector {

    private static final String TAG = "DBConnectorLib"; //set Logging tag

    /*
        These private strings are the settings used to connect the database, they all have public
        setters to make sure they can be over-written, but can never be read to prevent unauthorized
        database access.
     */
    private static String DatabaseRoot = "jdbc:mysql://192.168.0.200:3306/dummyemployee";
    private static String DatabaseUser = "OXYSMakerSafe";
    private static String DatabasePasswd = "1234";
    private static String authorizationProceedure = null;
    private static String loggingProceedure = null;
    private static String machineType = null;
    private static String[] Schemas = {"dummyemployee"};
    private static String EmployeeTable = "employeeinfo";
    private static String LoggingTable = "";
    private static String TechTable = "";

    private enum Engine {mySQL, PostGreSQL, SQLServer, ODBC, MariaDB, Oracle, Auto}

    private static Engine engine = null;

    //Employee class is to store the information about the employee gathered from the database to minimize database hits
    public static class Employee {
        String FirstName;
        String LastName;
        String ID;
        int training;

    }

    private static Employee currentEmployee;

    private static void setCurrentEmployee(Employee currentEmployee) {
        DatabaseConnector.currentEmployee = currentEmployee;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public static void setEngine(Engine engine) {
        DatabaseConnector.engine = engine;
    }

    public static void setAuthorizationProceedure(String authorizationProceedure) {
        DatabaseConnector.authorizationProceedure = authorizationProceedure;
    }

    public static void setDatabasePasswd(String databasePasswd) {
        DatabasePasswd = databasePasswd;
    }

    public static void setDatabaseRoot(String databaseRoot) {
        DatabaseRoot = databaseRoot;
    }

    public static void setDatabaseUser(String databaseUser) {
        DatabaseUser = databaseUser;
    }

    public static void setEmployeeTable(String employeeTable) {
        EmployeeTable = employeeTable;
    }

    public static void setLoggingProceedure(String loggingProceedure) {
        DatabaseConnector.loggingProceedure = loggingProceedure;
    }

    public static void setLoggingTable(String loggingTable) {
        LoggingTable = loggingTable;
    }

    public static void setMachineType(String machineType) {
        DatabaseConnector.machineType = machineType;
    }

    public static void setSchemas(String[] schemas) {
        Schemas = schemas;
    }

    public static void setTechTable(String techTable) {
        TechTable = techTable;
    }


    /*
        This method will query the database specified to see if the user that just badged in is
        authorized to use the device. It will also set up the currentEmployee to the row obtained.
     */
    static boolean EmployeeAuthorized(String badgeNumber) throws SQLException, ClassNotFoundException, UnsupportedEncodingException {

        Class.forName("com.mysql.jdbc.Driver");


        try (Connection connection = DriverManager.getConnection(DatabaseRoot, DatabaseUser, DatabasePasswd)) {
            DriverManager.registerDriver(DriverManager.getDriver(DatabaseRoot));
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM employeeinfo WHERE id='" + badgeNumber + "';");
            results.next();

            DatabaseConnector.Employee employee = new DatabaseConnector.Employee();

            employee.FirstName = results.getString(1);
            employee.ID = results.getString(2);
            employee.training = results.getInt(3);

            DatabaseConnector.setCurrentEmployee(employee);

            if (results.getInt(3) == 1) {
                connection.close();
                return true;
            } else {
                connection.close();
                return false;
            }
        }


    }

    public static void LogDeviceActivity() {

    }

    public static void getDeviceType() {

    }

    public static void getTechContact() {

    }


}