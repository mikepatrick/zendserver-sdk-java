package org.zend.php.zendserver.deployment.core.sdk;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.zend.php.zendserver.deployment.core.PreferenceManager;
import org.zend.sdklib.mapping.IMapping;
import org.zend.sdklib.mapping.IMappingModel;
import org.zend.sdklib.mapping.PropertiesBasedMappingLoader;

public class EclipseMappingModelLoader extends PropertiesBasedMappingLoader {

	private IDocument document;

	public EclipseMappingModelLoader() {
	}

	public EclipseMappingModelLoader(IDocument document) {
		this.document = document;
	}

	public List<IMapping> getDefaultExclusion() throws IOException {
		return getMappings(getExclusionsPreference());
	}

	private static String[] getExclusionsPreference() {
		String pref = PreferenceManager.getInstance().getString(
				PreferenceManager.EXCLUDE);
		if (!"".equals(pref)) {
			return pref.split(SEPARATOR);
		}
		return new String[0];
	}

	@Override
	public void store(IMappingModel model, File output)
			throws IOException {
		byte[] result = getByteArray(model);
		if (document != null) {
			document.set(new String(result));
		}
	}

}
