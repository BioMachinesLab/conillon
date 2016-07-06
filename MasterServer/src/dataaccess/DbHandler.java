package dataaccess;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.PropertiesHandler;

public class DbHandler {

	public static void UpdateWorkerSession(dataaccess.dataobjects.Worker dbWorker) {
		Connection conn = null;
		CallableStatement cStmt = null;
		Boolean result;
		try {
			Class.forName(JDBC_DRIVER);

			PropertiesHandler ph = PropertiesHandler.getInstance();

			String dbUrl = ph.getPropValue("db_url"), dbUserName = ph.getPropValue("db_user_name"),
					dbUserPass = ph.getPropValue("db_user_pass");

			conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPass);

			cStmt = conn.prepareCall(String.format("{CALL PCNLWKS00uAct_UpdateWorkerSession (%d,%d,%d,%d,%s,null)}",
					dbWorker.getId(), dbWorker.getNumberOfTasksProcessed(), // tasks
																			// processed
					dbWorker.getAverageTime(), // average Time (long)
					dbWorker.getTotal_running_time(), // running time
					dbWorker.getFormattedAverageSpeed()// , //speed
			// 10.101 //Speed by core
			));
			result = cStmt.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (cStmt != null)
					cStmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				//se.printStackTrace();
			}
		}
	}

	public static int InsertNewWorker(dataaccess.dataobjects.Worker dbWorker) {

		Connection conn = null;
		CallableStatement cStmt = null;
		ResultSet result;
		int insertedOid = 0;
		try {
			Class.forName(JDBC_DRIVER);

			PropertiesHandler ph = PropertiesHandler.getInstance();
			String dbUrl = ph.getPropValue("db_url"), dbUserName = ph.getPropValue("db_user_name"),
					dbUserPass = ph.getPropValue("db_user_pass");

			conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPass);

			cStmt = conn.prepareCall(
					String.format("{CALL PCNLWRK00iIns_NewWorker (\"%s\", \"%s\",\"%s\", %d, \"%s\", %d, %d, %d)}",
							dbWorker.getIp(), 
							dbWorker.getMac_address(), 
							dbWorker.getHost_name(),
							dbWorker.getNum_cores(), 
							dbWorker.getOperative_system(),
							dbWorker.getPerformance(),
							0, // id																						//room
							0)); // is banned

			result = cStmt.executeQuery();

			while (result.next()) {
				insertedOid = result.getInt("Id");
				dbWorker.setId(insertedOid);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (cStmt != null)
					cStmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				//se.printStackTrace();
			}
		}

		return insertedOid;
	}

	public static void InsertNewClient(dataaccess.dataobjects.Client dbClient) {

		Connection conn = null;
		CallableStatement cStmt = null;
		Boolean result;
		try {
			Class.forName(JDBC_DRIVER);

			PropertiesHandler ph = PropertiesHandler.getInstance();
			String dbUrl = ph.getPropValue("db_url"), dbUserName = ph.getPropValue("db_user_name"),
					dbUserPass = ph.getPropValue("db_user_pass");

			conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPass);

			cStmt = conn.prepareCall(String.format("{CALL PCNLCLT00iIns_NewClient (%d, \"%s\", \"%s\",\"%s\")}",
					dbClient.getId(), dbClient.getIp(), dbClient.getMac_address(), dbClient.getHost_name()));

			result = cStmt.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (cStmt != null)
					cStmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				//se.printStackTrace();
			}
		}
	}

	public static void UpdateClient(dataaccess.dataobjects.Client dbClient) {
		Connection conn = null;
		CallableStatement cStmt = null;
		Boolean result;
		try {
			Class.forName(JDBC_DRIVER);

			PropertiesHandler ph = PropertiesHandler.getInstance();
			String dbUrl = ph.getPropValue("db_url"), dbUserName = ph.getPropValue("db_user_name"),
					dbUserPass = ph.getPropValue("db_user_pass");

			conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPass);
			// PCNLCLT00uAct_UpdateClient
			cStmt = conn.prepareCall(String.format("{CALL PCNLCLS00uAct_UpdateClientSession (%d,\"%d\", %s, %d, %d)}",
					dbClient.getId(), dbClient.getAverage_time(), // average
																	// Time
																	// (long)
					dbClient.getFormattedAverageSpeed(), // average speed (double)
					dbClient.getTotal_running_time(), // total running time
														// (long)
					dbClient.getTotal_tasks()));

			result = cStmt.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		// finally block used to close resources
		try {
			if (cStmt != null)
				cStmt.close();
		} catch (SQLException se2) {
		} // nothing we can do
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException se) {
			//se.printStackTrace();
		}
	}
	}
	
	
	public static List<dataaccess.dataobjects.Worker> getWorkersStatsOrderedByPerformanceDesc() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT WRS.oid AS id, WRK.ip, WRK.host_name, WRK.num_cores, WRS.performance ")
		.append("FROM VCNLWRK01_Worker WRK ")
		.append("INNER JOIN VCNLWKS01_WorkerSession WRS ON WRK.oid = WRS.oidWorker AND WRS.stop_time_stamp IS NULL ")
		.append("WHERE WRK.is_banned <> 1 ")
		.append("ORDER BY performance DESC");
		
		List<Map<String, Object>> table = executeQuery(query.toString());
		if (table == null) {
			return null;
		}
		
		List<dataaccess.dataobjects.Worker> workersDO = new ArrayList<>(table.size());
		dataaccess.dataobjects.Worker workerDO;
		for (Map<String, Object> row : table) {
			workerDO = new dataaccess.dataobjects.Worker(row);
			workersDO.add(workerDO);
		}
		
		return workersDO;
	}
	
	public static double convertBigDecimalToDouble(BigDecimal value) {
		return value.doubleValue();
	}
	public static long convertBigDecimalToLong(BigDecimal value) {
		return value.setScale(0, BigDecimal.ROUND_UP).longValueExact();
	}
	public static  int convertBigDecimalToInt(BigDecimal value) {
		return value.setScale(0, BigDecimal.ROUND_UP).intValueExact();
	}
	
	
	private static List<Map<String, Object>> executeQuery(String query) {

		List<Map<String, Object>> table = null;
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			//System.out.println("Connecting to database...");
			PropertiesHandler ph = PropertiesHandler.getInstance();
			String dbUrl = ph.getPropValue("db_url"), dbUserName = ph.getPropValue("db_user_name"),
					dbUserPass = ph.getPropValue("db_user_pass");
			conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPass);

			// STEP 4: Execute a query
			//System.out.println("Creating statement...");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();
			table = new ArrayList<>();
			Map<String, Object> row = null;
			
			while (rs.next()){
				row = new HashMap<>(columns);
				for(int i=1; i<=columns; ++i){           
					row.put(md.getColumnName(i),rs.getObject(i));
			    }
				table.add(row);
			}
			
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();

		} catch (SQLException se) {
			se.printStackTrace();
			// Handle errors for JDBC

			System.out.println(se.getMessage());
		} catch (Exception e) {
			e.getMessage();
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				//se.printStackTrace();
			}
		}
		return table;
	}
	
	public static void GetToDatabase() {

		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			PropertiesHandler ph = PropertiesHandler.getInstance();
			String dbUrl = ph.getPropValue("db_url"), dbUserName = ph.getPropValue("db_user_name"),
					dbUserPass = ph.getPropValue("db_user_pass");
			conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPass);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "select oid, id, ip, mac_address, host_name, num_cores, operative_system, average_time, average_speed, total_running_time, id_room, is_banned FROM VCNLWRK00_Worker";
			ResultSet rs = stmt.executeQuery(sql);

			dataaccess.dataobjects.Worker workerDataObject = null;

			// STEP 5: Extract data from result set
			while (rs.next()) {
				workerDataObject = new dataaccess.dataobjects.Worker();
				// Retrieve by column name
				workerDataObject.setId(rs.getInt("id"));
				workerDataObject.setIp(rs.getString("ip"));
				workerDataObject.setMac_address(rs.getString("mac_address"));
				workerDataObject.setHost_name(rs.getString("host_name"));
				workerDataObject.setNum_cores(rs.getInt("num_cores"));
				workerDataObject.setOperative_system(rs.getString("operative_system"));
				workerDataObject.setAverageTime(rs.getLong("average_time"));
				workerDataObject.setAverageSpeed(rs.getFloat("average_speed"));
				workerDataObject.setTotal_running_time(rs.getLong("total_running_time"));
				workerDataObject.setId_room(rs.getInt("id_room"));
				workerDataObject.setIsBanned(rs.getBoolean("is_banned"));
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();

		} catch (SQLException se) {
			se.printStackTrace();
			// Handle errors for JDBC

			System.out.println(se.getMessage());
		} catch (Exception e) {
			e.getMessage();
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				//se.printStackTrace();
			}
		}
	}

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	// static final String DB_URL = //"jdbc:mysql://localhost/dsConillon010";
	// "jdbc:mysql://localhost/dsConillon010?noAccessToProcedureBodies=true";

	// Database credentials
	// static final String USER = "conillon_user";
	// static final String PASS = "C0n1770n";
	;

}
