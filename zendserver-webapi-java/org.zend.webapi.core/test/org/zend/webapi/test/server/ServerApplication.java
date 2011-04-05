package org.zend.webapi.test.server;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;
import org.zend.webapi.test.server.response.ConfigurationResponse;
import org.zend.webapi.test.server.response.ServerResponse;
import org.zend.webapi.test.server.response.ServiceResponse;

public class ServerApplication extends Application {

	@Override
	public synchronized Restlet createRoot() {
		Router router = new Router(getContext());

		Restlet getSystemInfo = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				ServerResponse serverResponse = ZendSystem.getInstance()
						.getSystemInfo();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet clusterGetServerStatus = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				ServerResponse serverResponse = ZendSystem.getInstance()
						.clusterGetServerStatus();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet clusterAddServer = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				String postParams = request.getEntityAsText();
				Assert.assertNotNull(postParams);
				ServerResponse serverResponse = ZendSystem.getInstance()
						.clusterAddServer();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet clusterRemoveServer = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				String postParams = request.getEntityAsText();
				Assert.assertNotNull(postParams);
				ServerResponse serverResponse = ZendSystem.getInstance()
						.clusterRemoveServer();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet clusterDisableServer = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				String postParams = request.getEntityAsText();
				Assert.assertNotNull(postParams);
				ServerResponse serverResponse = ZendSystem.getInstance()
						.clusterDisableServer();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet clusterEnableServer = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				String postParams = request.getEntityAsText();
				Assert.assertNotNull(postParams);
				ServerResponse serverResponse = ZendSystem.getInstance()
						.clusterEnableServer();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet restartPhp = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				ServerResponse serverResponse = ZendSystem.getInstance()
						.restartPhp();
				prepareResponse(response, serverResponse);
			}
		};

		Restlet configurationExport = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				final ConfigurationResponse serverResponse = (ConfigurationResponse) ZendSystem
						.getInstance().configurationExport();
				InputRepresentation representation = new InputRepresentation(
						new ByteArrayInputStream(serverResponse.getContent()),
						MediaType.valueOf("application/vnd.zend.serverconfig"));
				Disposition disposition = new Disposition(
						Disposition.TYPE_ATTACHMENT);
				disposition.setFilename(serverResponse.getFileName());
				representation.setDisposition(disposition);
				representation.setSize(serverResponse.getFileSize());
				response.setEntity(representation);
				response.setStatus(serverResponse.getStatus());
			}
		};

		Restlet configurationImport = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				final ServerResponse serverResponse = ZendSystem.getInstance()
						.configurationImport();
				prepareResponse(response, serverResponse);
			}
		};

		router.attach("/ZendServerManager/Api/getSystemInfo", getSystemInfo);
		router.attach("/ZendServerManager/Api/clusterGetServerStatus",
				clusterGetServerStatus);
		router.attach("/ZendServerManager/Api/clusterAddServer",
				clusterAddServer);
		router.attach("/ZendServerManager/Api/clusterRemoveServer",
				clusterRemoveServer);
		router.attach("/ZendServerManager/Api/clusterDisableServer",
				clusterDisableServer);
		router.attach("/ZendServerManager/Api/clusterEnableServer",
				clusterEnableServer);
		router.attach("/ZendServerManager/Api/restartPhp", restartPhp);
		router.attach("/ZendServerManager/Api/configurationExport",
				configurationExport);
		router.attach("/ZendServerManager/Api/configurationImport",
				configurationImport);

		return router;
	}

	private void prepareResponse(Response response,
			ServerResponse serverResponse) {
		Representation representation = new DomRepresentation(
				MediaType.APPLICATION_XML,
				((ServiceResponse) serverResponse).getData());
		response.setEntity(representation);
		response.setStatus(serverResponse.getStatus());
	}

}