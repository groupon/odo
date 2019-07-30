/*
 Copyright 2014 Groupon, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package com.groupon.odo.bmp;

import java.io.File;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;

import org.apache.commons.io.FileUtils;

import com.groupon.odo.bmp.KeyStoreManager;

public class Utils {
	
	/**
	 * Gets a keystore manager for a given hostname
	 * Creates one/key if it does not already exist
	 * @param hostname
	 * @return
	 * @throws Exception
	 */
	public static KeyStoreManager getKeyStoreManager(String hostname) throws Exception {
		File root = getKeyStoreRoot(hostname);

    	// create entry
    	KeyStoreManager keyStoreManager = new KeyStoreManager(root);
    	
    	// under the hood this will generate the cert if it doesn't exist
    	keyStoreManager.getCertificateByHostname(hostname);
    	
    	// use this since getCertificateByHostname always returns null, but hostname == alias for our purpose
    	X509Certificate cert = keyStoreManager.getCertificateByAlias(hostname);
    	try {
    		cert.checkValidity();
    	} catch (CertificateExpiredException cee) {
    		// if the cert is expired we should destroy it and recursively call this function
    		keyStoreManager = null;
    		FileUtils.deleteDirectory(root);
    		
    		return getKeyStoreManager(hostname);
    	}
    	
    	return keyStoreManager;
	}
	
	private static File getKeyStoreRoot(String hostname) throws Exception {
		File root = new File("seleniumSslSupport" + File.separator, hostname);
		if (!root.exists()) {
    		// create it and get the root cert
    		root.delete();
        	root.mkdirs();
    		com.groupon.odo.proxylib.Utils.copyResourceToLocalFile("cybervillainsCA.jks", root.getAbsolutePath() + File.separator + "cybervillainsCA.jks");
        	com.groupon.odo.proxylib.Utils.copyResourceToLocalFile("cybervillainsCA.cer", root.getAbsolutePath() + File.separator + "cybervillainsCA.cer");
    	}
		
		return root;
	}
}
