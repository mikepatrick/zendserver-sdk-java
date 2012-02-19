/*******************************************************************************
 * Copyright (c) May 24, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdklib.application;

//import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import org.zend.sdklib.internal.application.ZendConnection;
import org.zend.sdklib.mapping.IMappingLoader;
import org.zend.sdklib.repository.site.Application;
import org.zend.sdklib.target.ITargetLoader;
import org.zend.webapi.core.WebApiClient;
import org.zend.webapi.core.WebApiException;
import org.zend.webapi.core.connection.data.ApplicationInfo;
import org.zend.webapi.core.connection.data.ApplicationsList;
import org.zend.webapi.core.connection.request.NamedInputStream;
import org.zend.webapi.core.progress.BasicStatus;
import org.zend.webapi.core.progress.StatusCode;

/**
 * Utility class which provides methods to perform operations on application.
 * 
 * @author Wojciech Galanciak, 2011
 * 
 */
public class ZendApplication extends ZendConnection {

	public static final String TEMP_PREFIX = "ZendStudioDeployment";

	public ZendApplication() {
		super();
	}

	public ZendApplication(IMappingLoader mappingLoader) {
		super(mappingLoader);
	}

	public ZendApplication(ITargetLoader loader) {
		super(loader);
	}

	public ZendApplication(ITargetLoader loader,
			IMappingLoader mappingLoader) {
		super(loader, mappingLoader);
	}

	/**
	 * Provides information about status of specified application(s) in selected
	 * target.
	 * 
	 * @param targetId
	 * @param applicationIds
	 *            - array of application id(s) for which status should be
	 *            checked
	 * @return instance of {@link ApplicationsList} or <code>null</code> if
	 *         there where problems with connections or target with specified id
	 *         does not exist
	 */
	public ApplicationsList getStatus(String targetId, String... applicationIds) {
		try {
			WebApiClient client = getClient(targetId);
			applicationIds = applicationIds == null ? new String[0]
					: applicationIds;
			notifier.statusChanged(new BasicStatus(StatusCode.STARTING, "Application Status",
					"Retrieving Application status(es) from selected target...", -1));
			ApplicationsList result = client.applicationGetStatus(applicationIds);
			notifier.statusChanged(new BasicStatus(StatusCode.STOPPING, "Application Status",
					"Application status(es) retrievied successfully. "));
			return result;
		} catch (MalformedURLException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Application Status",
					"Error duirng retrieving application status from '" + targetId
							+ "'", e));
			log.error(e);
		} catch (WebApiException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Application Status",
					"Error duirng retrieving application status from '" + targetId
							+ "'", e));
			log.error("Error duirng retrieving application status from '"
					+ targetId + "'.");
			log.error("\tpossible error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Deploys a new application to the specified target.
	 * 
	 * @param path
	 *            - path to project location or application package
	 * @param basePath
	 *            - base path to deploy the application to. relative to the
	 *            host/vhost
	 * @param targetId
	 *            - target id
	 * @param propertiesFile
	 *            - path to properties file which consists user deployment
	 *            parameters
	 * @param appName
	 *            - application name
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @param vhostName
	 *            - the name of the vhost to use, if such a virtual host wasn't
	 *            already created by Zend Server it will be created
	 * @param defaultServer
	 *            - deploy the application on the default server; the base URL
	 *            host provided will be ignored and replaced with
	 *            <default-server>.
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo deploy(String path, String basePath,
			String targetId, String propertiesFile, String appName,
			Boolean ignoreFailures, String vhostName, Boolean defaultServer) {
		Map<String, String> userParams = null;
		if (propertiesFile != null) {
			notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
					"Deploying", "Reading user parameters properites file...",
					-1));
			File propsFile = new File(propertiesFile);
			if (propsFile.exists()) {
				userParams = getUserParameters(propsFile);
			} else {
				userParams = getUserParameters(propertiesFile);
			}
		}
		notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
				"Deploying", "Reading user parametes is completed."));
		return deploy(path, basePath, targetId, userParams, appName,
				ignoreFailures, vhostName, defaultServer);
	}

	/**
	 * Deploys a new application to the specified target.
	 * 
	 * @param inputStream
	 *            - input stream for application package
	 * @param application
	 *            - application description from repository site
	 * @param targetId
	 *            - target id
	 * @param propertiesFile
	 *            - path to properties file which consists user deployment
	 *            parameters
	 * @param appName
	 *            - application name
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @param vhostName
	 *            - the name of the vhost to use, if such a virtual host wasn't
	 *            already created by Zend Server it will be created
	 * @param defaultServer
	 *            - deploy the application on the default server; the base URL
	 *            host provided will be ignored and replaced with
	 *            <default-server>.
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo deploy(InputStream inputStream,
			Application application, String basePath, String targetId,
			String propertiesFile, String appName, Boolean ignoreFailures,
			String vhostName, Boolean defaultServer) {
		if (inputStream != null && application != null) {
			String path = getPackagePath(inputStream, application);
			if (path != null) {
				return deploy(path, basePath, targetId, propertiesFile,
						appName, ignoreFailures, vhostName, defaultServer);
			}
		}
		return null;
	}

	/**
	 * Deploys a new application to the specified target.
	 * 
	 * @param path
	 *            - path to project location or application package
	 * @param basePath
	 *            - base path to deploy the application to. relative to
	 *            host/vhost
	 * @param targetId
	 *            - target id
	 * @param userParams
	 *            - map with user parameters (key and value)
	 * 
	 * @param appName
	 *            - application name
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @param vhostName
	 *            - The virtual host to use, if such a virtual host wasn't
	 *            already created by Zend Server - it is created.
	 * @param defaultServer
	 *            - deploy the application on the default server; the base URL
	 *            host provided will be ignored and replaced with
	 *            <default-server>.
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo deploy(String path, String basePath,
			String targetId, Map<String, String> userParams, String appName,
			Boolean ignoreFailures, String vhostName, Boolean defaultServer) {
		deleteFile(getTempFile(path));
		File zendPackage = createPackage(path);
		try {
			if (zendPackage != null) {
				String baseUrl = resolveBaseUrl(new File(path), basePath,
						defaultServer,
						vhostName);
				WebApiClient client = getClient(targetId);
				if (appName == null) {
					String[] segments = baseUrl.split("/");
					appName = segments.length > 0 ? segments[segments.length - 1]
							: null;
				}
				notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
						"Deploying", "Deploying application to the target...",
						-1));
				ApplicationInfo result = client.applicationDeploy(
						new NamedInputStream(zendPackage), baseUrl,
						ignoreFailures, userParams, appName, vhostName != null,
						defaultServer);
				notifier.statusChanged(new BasicStatus(StatusCode.STOPPING, "Deploying",
						"Application deployed successfully"));
				deleteFile(getTempFile(path));
				return result;
			}
		} catch (MalformedURLException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
					"Deploying", "Error during deploying application to '"
							+ targetId + "'", e));
			log.error(e);
			deleteFile(getTempFile(path));
		} catch (WebApiException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
					"Deploying", "Error during deploying application to '"
							+ targetId + "'", e));
			log.error("Error during deploying application to '" + targetId
					+ "':");
			log.error("\tpossible error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Deploys a new application to the specified target.
	 * 
	 * @param inputStream
	 *            - input stream for application package
	 * @param application
	 *            - application description from repository site
	 * @param basePath
	 *            - base path to deploy the application to. relative to
	 *            host/vhost
	 * @param targetId
	 *            - target id
	 * @param userParams
	 *            - map with user parameters (key and value)
	 * @param appName
	 *            - application name
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @param vhostName
	 *            - The virtual host to use, if such a virtual host wasn't
	 *            already created by Zend Server - it is created.
	 * @param defaultServer
	 *            - deploy the application on the default server; the base URL
	 *            host provided will be ignored and replaced with
	 *            <default-server>.
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo deploy(InputStream inputStream,
			Application application, String basePath, String targetId,
			Map<String, String> userParams, String appName,
			Boolean ignoreFailures, String vhostName, Boolean defaultServer) {
		if (inputStream != null && application != null) {
			String path = getPackagePath(inputStream, application);
			if (path != null) {
				return deploy(path, basePath, targetId, userParams, appName,
						ignoreFailures, vhostName, defaultServer);
			}
		}
		return null;
	}

	/**
	 * Redeploys an existing application, whether in order to fix a problem or
	 * to reset an installation.
	 * 
	 * @param targetId
	 *            - target id
	 * @param appId
	 *            - application id
	 * @param servers
	 *            - array of server id(s) on which application should be
	 *            redeployed
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no application with specified id in the
	 *         target
	 */
	public ApplicationInfo redeploy(String targetId, String appId,
			String[] servers, boolean ignoreFailures) {
		try {
			WebApiClient client = getClient(targetId);
			int appIdint = Integer.parseInt(appId);
			return client
					.applicationSynchronize(appIdint, ignoreFailures, servers);
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (NumberFormatException e) {
			log.error(e.getMessage());
		} catch (WebApiException e) {
			log.error("Error during redeploying application to '" + targetId
					+ "':");
			log.error("\tpossible error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Removes/undeploys an existing application.
	 * 
	 * @param targetId
	 *            - target id
	 * @param appId
	 *            - application id
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no application with specified id in the
	 *         target
	 */
	public ApplicationInfo remove(String targetId, String appId) {
		try {
			WebApiClient client = getClient(targetId);
			int appIdint = Integer.parseInt(appId);
			return client.applicationRemove(appIdint);
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (NumberFormatException e) {
			log.error(e.getMessage());
		} catch (WebApiException e) {
			log.error("Error during removing application from '" + targetId
					+ "':");
			log.error("\tpossible error: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Updates/redeploys an existing application.
	 * 
	 * @param path
	 *            - path to project location or application package
	 * @param targetId
	 *            - target id
	 * @param appId
	 *            - application id
	 * @param propertiesFile
	 *            - path to properties file which consists user deployment
	 *            parameters
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo update(String path, String targetId, String appId,
			String propertiesFile, Boolean ignoreFailures) {
		Map<String, String> userParams = null;
		if (propertiesFile != null) {
			notifier.statusChanged(new BasicStatus(StatusCode.STARTING, "Updating",
					"Reading user parameters properites file...", -1));
			File propsFile = new File(propertiesFile);
			if (propsFile.exists()) {
				userParams = getUserParameters(propsFile);
			}
		}
		notifier.statusChanged(new BasicStatus(StatusCode.STOPPING, "Updating",
				"Reading user parametes is completed."));
		return update(path, targetId, appId, userParams, ignoreFailures);
	}

	/**
	 * Updates/redeploys an existing application.
	 * 
	 * @param inputStream
	 *            - input stream for application package
	 * @param application
	 *            - application description from repository site
	 * @param targetId
	 *            - target id
	 * @param appId
	 *            - application id
	 * @param propertiesFile
	 *            - path to properties file which consists user deployment
	 *            parameters
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo update(InputStream inputStream,
			Application application, String targetId, String appId,
			String propertiesFile, Boolean ignoreFailures) {
		if (inputStream != null && application != null) {
			String path = getPackagePath(inputStream, application);
			if (path != null) {
				return update(path, targetId, appId, propertiesFile,
						ignoreFailures);
			}
		}
		return null;
	}

	/**
	 * Updates/redeploys an existing application.
	 * 
	 * @param path
	 *            - path to project location or application package
	 * @param targetId
	 *            - target id
	 * @param appId
	 *            - application id
	 * @param userParams
	 *            - map with user parameters (key and value)
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo update(String path, String targetId, String appId,
			Map<String, String> userParams, Boolean ignoreFailures) {
		File zendPackage = createPackage(path);
		try {
			if (zendPackage != null) {
				int appIdint = Integer.parseInt(appId);
				WebApiClient client = getClient(targetId);
				notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
						"Updating", "Updating application on the target...", -1));
				ApplicationInfo result = client.applicationUpdate(appIdint,
						new NamedInputStream(zendPackage), ignoreFailures,
						userParams);
				notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
						"Updating", "Application updated successfully"));
				return result;
			}
		} catch (MalformedURLException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Updating",
					"Error during updating application on '" + targetId + "'", e));
			log.error(e);
		} catch (WebApiException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Updating",
					"Error during updating application on '" + targetId + "'", e));
			log.error("Error during updating application on '" + targetId
					+ "':");
			log.error("\tpossible error: " + e.getMessage());
		} finally {
			deleteFile(getTempFile(path));
		}
		return null;
	}

	/**
	 * Updates/redeploys an existing application.
	 * 
	 * @param inputStream
	 *            - input stream for application package
	 * @param application
	 *            - application description from repository site
	 * @param targetId
	 *            - target id
	 * @param appId
	 *            - application id
	 * @param userParams
	 *            - map with user parameters (key and value)
	 * @param ignoreFailures
	 *            - ignore failures during staging if only some servers reported
	 *            failures
	 * @return instance of {@link ApplicationInfo} or <code>null</code> if there
	 *         where problems with connections or target with specified id does
	 *         not exist or there is no package/project in specified path
	 */
	public ApplicationInfo update(InputStream inputStream,
			Application application, String targetId, String appId,
			Map<String, String> userParams, Boolean ignoreFailures) {
		if (inputStream != null && application != null) {
			String path = getPackagePath(inputStream, application);
			if (path != null) {
				return update(path, targetId, appId, userParams, ignoreFailures);
			}
		}
		return null;
	}

	private Map<String, String> getUserParameters(File propsFile) {
		Map<String, String> result = null;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(propsFile));
			Enumeration<?> e = p.propertyNames();
			if (e.hasMoreElements()) {
				result = new HashMap<String, String>();
			}
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				result.put(key, p.getProperty(key));
			}
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return result;
	}

	public Map<String, String> getUserParameters(String propertiesString) {
		Map<String, String> result = new LinkedHashMap<String, String>(3);

		if (Pattern.matches("[[^,=]=[^,=]*][,[^,=]*=[^,=]*]*", propertiesString)) {
			final String[] split = propertiesString.split(",");
			for (String token : split) {
				final String[] val = token.split("=");
				if (val.length == 2) {
					result.put(val[0], val[1]);
				} else {
					log.error("Error parsing property string , skipping token " + token);
				}
			}
		} else {
			log.error("Error parsing property string " + propertiesString);
		}
		
		return result;
	}
	
	private File createPackage(String path) {
		File file = new File(path);
		if (!file.exists()) {
			log.error("Path does not exist: " + file);
			return null;
		}
		if (file.isDirectory()) {
			File tempFile = getTempFile(path);
			if (tempFile.isDirectory()) {
				File[] children = tempFile.listFiles();
				if (children.length == 1) {
					return children[0];
				}
			}
			return getPackageBuilder(path).createDeploymentPackage(tempFile);
		} else {
			return file;
		}
	}

	private File getTempFile(String path) {
		String tempDir = System.getProperty("java.io.tmpdir");
		path = path.replace("\\", "/");
		String suffix = path.substring(path.lastIndexOf("/") + 1);
		File tempFile = new File(tempDir + File.separator + TEMP_PREFIX
				+ suffix);
		if (!tempFile.exists()) {
			tempFile.mkdir();
		}
		return tempFile;
	}

	private boolean deleteFile(File file) {
		if (file == null || !file.exists()) {
			return true;
		}
		if (file.isDirectory()) {
			String[] children = file.list();
			for (int i = 0; i < children.length; i++) {
				boolean result = deleteFile(new File(file, children[i]));
				if (!result) {
					return false;
				}
			}
		}
		return file.delete();
	}

	private String getPackagePath(InputStream in, Application app) {
		String packageName = getPackageName(app.getUrl());
		File tempFile = getTempFile(packageName);
		tempFile = new File(tempFile.getAbsolutePath() + new Random().nextInt());
		if (!tempFile.exists()) {
			tempFile.mkdir();
		}
		File packageFile = new File(tempFile, packageName);
		OutputStream out = null;
		notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
				"Package Downloading",
				"Downloading package from repository...", -1));
		try {
			if (!packageFile.exists()) {
				packageFile.createNewFile();
			}
			out = new FileOutputStream(packageFile);
			byte[] buffer = new byte[2048];
			int len = in.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = in.read(buffer);
			}
		} catch (IOException e) {
			log.error(e);
			return null;
		} finally {
			closeStream(in);
			closeStream(out);
		}
		notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
				"Package Downloading",
				"Package is downloaded from repository successfully.", -1));
		return packageFile.getAbsolutePath();
	}

	private String getPackageName(String url) {
		url = url.replace("\\", "/");
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private String resolveBaseUrl(File path, String basePath,
			Boolean defaultServer, String vhostName)
			throws MalformedURLException {

		if (basePath == null) {
			basePath = path.getName();
		}
		if (basePath.startsWith("/")) {
			basePath = basePath.substring(1);
		}

		String url = MessageFormat.format("http://{0}/{1}",
				vhostName == null ? "default-server" : vhostName, basePath);
		log.debug("resolved url " + url);
		return url;
	}

}
