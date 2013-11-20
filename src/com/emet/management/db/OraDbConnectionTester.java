package com.emet.management.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emet.management.common.IUi;
import com.google.gson.Gson;

/**
 * 
 * @author Oleg B
 * 
 */

//TODO Oleg: 1. OraDbConnectionTester, 2. statement + select from dual 

public class OraDbConnectionTester implements IUi {
	
	Logger logger = LoggerFactory.getLogger(OraDbConnectionTester.class);
	
	public String checkConnection(String fileNameIn) {
		Connection con;
		Gson gsonin = new Gson();
		OraJdbcDTO jdbcDto = new OraJdbcDTO();
		logger.trace("===>>> in");
		try {

			BufferedReader br = new BufferedReader(new FileReader(fileNameIn));
			jdbcDto = gsonin.fromJson(br, OraJdbcDTO.class);
			Class.forName("oracle.jdbc.OracleDriver");
			String url = "jdbc:oracle:thin:@" + jdbcDto.host + ":" + jdbcDto.port
					+ ":" + jdbcDto.sid;
			con = DriverManager.getConnection(url, jdbcDto.user, jdbcDto.password);
			con.close();
			logger.trace(done);
		}

		catch (Exception e) {
			logger.error(errorTag + e.getLocalizedMessage());
			return errorTag + e.getLocalizedMessage();
		}

		logger.trace("<<<=== Out");
		System.err.println( done );
		return done;
	}

	public static void main(String[] args) {
		OraDbConnectionTester c = new OraDbConnectionTester();
		c.checkConnection(args[0]);
	}

}
