/*******************************************************************************
 * Copyright (c) 2013 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.sdklib.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.zend.sdklib.descriptor.pkg.Package;
import org.zend.sdklib.descriptor.pkg.Version;
import org.zend.sdklib.internal.application.ZendConnection;
import org.zend.sdklib.internal.project.ProjectResourcesWriter;
import org.zend.sdklib.internal.utils.JaxbHelper;
import org.zend.sdklib.mapping.IMappingLoader;
import org.zend.sdklib.mapping.IVariableResolver;
import org.zend.sdklib.target.ITargetLoader;
import org.zend.webapi.core.WebApiClient;
import org.zend.webapi.core.WebApiException;
import org.zend.webapi.core.connection.data.ApplicationsList;
import org.zend.webapi.core.connection.data.LibraryList;
import org.zend.webapi.core.connection.request.NamedInputStream;
import org.zend.webapi.core.progress.BasicStatus;
import org.zend.webapi.core.progress.StatusCode;

/**
 * Utility class which provides methods to perform operations on library.
 * 
 * @author Wojciech Galanciak, 2013
 * 
 */
public class ZendLibrary extends ZendConnection {

	public static final String TEMP_PREFIX = "ZendStudioDeployment";

	private IVariableResolver variableResolver;

	public ZendLibrary() {
		super();
	}

	public ZendLibrary(IMappingLoader mappingLoader) {
		super(mappingLoader);
	}

	public ZendLibrary(ITargetLoader loader) {
		super(loader);
	}

	public ZendLibrary(ITargetLoader loader, IMappingLoader mappingLoader) {
		super(loader, mappingLoader);
	}

	public void setVariableResolver(IVariableResolver variableResolver) {
		this.variableResolver = variableResolver;
	}

	/**
	 * Provides status of specified library/libraries in selected target.
	 * 
	 * @param targetId
	 * @param libaryId
	 *            - array of library id(s) for which status should be checked
	 * @return instance of {@link ApplicationsList} or <code>null</code> if
	 *         there where problems with connections or target with specified id
	 *         does not exist
	 */
	public LibraryList getStatus(String targetId, String... libaryId) {
		try {
			WebApiClient client = getClient(targetId);
			libaryId = libaryId == null ? new String[0] : libaryId;
			notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
					"Library Status",
					"Retrieving library status(es) from selected server...", -1));
			LibraryList result = client.libraryGetStatus(libaryId);
			notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
					"Library Status",
					"Library status(es) retrievied successfully. "));
			return result;
		} catch (MalformedURLException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
					"Library Status",
					"Error during retrieving library status from '" + targetId
							+ "'", e));
			log.error(e);
		} catch (WebApiException e) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
					"Library Status",
					"Library during retrieving library status from '"
							+ targetId + "'", e));
			log.error("Error during retrieving library status from '"
					+ targetId + "'.");
			log.error("\tpossible error: " + e.getMessage());
		}
		return null;
	}

	public LibraryList deploy(String path, String configLocation, String targetId, boolean zpkPackage) {
		if (path != null) {
			File zendPackage = createPackage(path, configLocation);
			try {
				if (zendPackage != null) {
					WebApiClient client = getClient(targetId);
					notifier.statusChanged(new BasicStatus(StatusCode.STARTING, "Deploying",
							"Deploying library to the server...", -1));
					LibraryList result = client.libraryVersionDeploy(new NamedInputStream(zendPackage));
					notifier.statusChanged(
							new BasicStatus(StatusCode.STOPPING, "Deploying", "Library deployed successfully"));
					return result;
				}
			} catch (MalformedURLException e) {
				notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Deploying",
						"Error during deploying library to '" + targetId + "'", e));
				log.error(e);
			} catch (WebApiException e) {
				notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Deploying",
						"Error during deploying library to '" + targetId + "'", e));
				log.error("Error during deploying library to '" + targetId + "':");
				log.error("\tpossible error: " + e.getMessage());
			} finally {
				if (zendPackage != null)
					deleteDirectory(zendPackage.getParentFile());
			}
			return null;
		}
		return null;
	}

	public LibraryList deploy(String path, String targetId, boolean zpkPackage) {
		return deploy(path, path, targetId, zpkPackage);
	}

	public LibraryList synchronize(String path, String configLocation, int id,
			String targetId) {
		if (path != null) {
			File zendPackage = createPackage(path, configLocation);
			try {
				if (zendPackage != null) {
					WebApiClient client = getClient(targetId);
					notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
							"Synchronizing",
							"Synchronizing library to the target...", -1));
					LibraryList result = client.librarySynchronize(id,
							new NamedInputStream(zendPackage));
					notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
							"Synchronizing",
							"Library synchronized successfully"));
					deleteFile(getTempFile(path));
					return result;
				}
			} catch (MalformedURLException e) {
				notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
						"Synchronizing",
						"Error during synchronizing library to '" + targetId
								+ "'", e));
				log.error(e);
				deleteFile(getTempFile(path));
			} catch (WebApiException e) {
				notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
						"Synchronizing",
						"Error during synchronizing library to '" + targetId
								+ "'", e));
				log.error("Error during synchronizing library to '" + targetId
						+ "':");
				log.error("\tpossible error: " + e.getMessage());
			}
			return null;
		}
		return null;
	}

	public LibraryList synchronize(String path, int id, String targetId) {
		return synchronize(path, path, id, targetId);
	}

	private File createPackage(String path, String configLocation) {
		File file = new File(path);
		if (!file.exists()) {
			notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Deploying", "Path does not exist: " + file));
			log.error("Path does not exist: " + file);
			return null;
		}
		if (file.isDirectory()) {
			File tempFile = getTempFile(path);
			return getPackageBuilder(path, configLocation, variableResolver)
					.createDeploymentPackage(tempFile);
		} else if (file.getName().endsWith(".zip")) {
			File tempUnzipped = null;
			try {
				tempUnzipped = unzip(file);
				File tempFile = getTempFile("/" + new Random().nextInt());
				return getPackageBuilder(tempUnzipped.getAbsolutePath(),
						configLocation, variableResolver)
						.createDeploymentPackage(tempFile);
			} finally {
				if (tempUnzipped != null) {
					deleteDirectory(tempUnzipped);
				}
			}
		} else if (file.getName().endsWith(".zpk")) {
			File tempFile = null;
			try {
				tempFile = getTempFile("/" + new Random().nextInt());
				FileUtils.copyFileToDirectory(file, tempFile);
				return new File(tempFile, file.getName());
			} catch (IOException e) {
				String message = "Could not copy " + file.getAbsolutePath() + " to " + tempFile.getAbsolutePath();
				notifier.statusChanged(new BasicStatus(StatusCode.ERROR, "Deploying", message));
				log.error(message);
				log.error("\tpossible error: " + e.getMessage());
			}
			return null;
		}
		return file;
	}

	public File unzip(File zipFile) {
		byte[] buffer = new byte[4096];
		try {
			File folder = getTempFile("/" + new Random().nextInt());
			ZipInputStream zipInput = new ZipInputStream(new FileInputStream(
					zipFile));
			ZipEntry entry = zipInput.getNextEntry();
			while (entry != null) {
				String fileName = entry.getName();
				File newFile = new File(folder, fileName);
				if (fileName.endsWith("/")) {
					newFile.mkdirs();
				} else {
					new File(newFile.getParent()).mkdirs();
					FileOutputStream out = new FileOutputStream(newFile);
					int length = 0;
					while ((length = zipInput.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					out.close();
				}
				entry = zipInput.getNextEntry();
			}
			zipInput.closeEntry();
			zipInput.close();
			return folder;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getDeploymentPackageName(File directory, File configLocation) {
		if (directory == null || !directory.isDirectory()) {
			log.error(new IllegalArgumentException(
					"Location cannot be null or non-existing directory"));
			return null;
		}
		configLocation = configLocation.getAbsoluteFile();
		String name = getPackageName(configLocation);
		if (name == null) {
			return null;
		}
		return name + ".zpk";
	}

	private String getPackageName(File container) {
		String result = null;
		Package p = getPackage(container);
		if (p != null) {
			String name = p.getName();
			final Version version2 = p.getVersion();
			if (version2 == null) {
				throw new IllegalStateException(
						"Error, missing <version> element in deployment descriptor");
			}

			String version = version2.getRelease();

			if (name != null && version != null) {
				result = name + "-" + version;
			}
		}
		return result;
	}

	private Package getPackage(File container) {
		File descriptorFile = new File(container,
				ProjectResourcesWriter.DESCRIPTOR);
		if (!descriptorFile.exists()) {
			log.error(descriptorFile.getAbsoluteFile() + " does not exist.");
			return null;
		}
		FileInputStream pkgStream = null;
		Package p = null;
		try {
			pkgStream = new FileInputStream(descriptorFile);
			p = JaxbHelper.unmarshalPackage(pkgStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				pkgStream.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return p;
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

	private void deleteDirectory(File dir) {
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			log.error("Could not delete folder: " + dir.getAbsolutePath()); 
			log.error(e.getLocalizedMessage());
		}
	}
}
