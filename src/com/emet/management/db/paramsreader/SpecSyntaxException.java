package com.emet.management.db.paramsreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emet.management.common.IUi;

public class SpecSyntaxException extends Exception implements IUi{

	private static final long serialVersionUID = -9139340021011168614L;
	final static Logger logger = LoggerFactory
			.getLogger(ProcedureParamsCaller.class);

	public SpecSyntaxException(String str, String spec) {
		super(msgTag + str);
		System.out.println(errorTag);
		logger.error("Spec: " + spec);
	}

}
