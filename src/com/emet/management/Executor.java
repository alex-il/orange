package com.emet.management;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: Description: Copyright: Copyright (c) E&M (Israel) Ltd. All Rights
 * Reserved.
 * 
 * @author Oleg Branopolsky
 * @version 1.0
 */
public class Executor {
	static final Logger logger = LoggerFactory.getLogger(Executor.class);
	public static void main(String[] args) {
		execute("d:\\Artifacts\\tbmsadmin.bat");
	}

	public static String execute(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			InputStream in = process.getInputStream();
			InputStream err = process.getErrorStream();
			SequenceInputStream sis = new SequenceInputStream(in, err);
			BufferedReader br = new BufferedReader(new InputStreamReader(sis));
			String line;
			while ((line = br.readLine()) != null) {
				logger.debug(line); 
				if(line.toLowerCase().contains("error")){
					logger.error(line);
				}
			}
			int errorCode = process.waitFor();
			if (errorCode == 0) {
				logger.info("Success");
				return "Executor.execute(): Succsess";
			} else {
				logger.error("Error. Exit code: " + errorCode);
				return String.format("Executor.execute(): error.code:%d", errorCode);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			return e.getLocalizedMessage();
		}
	}
}