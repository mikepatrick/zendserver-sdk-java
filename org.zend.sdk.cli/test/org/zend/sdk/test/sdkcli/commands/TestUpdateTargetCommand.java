package org.zend.sdk.test.sdkcli.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.MalformedURLException;

import org.junit.Test;
import org.zend.sdkcli.CommandFactory;
import org.zend.sdkcli.ParseError;
import org.zend.sdkcli.internal.commands.CommandLine;
import org.zend.sdkcli.internal.commands.UpdateTargetCommand;
import org.zend.sdklib.manager.TargetException;
import org.zend.webapi.core.WebApiException;

public class TestUpdateTargetCommand extends AbstractTargetCommandTest {

	private String[] validCommand = new String[] { "update", "target", "-t",
			"dev4", "-k", "newKey", "-s", "000000", "-h", "http://newHost" };

	@Test
	public void testExecute() throws ParseError, WebApiException, TargetException {
		CommandLine cmdLine = new CommandLine(validCommand);
		UpdateTargetCommand command = getCommand(cmdLine);
		assertNotNull(command);
		manager.add(getTarget());
		assertTrue(command.execute(cmdLine));
	}

	@Test
	public void testExecuteUpdateFail() throws ParseError, WebApiException {
		CommandLine cmdLine = new CommandLine(validCommand);
		UpdateTargetCommand command = getCommand(cmdLine);
		assertNotNull(command);
		assertFalse(command.execute(cmdLine));
	}

	@Test
	public void testExecuteInvalidUrl() throws ParseError, WebApiException, TargetException,
			MalformedURLException {
		CommandLine cmdLine = new CommandLine(new String[] { "update",
				"target", "-t", "dev4", "-h", "a111:/\test1test" });
		UpdateTargetCommand command = getCommand(cmdLine);
		assertNotNull(command);
		manager.add(getTarget());
		assertFalse(command.execute(cmdLine));
	}

	@Test
	public void testExecuteNoChanges() throws ParseError, WebApiException, TargetException,
			MalformedURLException {
		CommandLine cmdLine = new CommandLine(new String[] { "update",
				"target", "-t", "dev4" });
		UpdateTargetCommand command = getCommand(cmdLine);
		assertNotNull(command);
		manager.add(getTarget());
		assertTrue(command.execute(cmdLine));
	}

	@Test
	public void testExecuteProperties() throws ParseError, WebApiException, TargetException,
			MalformedURLException {
		CommandLine cmdLine = new CommandLine(new String[] { "update",
				"target", "-t", "dev4", "-p",
				this.getClass().getResource("target.properties").getPath() });
		UpdateTargetCommand command = getCommand(cmdLine);
		assertNotNull(command);
		manager.add(getTarget());
		assertTrue(command.execute(cmdLine));
	}

	@Test
	public void testExecuteInvalidPropertiesFile() throws ParseError,
			WebApiException, TargetException, MalformedURLException {
		CommandLine cmdLine = new CommandLine(new String[] { "update",
				"target", "-t", "dev4", "-p", "nofilename" });
		UpdateTargetCommand command = getCommand(cmdLine);
		assertNotNull(command);
		manager.add(getTarget());
		assertTrue(command.execute(cmdLine));
	}

	private UpdateTargetCommand getCommand(CommandLine cmdLine)
			throws ParseError {
		UpdateTargetCommand command = spy((UpdateTargetCommand) CommandFactory
				.createCommand(cmdLine));
		doReturn(manager).when(command).getTargetManager();
		return command;
	}

}
