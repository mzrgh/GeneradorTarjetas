package es.alcampo.utilsCLM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, SQLException {
		PrintStream fichero = new PrintStream(new File("log.txt"));
		System.setOut(fichero);

		String serie = "ALCAMPO"; // --> esto tiene que venir por parámetro A
		int totalTarjetas = 1000; // --> esto tiene que venir por parámetro B
		String raiz = ""; // Se determina a partir del parámetro A

		switch (serie) {
		case "ALCAMPO":
			raiz = "4002";
			break;
		case "ALCAMPO_D":
			raiz = "4003";
			break;
		case "ALCAMPO_O":
			raiz = "4004";
			break;
		default:
			raiz = "Invalid";
			break;
		}

		int totalIntentos = 0;

		if (raiz == "Invalid") {
			System.out.println("*** NO COPIAR ***** Raíz Inválida. NO se generan tarjetas");
		} else {
            Connection connection = null;
			  try {
		            // Registramos el driver de MySQL 
		            try {
		                Class.forName("com.mysql.cj.jdbc.Driver");
		            } catch (ClassNotFoundException ex) {
		                System.out.println("Error al registrar el driver de MySQL: " + ex);
		            }

		            // Database connect
		            connection = DriverManager.getConnection(
		                    "jdbc:mysql://10.70.1.242:3307/tarjetas_clm",
		                    "root", "manager");
		            boolean valid = connection.isValid(50000);
		            System.out.println(valid ? "TEST OK" : "TEST FAIL");
		        } catch (java.sql.SQLException sqle) {
		            System.out.println("Error: " + sqle);
		        }
			
/*			
			DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
			String urlConnection = "jdbc:mysql://10.70.1.242:3307/tarjetas_dm";
			Connection connection = DriverManager.getConnection(urlConnection, "root", "manager"); */

			int totalTarjetasG = 0;
			System.out.println("HEADER;20180727");
			System.out.println("IDENTIFIER_SERIES;;" + serie);
			while (totalTarjetasG <= totalTarjetas) {
				String tarjetaG = generarCodigo13(raiz); // obtener Tarjeta
				if (buscarTarjeta(connection, tarjetaG)) {
					grabarTarjetaBD(connection, tarjetaG); // grabar codigo en BBDD
					System.out.println(tarjetaG); // grabar codigo en fichero de salida
					totalTarjetasG++;
				}
				totalIntentos++;
			}
			System.out.println("FOOTER;" + (totalTarjetasG + 1));
			System.out.println("*** NO COPIAR ***** Total Intentos =" + (totalIntentos));
		}
	}

	private static String generarCodigo13(String raiz) {
		final int VALOR_FIJO = 10000000;
		String codigo12 = "";
		String codigo13 = "";
		int contador = (int) Math.floor(Math.random() * (VALOR_FIJO));
		codigo12 = raiz + agregarCeros(Integer.toString(contador));
		int[] numeros = codigo12.chars().map(Character::getNumericValue).toArray();
		int somaPares = numeros[1] + numeros[3] + numeros[5] + numeros[7] + numeros[9] + numeros[11];
		int somaImpares = numeros[0] + numeros[2] + numeros[4] + numeros[6] + numeros[8] + numeros[10];
		int resultado = somaImpares + somaPares * 3;
		int digitoVerificador = 10 - resultado % 10;
		if (digitoVerificador == 10)
			digitoVerificador = 0;
		codigo13 = codigo12 + Integer.toString(digitoVerificador);
		return codigo13;
	}

	private static String agregarCeros(String string) {
		String ceros = "";
		int largo = 8;
		int cantidad = largo - string.length();
		if (cantidad >= 1) {
			for (int i = 0; i < cantidad; i++) {
				ceros += "0";
			}
			return (ceros + string);
		} else
			return string;
	}

	public static boolean buscarTarjeta(Connection connection, String tarjetaG) throws SQLException {

		String sql;
		Statement s;

		s = connection.createStatement();
		sql = "select * from tarjetasGeneradas where num_Tarjeta=" + tarjetaG;

		ResultSet rs = s.executeQuery(sql);

		return rs.last();
	}

	public static void grabarTarjetaBD(Connection connection, String tarjetaG) throws SQLException {

		String sql;
		Statement s;

		s = connection.createStatement();
		sql = "INSERT INTO tarjetasGeneradas (num_Tarjeta, fecha_Generacion) VALUES ('" + tarjetaG
				+ "' , CURRENT_DATE());";

		s.executeQuery(sql);

	}
}