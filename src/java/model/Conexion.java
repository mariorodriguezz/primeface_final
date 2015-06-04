package model;

import java.sql.*;

public class Conexion {

    public Conexion() {
        
    }
    
    public Connection conectar() throws SQLException, ClassNotFoundException {
        Connection conexion = null;
        Class.forName("org.postgresql.Driver");
        conexion = DriverManager.getConnection("jdbc:postgresql://localhost:5432/tvshop", "postgres", "123");
        return conexion;
    }
    
    public void desconectar(Connection conexion) throws SQLException {
        conexion.close();
    }
    
    public void rollback(Connection conexion) throws SQLException, ClassNotFoundException{
        conexion.rollback();
    }
}