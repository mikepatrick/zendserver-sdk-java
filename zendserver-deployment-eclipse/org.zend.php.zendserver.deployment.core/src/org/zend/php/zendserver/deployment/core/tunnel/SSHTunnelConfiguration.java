/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.php.zendserver.deployment.core.tunnel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.server.core.Server;

/**
 * SSH tunnel configuration. It contains all settings required to set up ssh
 * tunneling with port forwarding and HTTP proxy. It supports both
 * username/password and private key credentials.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
@SuppressWarnings("restriction")
public class SSHTunnelConfiguration {

	private static final String ENABLED = "sshTunnelEnabled"; //$NON-NLS-1$
	private static final String USERNAME = "sshTunnelAuthUsername"; //$NON-NLS-1$
	private static final String PASSWORD = "sshTunnelAuthPassword"; //$NON-NLS-1$
	private static final String PRIVATE_KEY = "sshTunnelPrivateKey"; //$NON-NLS-1$
	private static final String PORT_FORWARDING = "sshTunnelPortForwarding"; //$NON-NLS-1$
	private static final String HTTP_PROXY_HOST = "sshTunnelHttpProxyHost"; //$NON-NLS-1$
	private static final String HTTP_PROXY_PORT = "sshTunnelHttpProxyPort"; //$NON-NLS-1$

	private static final String FORWARDING_SEPARATOR = ";"; //$NON-NLS-1$

	private boolean enabled;
	private String username;
	private String password;
	private String host;
	private String privateKey;
	private List<PortForwarding> portForwardings;
	private String httpProxyHost;
	private String httpProxyPort;

	public SSHTunnelConfiguration() {
		this.portForwardings = new ArrayList<PortForwarding>();
	}

	public static SSHTunnelConfiguration read(Server server) {
		SSHTunnelConfiguration config = new SSHTunnelConfiguration();
		config.setEnabled(Boolean.valueOf(server.getAttribute(ENABLED,
				String.valueOf(false))));
		config.setUsername(server.getAttribute(USERNAME, null));
		config.setPassword(server.getAttribute(PASSWORD, null));
		config.setHost(server.getHost());
		config.setPrivateKey(server.getAttribute(PRIVATE_KEY, null));
		config.setPortForwardings(deserializeForwarding(server.getAttribute(
				PORT_FORWARDING, null)));
		config.setHttpProxyHost(server.getAttribute(HTTP_PROXY_HOST, null));
		config.setHttpProxyPort(server.getAttribute(HTTP_PROXY_PORT, null));
		return config;
	}

	/**
	 * Store SSH tunnel configuration in specified PHP server's settings.
	 * 
	 * @param server
	 */
	public void store(Server server) {
		server.setAttribute(ENABLED, String.valueOf(this.isEnabled()));
		String username = getUsername();
		if (username != null && !username.isEmpty()) {
			server.setAttribute(USERNAME, username);
		} else {
			server.removeAttribute(USERNAME);
		}
		String password = getPassword();
		if (password != null && !password.isEmpty()) {
			server.setAttribute(PASSWORD, password);
		} else {
			server.removeAttribute(PASSWORD);
		}
		String privateKey = getPrivateKey();
		if (privateKey != null && !privateKey.isEmpty()) {
			server.setAttribute(PRIVATE_KEY, getPrivateKey());
		} else {
			server.removeAttribute(PRIVATE_KEY);
		}
		String portForwarding = serializeForwarding();
		if (!portForwarding.isEmpty()) {
			server.setAttribute(PORT_FORWARDING, portForwarding);
		} else {
			server.removeAttribute(PORT_FORWARDING);
		}
		String httpProxyHost = getHttpProxyHost();
		if (httpProxyHost != null && !httpProxyHost.isEmpty()) {
			server.setAttribute(HTTP_PROXY_HOST, httpProxyHost);
		} else {
			server.removeAttribute(HTTP_PROXY_HOST);
		}
		String httpProxyPort = getHttpProxyPort();
		if (httpProxyPort != null && !httpProxyPort.isEmpty()) {
			server.setAttribute(HTTP_PROXY_PORT, httpProxyPort);
		} else {
			server.removeAttribute(HTTP_PROXY_PORT);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public List<PortForwarding> getPortForwardings() {
		return portForwardings;
	}

	public String getHttpProxyHost() {
		return httpProxyHost;
	}

	public String getHttpProxyPort() {
		return httpProxyPort;
	}

	public String getHost() {
		return host;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public void setPortForwardings(List<PortForwarding> portForwardings) {
		this.portForwardings = portForwardings;
	}

	public void setHttpProxyHost(String httpProxyHost) {
		this.httpProxyHost = httpProxyHost;
	}

	public void setHttpProxyPort(String httpProxyPort) {
		this.httpProxyPort = httpProxyPort;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Remove SSH tunnel attributes for specified PHP server.
	 * 
	 * @param server
	 */
	public static void remove(Server server) {
		server.removeAttribute(ENABLED);
		server.removeAttribute(USERNAME);
		server.removeAttribute(PASSWORD);
		server.removeAttribute(PRIVATE_KEY);
		server.removeAttribute(PORT_FORWARDING);
		server.removeAttribute(HTTP_PROXY_HOST);
		server.removeAttribute(HTTP_PROXY_PORT);
	}

	private static List<PortForwarding> deserializeForwarding(String input) {
		List<PortForwarding> result = new ArrayList<PortForwarding>();
		if (input != null) {
			String[] entries = input.split(FORWARDING_SEPARATOR);
			for (String entry : entries) {
				PortForwarding forwarding = PortForwarding.deserialize(entry);
				if (forwarding != null) {
					result.add(forwarding);
				}
			}
		}
		return result;
	}

	private String serializeForwarding() {
		StringBuilder result = new StringBuilder();
		if (portForwardings != null) {
			for (PortForwarding portForwarding : portForwardings) {
				if (result.length() != 0) {
					result.append(FORWARDING_SEPARATOR);
				}
				result.append(portForwarding.serialize());
			}
		}
		return result.toString();
	}
	
}
