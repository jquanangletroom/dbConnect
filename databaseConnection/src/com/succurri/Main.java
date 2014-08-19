package com.succurri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Main {

	// example code
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.microsoft.sqlserver.SQLServerDriver";
	static final String DB_URL = "<databse connection";

	// Database credentials
	static final String USER = "<username>";
	static final String PASS = "<password>";

	public static void main(String[] args) {
		//storing the values from the database
		ArrayList<String> list = new ArrayList<String>();
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
//			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT Company_Name FROM Company";
			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				String company = rs.getString("Company_Name");

				// Display values		
//				System.out.println("Company Name: " + company);
				
				//store values in arraylist
				list.add(company);
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		System.out.println("Goodbye!");
		System.out.println("going through the list to see if all the stuff is stored");
		
		int count = 0 ;
		for(String object: list){
			count ++;
			System.out.println(count + ": " + object);
		}//for
	}// end main
	
	private static void connectKaseya(){
		
	}//connectKaseya()

}//class