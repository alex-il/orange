package com.emet.management.db.paramsreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.collection.parallel.ParIterableLike.Forall;
import scala.util.control.Exception.Finally;

import com.emet.management.common.IUi;
import com.emet.management.db.OraJdbcDTO;
import com.google.gson.Gson;

/**
 * 
 * @author Yuri S
 * 
 */
public class ProcedureParamsCaller implements IUi {
	final static Logger logger = LoggerFactory
			.getLogger(ProcedureParamsCaller.class);

	private OraJdbcDTO oraJdbcDTO;
	private int beginLineIndex = 1;

	// TODO Oleg to fix performance, join yo check connection code
	/**
	 * 
	 * Create connection for parameters received from GSON file
	 * 
	 * @return - Connection opened for ORACLE database
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private Connection createConnection() throws FileNotFoundException,
	ClassNotFoundException, SQLException {
		Connection con = null;

		Class.forName("oracle.jdbc.OracleDriver");
		String url = "jdbc:oracle:thin:@" + oraJdbcDTO.host + ":"
				+ oraJdbcDTO.port + ":" + oraJdbcDTO.sid;

		logger.debug(url + "," + oraJdbcDTO.user + "/" + oraJdbcDTO.password);

		con = DriverManager.getConnection(url, oraJdbcDTO.user,
				oraJdbcDTO.password);
		return con;
	}

	/**
	 * 
	 * Populate DTO variables from GSON file with name <fileNameIn>
	 * 
	 * @param fileNameIn
	 *            - path to GSON file
	 * @throws FileNotFoundException
	 */
	public void populateCallerParamDTO(String fileNameIn)
			throws FileNotFoundException {
		Gson gsonin = new Gson();
		oraJdbcDTO = new OraJdbcDTO();

		BufferedReader br = new BufferedReader(new FileReader(fileNameIn));
		oraJdbcDTO = gsonin.fromJson(br, OraJdbcDTO.class);
		int pointPosition = oraJdbcDTO.funcName.indexOf(".");
		if (pointPosition >= 0) {
			oraJdbcDTO.packageName = oraJdbcDTO.funcName.substring(0,
					pointPosition);
			oraJdbcDTO.funcName = oraJdbcDTO.funcName
					.substring(pointPosition + 1);
		}
	}

	/**
	 * 
	 * Build select with logic ALL_SOURCE where's PL/SQL code of Function
	 * 
	 * @return Select text
	 */
	private String buildSelect() {
		String select = "select all_source.line, ' '||replace(all_source.TEXT,chr(9),' ')|| ' ' line from all_source where 2=2 ";
		String ownerCondition = "";
		String ownerInnerCondition = "";
		String beginProcedureCondition = "";

		if (!oraJdbcDTO.dbSchema.equals("")) {
			ownerCondition = " and all_source.OWNER = ? ";
			ownerInnerCondition = " and all_source1.OWNER = ? ";
		}
		if (!oraJdbcDTO.packageName.equals("")) {
			// Procedure in package
			beginProcedureCondition = "and all_source.name = ?\n"
					+ "and all_source.TYPE = 'PACKAGE'\n"
					+ "and all_source.line >=\n"
					+ "    (select min(all_source1.line)\n"
					+ "       from all_source all_source1 where 2=2 "
					+ ownerInnerCondition
					+ "        and all_source1.name = ?\n"
					+ "        and all_source1.TYPE = 'PACKAGE'\n"
					+ "        and instr(upper(all_source1.TEXT), ?) > 0)";
			;
		} else {
			// Single procedure or function
			beginProcedureCondition = "and all_source.name = ?\n"
					+ "and all_source.TYPE in ('PROCEDURE','FUNCTION')";
		}
		select = select + beginProcedureCondition + " and all_source.line >= ?"
				+ ownerCondition + " order by all_source.line";

		return select;
	}

	/**
	 * parse function specification and populate parameters list
	 * 
	 * @param spec
	 *            Specification string Specification syntax: *
	 *            [PROCEDURE|FUNCTION] <Unit Name> ( <param_name1> {in|out|in
	 *            out} <data type> {:=|default <value>} ) [return <datatype>]
	 * @return Vector with parameters list
	 * @throws SpecSyntaxException
	 */
	public Vector<ProcedureParameter> parseUnitSpecification(String spec)
			throws SpecSyntaxException {
		Vector<ProcedureParameter> list = new Vector<ProcedureParameter>();
		String token;
		String functionNameToken;
		String paramsToken;
		String unitType;
		// Specification syntax: * [PROCEDURE|FUNCTION] <Unit Name> (
		// <param_name1> {in|out|in out} <data type> {:=|default <value>} )
		// [return <datatype>]
		if (spec.equals("")) {
			throw new SpecSyntaxException(Messages.FUNCTION_NOT_FOUND, spec);
		}
		StringTokenizer stringTokenizer = new StringTokenizer(spec, "(");
		if (stringTokenizer.countTokens() != 2) {
			throw new SpecSyntaxException(Messages.SPECIFICATION_ERROR, spec);
		}
		functionNameToken = (String) stringTokenizer.nextElement();
		paramsToken = (String) stringTokenizer.nextElement();

		stringTokenizer = new StringTokenizer(functionNameToken);
		if (stringTokenizer.countTokens() != 2) {
			throw new SpecSyntaxException(Messages.FUNCTION_CLAUSE_ERROR, spec);
		}
		unitType = (String) stringTokenizer.nextElement();
		if (!unitType.toUpperCase().equals("FUNCTION")) {
			throw new SpecSyntaxException(Messages.ONLY_FUNCTION_SUPPORTED,
					spec);
		}

		token = (String) stringTokenizer.nextElement();
		if (!token.toUpperCase().equals(oraJdbcDTO.funcName.toUpperCase())) {
			throw new SpecSyntaxException(Messages.FUNCTION_NAME_ERROR, spec);
		}

		stringTokenizer = new StringTokenizer(paramsToken, ")");
		paramsToken = (String) stringTokenizer.nextElement();

		stringTokenizer = new StringTokenizer(paramsToken, ",");
		while (stringTokenizer.hasMoreElements()) {
			ProcedureParameter procedureParameter = new ProcedureParameter();
			token = (String) stringTokenizer.nextElement(); // param clause

			StringTokenizer stringParamTokenizer = new StringTokenizer(token);
			procedureParameter.name = ((String) stringParamTokenizer
					.nextElement()).toUpperCase();
			procedureParameter.inout = ((String) stringParamTokenizer
					.nextElement()).toUpperCase();
			if (procedureParameter.inout.equals("NOCOPY")) {
				procedureParameter.inout = ((String) stringParamTokenizer
						.nextElement()).toUpperCase();
			}
			if (!procedureParameter.inout.equals("IN")
					&& !procedureParameter.inout.equals("OUT")) {
				procedureParameter.dataType = procedureParameter.inout;
				procedureParameter.inout = "IN";
			} else {
				procedureParameter.dataType = ((String) stringParamTokenizer
						.nextElement()).toUpperCase();
				if (procedureParameter.dataType.equals("OUT")) {
					procedureParameter.inout = "INOUT";
					procedureParameter.dataType = ((String) stringParamTokenizer
							.nextElement()).toUpperCase();
				}
			}
			if (stringParamTokenizer.hasMoreElements()) {
				token = ((String) stringParamTokenizer.nextElement())
						.toUpperCase(); // pass next token [:= | DEFAULT]
				procedureParameter.defaultvalue = (String) stringParamTokenizer
						.nextElement();
			} else {
				procedureParameter.defaultvalue = "NO_REQUIRED";
			}
			if (!procedureParameter.inout.equals("IN")) {
				throw new SpecSyntaxException(Messages.OUT_PARAMETER_ERROR,
						spec);
			}
			if (!procedureParameter.dataType.equals("NUMBER")
					&& !procedureParameter.dataType.equals("CLOB")
					&& !procedureParameter.dataType.equals("DATE")
					&& !procedureParameter.dataType.equals("INTEGER")) {
				procedureParameter.dataType = "CHAR";
			}

			list.add(procedureParameter);
		}

		return list;
	}

	/**
	 * 
	 * Get function specification string from ALL_SOURCE oracle view where's
	 * pl/sql code by function name and owner
	 * 
	 * @param con
	 *            Connection again to Oracle DB
	 * @param lineIndex
	 *            line number from begin search
	 * @return Function specification string Specification syntax: *
	 *         [PROCEDURE|FUNCTION] <Unit Name> ( <param_name1> {in|out|in out}
	 *         <data type> {:=|default <value>} ) [return <datatype>]
	 * @throws SQLException
	 */
	private String getUnitSpecification(Connection con) throws SQLException {
		PreparedStatement stmt;
		ResultSet resultSet;
		//		String line, unitSpecification = "";
		String line;
		StringBuffer unitSpecification = new StringBuffer(500);
		String select = buildSelect();
		boolean isPackageUnit = !oraJdbcDTO.packageName.equals("");

		stmt = con.prepareStatement(select);
		if (isPackageUnit && !oraJdbcDTO.dbSchema.equals("")) {
			stmt.setString(1, oraJdbcDTO.packageName);
			stmt.setString(2, oraJdbcDTO.dbSchema);
			stmt.setString(3, oraJdbcDTO.packageName);
			stmt.setString(4, oraJdbcDTO.funcName);
			stmt.setInt(5, beginLineIndex);
			stmt.setString(6, oraJdbcDTO.dbSchema);
		} else if (isPackageUnit) {
			stmt.setString(1, oraJdbcDTO.packageName);
			stmt.setString(2, oraJdbcDTO.packageName);
			stmt.setString(3, oraJdbcDTO.funcName);
			stmt.setInt(4, beginLineIndex);
		} else if (!oraJdbcDTO.dbSchema.equals("")) {
			stmt.setString(1, oraJdbcDTO.funcName);
			stmt.setString(2, oraJdbcDTO.dbSchema);
		} else {
			stmt.setString(1, oraJdbcDTO.funcName);
		}

		resultSet = stmt.executeQuery();
		while (resultSet.next()) {
			line = resultSet.getString(2);
			unitSpecification.append( line);
			if (isPackageUnit
					&& line.indexOf(';') != -1
					|| (line.toUpperCase().indexOf(" IS ") != -1 || line
					.toUpperCase().indexOf(" AS ") != -1)) {
				beginLineIndex = resultSet.getInt(1) + 1;
				break;
			}
		}
		resultSet.close();
		stmt.close();

		return unitSpecification.toString();
	}

	/**
	 * 
	 * Output parameters list to standard system output for future processing
	 * standard system output wrote to log file and IF ADMIN UI form can read
	 * and present in UI
	 * 
	 * 
	 * @param list
	 *            Parameter list
	 */
	public void printParameterListToOut(Vector<ProcedureParameter> list) {
		final char delimiter = ':';
		for (ProcedureParameter procedureParameter : list) {
			System.out.println(paramTag + procedureParameter.name + delimiter
					+ procedureParameter.dataType + delimiter
					+ procedureParameter.defaultvalue);
		}


		//		Enumeration<ProcedureParameter> en = list.elements();
		//		while (en.hasMoreElements()) {
		//			ProcedureParameter procedureParameter = (ProcedureParameter) en
		//					.nextElement();
		//			System.out.println(paramTag + procedureParameter.name + delimiter
		//					+ procedureParameter.dataType + delimiter
		//					+ procedureParameter.defaultvalue);
		//		}
	}

	public static void main(String[] args) {
		Connection con;
		//		String unitSpecification = "";
		String unitSpecification;
		ProcedureParamsCaller procParamsCaller = new ProcedureParamsCaller();
		Vector<ProcedureParameter> parseUnitSpecification = null;
		try {

			procParamsCaller.populateCallerParamDTO(args[0]);

			con = procParamsCaller.createConnection();
			do {
				unitSpecification = procParamsCaller.getUnitSpecification(con);
				try {
					parseUnitSpecification = procParamsCaller.parseUnitSpecification(unitSpecification);
					break;
				} catch (SpecSyntaxException e) {
					if (!e.getMessage().equals(msgTag + Messages.FUNCTION_NAME_ERROR))
					{
						con.close();
						throw e;
					}
				}
			} while (true);
			procParamsCaller.printParameterListToOut(parseUnitSpecification);				
			con.close();
			System.out.println(done);
		} catch (Exception e) {
			System.out.println(errorTag + e.getLocalizedMessage());
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		} 

	}
}
