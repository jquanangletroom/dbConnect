package com.succurri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {

	// example code
	// JDBC driver name and database URL
	//private static final String JDBC_DRIVER = "com.microsoft.sqlserver.SQLServerDriver"; //never use this
	private static final String DB_URL_CONNECTWISE = "";
	private static final String DB_URL_KASEYA = "";
	private static final String DB_URL_TRAINING = "";
	

	// Database credentials
	static final String USER = "";
	static final String PASS = "";

	public static void main(String[] args) {
		ArrayList<String>[] tempList = connectKaseya();
		ArrayList<String> orgName = tempList[0];
		ArrayList<String> orgID = tempList[1];

		// [0][] = servers, [1][] = workstations, and the columns [][x]
		// correspond to orgID.get(X)
		int[][] matrix = countMachines(orgID);
		for (int i = 0; i < orgName.size(); i++) {
			System.out.print("#" + i + " " + orgName.get(i) + ": "
					+ orgID.get(i) + " :: ");
			for (int j = 0; j < 2; j++) {
				if (j == 0) {
					System.out.print("Servers = " + matrix[j][i] + " ");
				} else {
					System.out.println("Workstations = " + matrix[j][i]);
				}// else
			}// for
		}// for
	}// end main

	private static ArrayList<String>[] connectKaseya() {
		Connection conn = null;
		Statement stmt = null;
		ArrayList<String> orgName = new ArrayList<String>();
		ArrayList<String> orgID = new ArrayList<String>();
		try {
			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL_KASEYA, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT orgName, ref FROM kasadmin.org ORDER BY orgName asc";
			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				orgName.add(rs.getString("orgName"));
				orgID.add(rs.getString("ref"));
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
		return new ArrayList[] { orgName, orgID };
	}// connectKaseya()

	private static ArrayList<String>[] connectConnectwise() {
		Connection conn = null;
		Statement stmt = null;
		ArrayList<String> companyName = new ArrayList<String>();
		try {
			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL_CONNECTWISE, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			// Connectwise
			sql = "SELECT Company_Name FROM Company";
			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				companyName.add(rs.getString("Company_Name"));
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
		return new ArrayList[] { companyName };
	}// connectConnectwise()

	private static int[][] countMachines(ArrayList<String> companyID) {
		Connection conn = null;
		Statement stmt = null;
		int[] numServers = new int[companyID.size()];
		int[] numWorkstations = new int[companyID.size()];

		// string array for if we are finding servers or workstations
		String[] getServOrWS = { "LIKE", "NOT LIKE" };
		try {
			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL_KASEYA, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statements...");
			for (int i = 0; i < companyID.size(); i++) {
				for (int j = 0; j < 2; j++) {
					stmt = conn.createStatement();
					String sql;
					sql = "SELECT COUNT(Machine_GroupID) AS thecount FROM vMachine WHERE Machine_GroupID like '%"
							+ companyID.get(i)
							+ "%' AND OsInfo "
							+ getServOrWS[j] + " '%server%'";
					ResultSet rs = stmt.executeQuery(sql);

					// STEP 5: Extract data from result set
					while (rs.next()) {
						if (j == 0) {
							numServers[i] = rs.getInt("thecount");
						} else {
							numWorkstations[i] = rs.getInt("thecount");
						}// else
					}// while
						// STEP 6: Clean-up environment
					rs.close();
					stmt.close();
				}// for
			}// for
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
		return new int[][] { numServers, numWorkstations };
	}// countMachines(ArrayList<String>)

	private static void updateQuantityCW(int[][] counts,
			ArrayList<String> companyID) {
		Connection conn = null;
		Statement stmt = null;

		// getting the current date in YYYYMMDD format
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = sdf.format(new Date());

		String[] serverVsWS = { "3", "2" };
		try {
			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL_CONNECTWISE, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statements...");
			for (int i = 0; i < companyID.size(); i++) {
				for (int j = 0; j < 2; j++) {
					stmt = conn.createStatement();
					String sql = "UPDATE AGR_Detail SET agr_detail.AGD_Qty = '"
							+ counts[j][i]
							+ "' FROM "
							+ "((AGR_Header inner join AGR_Detail "
							+ "ON AGR_header.AGR_Header_RecID = AGR_Detail.AGR_Header_RecID) inner join Company "
							+ "ON AGR_Header.Company_RecID = Company.Company_RecID) "
							+ "WHERE AGR_Type_RecID = '31' AND (AGD_Cancel IS NULL OR AGD_Cancel >= CONVERT(datetime, '"
							+ date
							+ "')) AND Company_ID = '"
							+ companyID.get(i)
							+ "' AND (AGR_Date_End IS NULL OR AGR_Date_End >= CONVERT(datetime, '"
							+ date + "')) AND IV_Item_RecID = '";
					if (j == 0) {
						// server
						 sql += serverVsWS[j] + "'";
					} else {
						// workstations
						sql += serverVsWS[j] +"'";
					}// else
					stmt.executeQuery(sql);
					stmt.close();
				}// for
			}// for
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
	}// updateQuantityCW(int[][],ArrayList<String>)
}// class