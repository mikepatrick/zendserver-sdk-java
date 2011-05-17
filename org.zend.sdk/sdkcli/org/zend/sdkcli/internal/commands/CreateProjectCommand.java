/*******************************************************************************
 * Copyright (c) May 16, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/

package org.zend.sdkcli.internal.commands;

import org.apache.commons.cli.Options;
import org.zend.sdkcli.ParseError;
import org.zend.sdklib.ZendProject;

/**
 * Concrete implementation of {@link AbstractCommand}. It represents
 * create-project command. In the result of calling it new PHP project is
 * created in defined location.
 * 
 * Command Parameters:
 * <table border="1">
 * <tr>
 * <th>Parameter</th>
 * <th>Required</th>
 * <th>Argument</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>name</td>
 * <td>true</td>
 * <td>String</td>
 * <td>Project name.</td>
 * </tr>
 * <tr>
 * <td>target</td>
 * <td>false</td>
 * <td>String</td>
 * <td>Target ID.</td>
 * </tr>
 * <tr>
 * <td>index</td>
 * <td>false</td>
 * <td>String</td>
 * <td>Index name.</td>
 * </tr>
 * <tr>
 * <td>path</td>
 * <td>false</td>
 * <td>String</td>
 * <td>Path to the location where project should be created. If it is not
 * specified, project will be created in the current location.</td>
 * </tr>
 * </table>
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public class CreateProjectCommand extends AbstractCommand {

	public static final String TARGET = "target";
	public static final String NAME = "name";
	public static final String INDEX = "index";
	public static final String PATH = "path";

	public CreateProjectCommand(CommandLine commandLine) throws ParseError {
		super(commandLine);
	}

	@Override
	public Options createOptions() {
		addArgumentOption(TARGET, false);
		addArgumentOption(NAME, true);
		addArgumentOption(INDEX, false);
		addArgumentOption(PATH, false);
		return this.options;
	}

	@Override
	public boolean execute() {
		// use parameters here, for example:
		String value = getValue(TARGET);
		
		// TODO Auto-generated method stub
		return new ZendProject().create();
	}
}
