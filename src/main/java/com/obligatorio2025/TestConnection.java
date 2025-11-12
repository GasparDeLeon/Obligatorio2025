package com.obligatorio2025;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/obligatorio2025?useSSL=false&serverTimezone=UTC";
        String usuario = "root";
        String contra = "";

        try {
            Connection connection = DriverManager.getConnection(url, usuario, contra);
            System.out.println("✅ Conexión exitosa!");
            connection.close();
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar: " + e.getMessage());
        }
    }
}
