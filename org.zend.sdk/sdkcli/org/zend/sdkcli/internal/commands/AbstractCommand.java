/*******************************************************************************
 * Copyright (c) May 16, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/

package org.zend.sdkcli.internal.commands;

import org.apache.commons.cli.Options;
import org.zend.sdkcli.ICommand;
import org.zend.sdkcli.ParseError;
import org.zend.sdkcli.internal.options.DetectOptionUtility;
import org.zend.sdkcli.internal.options.Option;
import org.zend.sdklib.logger.ILogger;
import org.zend.sdklib.logger.Log;

/**
 * Represents basic class for commands.
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public abstract class AbstractCommand implements ICommand {

	private static final String SILENT = "z";
	
	protected CommandLine commandLine;
	protected Options options;

	/**
	 * @param commandLine
	 * @throws ParseError
	 */
	public AbstractCommand() {
		// build options
		this.options = new Options();
		setupOptions();
	}

	@Option(opt = SILENT, required = false, description = "Silent mode: only errors are printed out.")
	public boolean isSilent() {
		return hasOption(SILENT);
	}
	
	/* (non-Javadoc)
	 * @see org.zend.sdkcli.ICommand#execute(org.zend.sdkcli.internal.commands.CommandLine)
	 */
	public boolean execute(CommandLine cmdLine) throws ParseError {
		// parse command line according to options
		this.commandLine = cmdLine;
		commandLine.parse(options);
		return doExecute();
	}

	/**
	 * @return true if process success
	 */
	protected abstract boolean doExecute();
	
	/**
	 * Commands setup their {@link Options}
	 */
	protected void setupOptions() {
		DetectOptionUtility.addOption(getClass(), options);
	}


	public String getValue(String parameterName) {
		return commandLine.getParameterValue(parameterName);
	}

	public boolean hasOption(String parameterName) {
		return commandLine.hasOption(parameterName);
	}

	public String[] getValues(String parameterName) {
		return commandLine.getParameterValues(parameterName);
	}

	/**
	 * @return the available logger for command line
	 */
	public ILogger getLogger() {
		return Log.getInstance().getLogger(this.getClass().getName(), isSilent());
	}

	public Options getOptions() {
		return options;
	}

}
