/*
SOURCE: https://raw.githubusercontent.com/lightbody/browsermob-proxy/browsermob-proxy-2.0-beta-9/src/main/java/net/lightbody/bmp/proxy/selenium/KeyStoreManager.java

                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


GROUPON LICENSE:

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

import net.lightbody.bmp.proxy.jetty.log.LogFactory;
import net.lightbody.bmp.proxy.selenium.ThumbprintUtil;
import org.apache.commons.logging.Log;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.HashMap;

/**
 * This is the main entry point into the Cybervillains CA.
 *
 * This class handles generation, storage and the persistent
 * mapping of input to duplicated certificates and mapped public
 * keys.
 *
 * Default setting is to immediately persist changes to the store
 * by writing out the keystore and mapping file every time a new
 * certificate is added.  This behavior can be disabled if desired,
 * to enhance performance or allow temporary testing without modifying
 * the certificate store.
 *
 ***************************************************************************************
 * Copyright (c) 2007, Information Security Partners, LLC
 * All rights reserved.
 *
 * In a special exception, Selenium/OpenQA is allowed to use this code under the Apache License 2.0.
 *
 * @author Brad Hill
 *
 */
public class KeyStoreManager {

    static Log log = LogFactory.getLog(KeyStoreManager.class);
    private final String CERTMAP_SER_FILE = "certmap.ser";
    private final String SUBJMAP_SER_FILE = "subjmap.ser";

    private final String EXPORTED_CERT_NAME = "cybervillainsCA.cer";

    private final char[] _keypassword = "password".toCharArray();
    private final char[] _keystorepass = "password".toCharArray();
    private final String _caPrivateKeystore = "cybervillainsCA.jks";
    private final String _caCertAlias = "signingCert";
    public static final String _caPrivKeyAlias = "signingCertPrivKey";

    X509Certificate _caCert;
    PrivateKey _caPrivKey;
    KeyStore _ks;

    private HashMap<PublicKey, PrivateKey> _rememberedPrivateKeys;
    private HashMap<PublicKey, PublicKey>  _mappedPublicKeys;
    private HashMap<String, String>        _certMap;
    private HashMap<String, String>		  _subjectMap;

    private final String KEYMAP_SER_FILE     = "keymap.ser";
    private final String PUB_KEYMAP_SER_FILE = "pubkeymap.ser";

    public final String RSA_KEYGEN_ALGO = "RSA";
    public final String DSA_KEYGEN_ALGO = "DSA";
    public final KeyPairGenerator _rsaKpg;
    public final KeyPairGenerator _dsaKpg;

    private SecureRandom _sr;




    private boolean persistImmediately = true;
    private File root;

    @SuppressWarnings("unchecked")
    public KeyStoreManager(File root) {
        this.root = root;

        Security.insertProviderAt(new BouncyCastleProvider(), 2);

        _sr = new SecureRandom();

        try
        {
            _rsaKpg = KeyPairGenerator.getInstance(RSA_KEYGEN_ALGO);
            _dsaKpg = KeyPairGenerator.getInstance(DSA_KEYGEN_ALGO);
        }
        catch(Throwable t)
        {
            throw new Error(t);
        }

        try {

            File privKeys = new File(root, KEYMAP_SER_FILE);


            if(!privKeys.exists())
            {
                _rememberedPrivateKeys = new HashMap<PublicKey,PrivateKey>();
            }
            else
            {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(privKeys));
                // Deserialize the object
                _rememberedPrivateKeys = (HashMap<PublicKey,PrivateKey>)in.readObject();
                in.close();
            }


            File pubKeys = new File(root, PUB_KEYMAP_SER_FILE);

            if(!pubKeys.exists())
            {
                _mappedPublicKeys = new HashMap<PublicKey,PublicKey>();
            }
            else
            {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(pubKeys));
                // Deserialize the object
                _mappedPublicKeys = (HashMap<PublicKey,PublicKey>)in.readObject();
                in.close();
            }

        } catch (FileNotFoundException e) {
            // check for file exists, won't happen.
            e.printStackTrace();
        } catch (IOException e) {
            // we could correct, but this probably indicates a corruption
            // of the serialized file that we want to know about; likely
            // synchronization problems during serialization.
            e.printStackTrace();
            throw new Error(e);
        } catch (ClassNotFoundException e) {
            // serious problem.
            e.printStackTrace();
            throw new Error(e);
        }



        _rsaKpg.initialize(1024, _sr);
        _dsaKpg.initialize(1024, _sr);


        try
        {
            _ks = KeyStore.getInstance("JKS");

            reloadKeystore();
        }
        catch(FileNotFoundException fnfe)
        {
            try
            {
                createKeystore();
            }
            catch(Exception e)
            {
                throw new Error(e);
            }
        }
        catch(Exception e)
        {
            throw new Error(e);
        }


        try {

            File file = new File(root, CERTMAP_SER_FILE);

            if(!file.exists())
            {
                _certMap = new HashMap<String,String>();
            }
            else
            {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                // Deserialize the object
                _certMap = (HashMap<String,String>)in.readObject();
                in.close();
            }

        } catch (FileNotFoundException e) {
            // won't happen, check file.exists()
            e.printStackTrace();
        } catch (IOException e) {
            // corrupted file, we want to know.
            e.printStackTrace();
            throw new Error(e);
        } catch (ClassNotFoundException e) {
            // something very wrong, exit
            e.printStackTrace();
            throw new Error(e);
        }


        try {

            File file = new File(root, SUBJMAP_SER_FILE);

            if(!file.exists())
            {
                _subjectMap = new HashMap<String,String>();
            }
            else
            {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                // Deserialize the object
                _subjectMap = (HashMap<String,String>)in.readObject();
                in.close();
            }

        } catch (FileNotFoundException e) {
            // won't happen, check file.exists()
            e.printStackTrace();
        } catch (IOException e) {
            // corrupted file, we want to know.
            e.printStackTrace();
            throw new Error(e);
        } catch (ClassNotFoundException e) {
            // something very wrong, exit
            e.printStackTrace();
            throw new Error(e);
        }


    }

    private void reloadKeystore() throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        InputStream is = new FileInputStream(new File(root, _caPrivateKeystore));

        if (is != null)	{
            _ks.load(is, _keystorepass);
            _caCert = (X509Certificate)_ks.getCertificate(_caCertAlias);
            _caPrivKey = (PrivateKey)_ks.getKey(_caPrivKeyAlias, _keypassword);
        }
    }

    /**
     * Creates, writes and loads a new keystore and CA root certificate.
     */
    protected void createKeystore() {

        java.security.cert.Certificate signingCert = null;
        PrivateKey  caPrivKey  = null;

        if(_caCert == null || _caPrivKey == null)
        {
            try
            {
                log.debug("Keystore or signing cert & keypair not found.  Generating...");

                KeyPair caKeypair = getRSAKeyPair();
                caPrivKey = caKeypair.getPrivate();
                signingCert = CertificateCreator.createTypicalMasterCert(caKeypair);

                log.debug("Done generating signing cert");
                log.debug(signingCert);

                _ks.load(null, _keystorepass);

                _ks.setCertificateEntry(_caCertAlias, signingCert);
                _ks.setKeyEntry(_caPrivKeyAlias, caPrivKey, _keypassword, new java.security.cert.Certificate[] {signingCert});

                File caKsFile = new File(root, _caPrivateKeystore);

                OutputStream os = new FileOutputStream(caKsFile);
                _ks.store(os, _keystorepass);

                log.debug("Wrote JKS keystore to: " +
                        caKsFile.getAbsolutePath());

                // also export a .cer that can be imported as a trusted root
                // to disable all warning dialogs for interception

                File signingCertFile = new File(root, EXPORTED_CERT_NAME);

                FileOutputStream cerOut = new FileOutputStream(signingCertFile);

                byte[] buf = signingCert.getEncoded();

                log.debug("Wrote signing cert to: " + signingCertFile.getAbsolutePath());

                cerOut.write(buf);
                cerOut.flush();
                cerOut.close();

                _caCert = (X509Certificate)signingCert;
                _caPrivKey  = caPrivKey;
            }
            catch(Exception e)
            {
                log.error("Fatal error creating/storing keystore or signing cert.", e);
                throw new Error(e);
            }
        }
        else
        {
            log.debug("Successfully loaded keystore.");
            log.debug(_caCert);

        }

    }

    /**
     * Stores a new certificate and its associated private key in the keystore.
     * @param hostname
     *@param cert
     * @param privKey @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public synchronized void addCertAndPrivateKey(String hostname, final X509Certificate cert, final PrivateKey privKey)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException
    {
//		String alias = ThumbprintUtil.getThumbprint(cert);

        _ks.deleteEntry(hostname);

        _ks.setCertificateEntry(hostname, cert);
        _ks.setKeyEntry(hostname, privKey, _keypassword, new java.security.cert.Certificate[] {cert});

        if(persistImmediately)
        {
            persist();
        }

    }

    /**
     * Writes the keystore and certificate/keypair mappings to disk.
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    public synchronized void persist() throws KeyStoreException, NoSuchAlgorithmException, CertificateException {
        try
        {
            FileOutputStream kso = new FileOutputStream(new File(root, _caPrivateKeystore));
            _ks.store(kso, _keystorepass);
            kso.flush();
            kso.close();
            persistCertMap();
            persistSubjectMap();
            persistKeyPairMap();
            persistPublicKeyMap();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Returns the aliased certificate.  Certificates are aliased by their SHA1 digest.
     * @see ThumbprintUtil
     * @param alias
     * @return
     * @throws KeyStoreException
     */
    public synchronized X509Certificate getCertificateByAlias(final String alias) throws KeyStoreException{
        return (X509Certificate)_ks.getCertificate(alias);
    }

    /**
     * Returns the aliased certificate.  Certificates are aliased by their hostname.
     * @see ThumbprintUtil
     * @param alias
     * @return
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws SignatureException
     * @throws CertificateNotYetValidException
     * @throws CertificateExpiredException
     * @throws InvalidKeyException
     * @throws CertificateParsingException
     */
    public synchronized X509Certificate getCertificateByHostname(final String hostname) throws KeyStoreException, CertificateParsingException, InvalidKeyException, CertificateExpiredException, CertificateNotYetValidException, SignatureException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, UnrecoverableKeyException{

        String alias = _subjectMap.get(getSubjectForHostname(hostname));

        if(alias != null) {
            return (X509Certificate)_ks.getCertificate(alias);
        }
        return getMappedCertificateForHostname(hostname);
    }

    /**
     * Gets the authority root signing cert.
     * @return
     * @throws KeyStoreException
     */
    @SuppressWarnings("unused")
    public synchronized X509Certificate getSigningCert() throws KeyStoreException {
        return _caCert;
    }

    /**
     * Gets the authority private signing key.
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    @SuppressWarnings("unused")
    public synchronized PrivateKey getSigningPrivateKey() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return _caPrivKey;
    }

    /**
     * Whether updates are immediately written to disk.
     * @return
     */
    public boolean getPersistImmediately() {
        return persistImmediately;
    }

    /**
     * Whether updates are immediately written to disk.
     * @param persistImmediately
     */
    public void setPersistImmediately(final boolean persistImmediately) {
        this.persistImmediately = persistImmediately;
    }

    /**
     * This method returns the duplicated certificate mapped to the passed in cert, or
     * creates and returns one if no mapping has yet been performed.  If a naked public
     * key has already been mapped that matches the key in the cert, the already mapped
     * keypair will be reused for the mapped cert.
     * @param cert
     * @return
     * @throws CertificateEncodingException
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws CertificateNotYetValidException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     */
    public synchronized X509Certificate getMappedCertificate(final X509Certificate cert)
            throws CertificateEncodingException,
            InvalidKeyException,
            CertificateException,
            CertificateNotYetValidException,
            NoSuchAlgorithmException,
            NoSuchProviderException,
            SignatureException,
            KeyStoreException,
            UnrecoverableKeyException
    {

        String thumbprint = ThumbprintUtil.getThumbprint(cert);

        String mappedCertThumbprint = _certMap.get(thumbprint);

        if(mappedCertThumbprint == null)
        {

            // Check if we've already mapped this public key from a KeyValue
            PublicKey mappedPk = getMappedPublicKey(cert.getPublicKey());
            PrivateKey privKey;

            if(mappedPk == null)
            {
                PublicKey pk = cert.getPublicKey();

                String algo = pk.getAlgorithm();

                KeyPair kp;

                if(algo.equals("RSA")) {
                    kp = getRSAKeyPair();
                }
                else if(algo.equals("DSA")) {
                    kp = getDSAKeyPair();
                }
                else
                {
                    throw new InvalidKeyException("Key algorithm " + algo + " not supported.");
                }
                mappedPk = kp.getPublic();
                privKey = kp.getPrivate();

                mapPublicKeys(cert.getPublicKey(), mappedPk);
            }
            else
            {
                privKey = getPrivateKey(mappedPk);
            }


            X509Certificate replacementCert =
                    CertificateCreator.mitmDuplicateCertificate(
                            cert,
                            mappedPk,
                            getSigningCert(),
                            getSigningPrivateKey());

            addCertAndPrivateKey(null, replacementCert, privKey);

            mappedCertThumbprint = ThumbprintUtil.getThumbprint(replacementCert);

            _certMap.put(thumbprint, mappedCertThumbprint);
            _certMap.put(mappedCertThumbprint, thumbprint);
            _subjectMap.put(replacementCert.getSubjectX500Principal().getName(), thumbprint);

            if(persistImmediately) {
                persist();
            }
            return replacementCert;
        }
        return getCertificateByAlias(mappedCertThumbprint);

    }

    /**
     * This method returns the mapped certificate for a hostname, or generates a "standard"
     * SSL server certificate issued by the CA to the supplied subject if no mapping has been
     * created.  This is not a true duplication, just a shortcut method
     * that is adequate for web browsers.
     *
     * @param hostname
     * @return
     * @throws CertificateParsingException
     * @throws InvalidKeyException
     * @throws CertificateExpiredException
     * @throws CertificateNotYetValidException
     * @throws SignatureException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     */
    public X509Certificate getMappedCertificateForHostname(String hostname) throws CertificateParsingException, InvalidKeyException, CertificateExpiredException, CertificateNotYetValidException, SignatureException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, UnrecoverableKeyException
    {
        String subject = getSubjectForHostname(hostname);

        String thumbprint = _subjectMap.get(subject);

        if(thumbprint == null) {

            KeyPair kp = getRSAKeyPair();

            X509Certificate newCert = CertificateCreator.generateStdSSLServerCertificate(kp.getPublic(),
                    getSigningCert(),
                    getSigningPrivateKey(),
                    subject);

            addCertAndPrivateKey(hostname, newCert, kp.getPrivate());

            thumbprint = ThumbprintUtil.getThumbprint(newCert);

            _subjectMap.put(subject, thumbprint);

            if(persistImmediately) {
                persist();
            }

            return newCert;

        }
        return getCertificateByAlias(thumbprint);


    }

    private String getSubjectForHostname(String hostname) {
        //String subject = "C=USA, ST=WA, L=Seattle, O=Cybervillains, OU=CertificationAutority, CN=" + hostname + ", EmailAddress=evilRoot@cybervillains.com";
        String subject = "CN=" + hostname + ", OU=Test, O=CyberVillainsCA, L=Seattle, S=Washington, C=US";
        return subject;
    }

    private synchronized void persistCertMap() {
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(root, CERTMAP_SER_FILE)));
            out.writeObject(_certMap);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // writing, this shouldn't happen...
            e.printStackTrace();
        } catch (IOException e) {
            // big problem!
            e.printStackTrace();
            throw new Error(e);
        }
    }



    private synchronized void persistSubjectMap() {
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(root, SUBJMAP_SER_FILE)));
            out.writeObject(_subjectMap);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // writing, this shouldn't happen...
            e.printStackTrace();
        } catch (IOException e) {
            // big problem!
            e.printStackTrace();
            throw new Error(e);
        }
    }


    /**
     * For a cert we have generated, return the private key.
     * @param cert
     * @return
     * @throws CertificateEncodingException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     */
    public synchronized PrivateKey getPrivateKeyForLocalCert(final X509Certificate cert)
            throws CertificateEncodingException, KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException
    {
        String thumbprint = ThumbprintUtil.getThumbprint(cert);

        return (PrivateKey)_ks.getKey(thumbprint, _keypassword);
    }


    /**
     * Generate an RSA Key Pair
     * @return
     */
    public KeyPair getRSAKeyPair()
    {
        KeyPair kp = _rsaKpg.generateKeyPair();
        rememberKeyPair(kp);
        return kp;

    }

    /**
     * Generate a DSA Key Pair
     * @return
     */
    public KeyPair getDSAKeyPair()
    {
        KeyPair kp = _dsaKpg.generateKeyPair();
        rememberKeyPair(kp);
        return kp;
    }


    private synchronized void persistPublicKeyMap() {
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(root, PUB_KEYMAP_SER_FILE)));
            out.writeObject(_mappedPublicKeys);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // writing, won't happen
            e.printStackTrace();
        } catch (IOException e) {
            // very bad
            e.printStackTrace();
            throw new Error(e);
        }
    }

    private synchronized void persistKeyPairMap() {
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(root, KEYMAP_SER_FILE)));
            out.writeObject(_rememberedPrivateKeys);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // writing, won't happen.
            e.printStackTrace();
        } catch (IOException e) {
            // very bad
            e.printStackTrace();
            throw new Error(e);
        }
    }

    private synchronized void rememberKeyPair(final KeyPair kp)
    {
        _rememberedPrivateKeys.put(kp.getPublic(), kp.getPrivate());
        if(persistImmediately) { persistKeyPairMap(); }
    }

    /**
     * Stores a public key mapping.
     * @param original
     * @param substitute
     */
    public synchronized void mapPublicKeys(final PublicKey original, final PublicKey substitute)
    {
        _mappedPublicKeys.put(original, substitute);
        if(persistImmediately) { persistPublicKeyMap(); }
    }

    /**
     * If we get a KeyValue with a given public key, then
     * later see an X509Data with the same public key, we shouldn't split this
     * in our MITM impl.  So when creating a new cert, we should check if we've already
     * assigned a substitute key and re-use it, and vice-versa.
     * @param pk
     * @return
     */
    public synchronized PublicKey getMappedPublicKey(final PublicKey original)
    {
        return _mappedPublicKeys.get(original);
    }

    /**
     * Returns the private key for a public key we have generated.
     * @param pk
     * @return
     */
    public synchronized PrivateKey getPrivateKey(final PublicKey pk)
    {
        return  _rememberedPrivateKeys.get(pk);
    }

    public KeyStore getKeyStore() {
        return _ks;
    }
}