package org.zend.sdk.test.sdklib.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zend.sdk.test.AbstractTest;
import org.zend.sdklib.internal.target.UserBasedTargetLoader;
import org.zend.sdklib.internal.target.ZendTarget;
import org.zend.sdklib.internal.target.ZendTargetAutoDetect;
import org.zend.sdklib.manager.DetectionException;
import org.zend.sdklib.manager.TargetException;
import org.zend.sdklib.manager.TargetsManager;
import org.zend.sdklib.target.ITargetLoader;
import org.zend.sdklib.target.IZendTarget;
import org.zend.sdklib.target.LicenseExpiredException;
import org.zend.webapi.core.WebApiException;

public class TestTargetsManager extends AbstractTest {

	private ITargetLoader loader;
	private File file;

	@Before
	public void startup() {
		final String tempDir = System.getProperty("java.io.tmpdir");
		file = new File(tempDir + File.separator + new Random().nextInt());
		file.mkdir();
		loader = new UserBasedTargetLoader(file);
	}

	@After
	public void shutdown() {
		loader = null;
		file.deleteOnExit();
	}

	@Test
	public void testCreateManagerWithInvalidTarget() throws WebApiException,
			MalformedURLException {
		ITargetLoader loader = spy(new UserBasedTargetLoader(file));
		when(loader.loadAll()).thenReturn(
				new IZendTarget[] { new ZendTarget(null, new URL(
						"http://localhost"), "mykey", "43543") });
		TargetsManager manager = new TargetsManager(loader);
		assertTrue(manager.getTargets().length == 0);
	}

	@Test
	public void testCreateManagerWithValidTarget() throws WebApiException,
			MalformedURLException {
		ITargetLoader loader = spy(new UserBasedTargetLoader(file));
		when(loader.loadAll()).thenReturn(
				new IZendTarget[] { new ZendTarget("dev3", new URL(
						"http://localhost"), "mykey", "43543") });
		TargetsManager manager = new TargetsManager(loader);
		assertTrue(manager.getTargets().length == 1);
	}

	@Test
	public void testAddTarget() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = getTarget();
		manager.add(target);
		assertTrue(manager.getTargets().length == 1);
	}

	@Test
	public void testNullIdAddTarget() throws WebApiException, TargetException,
			MalformedURLException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = spy(new ZendTarget(null, new URL(
				"http://localhost:10081"), "mykey", "43543"));
		doReturn(true).when(target).connect();
		manager.add(target);
		assertTrue(manager.getTargets().length == 0);
	}

	@Test
	public void testAddDisconnectedTarget() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = getTarget();
		when(target.connect()).thenReturn(false);
		assertNull(manager.add(target));
	}

	@Test
	public void testAddDuplicatedTarget() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = getTarget();
		manager.add(target);
		manager.add(target);
		assertTrue(manager.getTargets().length == 1);
	}

	@Test
	public void testAddNullTarget() throws TargetException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		assertNull(manager.add(null));
	}

	@Test
	public void testRemoveTarget() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = getTarget();
		manager.add(target);
		assertTrue(manager.getTargets().length == 1);
		manager.remove(target);
		assertTrue(manager.getTargets().length == 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveNullTarget() throws WebApiException {
		TargetsManager manager = new TargetsManager(loader);
		manager.remove(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveNotAddedTarget() throws WebApiException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = getTarget();
		manager.remove(target);
	}

	@Test
	public void testGetTargetById() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = new TargetsManager(loader);
		IZendTarget target = getTarget();
		manager.add(target);
		assertTrue(manager.getTargets().length == 1);
		assertSame(target, manager.getTargetById(target.getId()));
	}

	@Test
	public void testGetNullTargetById() throws WebApiException {
		TargetsManager manager = new TargetsManager(loader);
		assertNull(manager.getTargetById(null));
	}

	@Test
	public void testGetNotAddedTargetById() throws WebApiException {
		TargetsManager manager = new TargetsManager(loader);
		assertNull(manager.getTargetById("0"));
	}

	@Test
	public void testDetectLocalhost() throws WebApiException, TargetException,
			IOException, DetectionException, LicenseExpiredException {
		TargetsManager manager = getManagerForLocalhostDetection();
		IZendTarget target = manager.detectLocalhostTarget("test", "sdk.admin",
				false, false);
		assertNotNull(target);
	}

	@Test(expected = DetectionException.class)
	public void testDetectLocalhostNoKey() throws WebApiException,
			TargetException, IOException, DetectionException, LicenseExpiredException {
		TargetsManager manager = getManagerForLocalhostDetection();
		manager.detectLocalhostTarget("test", null, false, false);
	}

	@Test
	public void testCreateTarget() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		doReturn(getTarget()).when(manager).add(any(IZendTarget.class));
		assertNotNull(manager.createTarget("1", "http://localhost", "mykey",
				"43543"));
	}

	@Test
	public void testCreateTargetNoId() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		doReturn(getTarget()).when(manager).add(any(IZendTarget.class));
		assertNotNull(manager
				.createTarget("http://localhost", "mykey", "43543"));
	}

	@Test
	public void testCreateTargetFailAdd() throws TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		doReturn(null).when(manager).add(any(IZendTarget.class));
		assertNull(manager.createTarget("1", "http://localhost", "mykey",
				"43543"));
	}

	@Test
	public void testCreateTargetInvalidUrl() throws TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		doReturn(null).when(manager).add(any(IZendTarget.class));
		assertNull(manager.createTarget("1", "aaa11://localhost", "mykey",
				"43543"));
	}

	@Test
	public void testCreateTargetAddThrowsException() throws TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		Mockito.doThrow(new TargetException("testError")).when(manager)
				.add(any(IZendTarget.class));
		assertNull(manager.createTarget("1", "http://localhost", "mykey",
				"43543"));
	}

	@Test
	public void testUpdateTarget() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertNotNull(manager.updateTarget("dev4", "http://test1test", null, "new",
				"00112233"));
		IZendTarget actual = manager.getTargetById("dev4");
		assertEquals("http://test1test", actual.getHost().toString());
		assertEquals("new", actual.getKey());
		assertEquals("00112233", actual.getSecretKey());
	}

	@Test
	public void testUpdateTarget2() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertNotNull(manager.updateTarget("dev4", "http://test1test", null, "new",
				null));
		IZendTarget actual = manager.getTargetById("dev4");
		assertEquals("http://test1test", actual.getHost().toString());
		assertEquals("new", actual.getKey());
		assertEquals("43543", actual.getSecretKey());
	}

	@Test
	public void testUpdateTarget3() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertNotNull(manager.updateTarget("dev4", "http://test1test", null, null,
				null));
		IZendTarget actual = manager.getTargetById("dev4");
		assertEquals("http://test1test", actual.getHost().toString());
		assertEquals("mykey", actual.getKey());
		assertEquals("43543", actual.getSecretKey());
	}

	@Test
	public void testUpdateTargetNullId() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertNull(manager.updateTarget(null, null, null, null, null));
	}

	@Test
	public void testUpdateTargetInvalidUrl() throws WebApiException, TargetException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertNull(manager.updateTarget("dev4", "a111://qwerty", null, null, null));
	}

	@Test
	public void testCreateUniqueId() throws TargetException, WebApiException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertEquals("1", manager.createUniqueId(null));
	}

	@Test
	public void testCreateUniqueIdPrefix() throws TargetException,
			WebApiException, LicenseExpiredException {
		TargetsManager manager = spy(new TargetsManager(loader));
		manager.add(getTarget());
		assertEquals("target1", manager.createUniqueId("target"));
	}

	@Test
	public void testIsPhpcloudTargetFalse() throws MalformedURLException {
		IZendTarget target = new ZendTarget("dev4",
				new URL("http://localhost"), "mykey", "43543");
		assertFalse(TargetsManager.isPhpcloud(target));
	}

	@Test
	public void testIsPhpcloudTargetTrue() throws MalformedURLException {
		IZendTarget target = new ZendTarget("dev4", new URL(
				"http://test.my.phpcloud.com"), "mykey", "43543");
		assertTrue(TargetsManager.isPhpcloud(target));
	}

	@Test
	public void testIsPhpcloudTargetNull() throws MalformedURLException {
		assertFalse(TargetsManager.isPhpcloud((IZendTarget) null));
	}

	@Test
	public void testIsPhpcloudHostFalse() throws MalformedURLException {
		assertFalse(TargetsManager.isPhpcloud("http://localhost"));
	}

	@Test
	public void testIsPhpcloudHostTrue() throws MalformedURLException {
		assertTrue(TargetsManager.isPhpcloud("http://test.my.phpcloud.com"));
	}

	@Test
	public void testIsPhpcloudHostNull() throws MalformedURLException {
		assertFalse(TargetsManager.isPhpcloud((String) null));
	}

	private IZendTarget getTarget() throws WebApiException, LicenseExpiredException {
		IZendTarget target = null;
		try {
			target = spy(new ZendTarget("dev4", new URL("http://localhost"),
					"mykey", "43543"));
			doReturn(true).when(target).connect();
		} catch (MalformedURLException e) {
			// ignore
		}
		return target;
	}

	private TargetsManager getManagerForLocalhostDetection()
			throws IOException, DetectionException {
		TargetsManager manager = spy(new TargetsManager(loader));
		ZendTargetAutoDetect detector = spy(new ZendTargetAutoDetect(false));
		doReturn("test/config/server").when(detector)
				.findLocalhostInstallDirectory();
		detector.init();
		doReturn(detector).when(manager).getAutoDetector();
		return manager;
	}

}
