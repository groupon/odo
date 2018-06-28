/*
SOURCE: https://github.com/lightbody/browsermob-proxy/blob/browsermob-proxy-2.0-beta-9/src/main/java/net/lightbody/bmp/proxy/selenium/CertificateCreator.java

ORIGINAL LICENSE:
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

import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import net.lightbody.bmp.proxy.selenium.ThumbprintUtil;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Methods for creating certificates.
 *
 * ***************************************************************************************
 * Copyright (c) 2007, Information Security Partners, LLC
 * All rights reserved.
 *
 * In a special exception, Selenium/OpenQA is allowed to use this code under the Apache License 2.0.
 *
 * @author Brad Hill
 *
 */
public class CertificateCreator {


	private static final HashSet<String> clientCertOidsNeverToCopy = new HashSet<String>();
	private static final HashSet<String> clientCertDefaultOidsNotToCopy = new HashSet<String>();

	/**
	 * The default key generation algorithm for this package is RSA.
	 */
	public static final String KEYGEN_ALGO = "RSA";

    /**
     * The default sign algorithm for this package is SHA1 with RSA.
     */
    // BEGIN ODO CHANGES
    // Changed from "SHA1withRSA" to "SHA256withRSA" in order to satisfy
    // App Transport Security requirements found here:
    // https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html#//apple_ref/doc/uid/TP40009251-SW57
    public static final String SIGN_ALGO = "SHA256withRSA";
    // END ODO CHANGES


	/**
	 * X.509 OID for Subject Key Identifier Extension - Replaced when duplicating a cert.
	 */
	public static final String OID_SUBJECT_KEY_IDENTIFIER     = "2.5.29.14";

	/**
	 * X.509 OID for Subject Authority Key Identifier - Replaced when duplicating a cert.
	 */
	public static final String OID_AUTHORITY_KEY_IDENTIFIER   = "2.5.29.35";

	/**
	 * X.509 OID for Issuer Alternative Name - Omitted when duplicating a cert by default.
	 */
	public static final String OID_ISSUER_ALTERNATIVE_NAME    = "2.5.29.8";

	/**
	 * X.509 OID for Issuer Alternative Name 2 - Omitted when duplicating a cert by default.
	 */
	public static final String OID_ISSUER_ALTERNATIVE_NAME_2  = "2.5.29.18";

	/**
	 * X.509 OID for Certificate Revocation List Distribution Point - Omitted when duplicating a cert by default.
	 */
	public static final String OID_CRL_DISTRIBUTION_POINT     = "2.5.28.31";

	/**
	 * X.509 OID for Authority Information Access - Omitted when duplicating a cert by default.
	 */
	public static final String OID_AUTHORITY_INFO_ACCESS      = "1.3.6.1.5.5.7.1.1";

	/**
	 * X.509 OID for Additional CA Issuers for AIA - Omitted when duplicating a cert by default.
	 */
	public static final String OID_ID_AD_CAISSUERS            = "1.3.6.1.5.5.7.48.2";


	static
	{
		clientCertOidsNeverToCopy.add(OID_SUBJECT_KEY_IDENTIFIER);
		clientCertOidsNeverToCopy.add(OID_AUTHORITY_KEY_IDENTIFIER);

		clientCertDefaultOidsNotToCopy.add(OID_ISSUER_ALTERNATIVE_NAME);
		clientCertDefaultOidsNotToCopy.add(OID_ISSUER_ALTERNATIVE_NAME_2);
		clientCertDefaultOidsNotToCopy.add(OID_CRL_DISTRIBUTION_POINT);
		clientCertDefaultOidsNotToCopy.add(OID_AUTHORITY_INFO_ACCESS);
	}


	/**
	 * Utility method for generating a "standard" server certificate. Recognized by most
	 * browsers as valid for SSL/TLS.  These certificates are generated de novo, not from
	 * a template, so they will not retain the structure of the original certificate and may
	 * not be suitable for applications that require Extended Validation/High Assurance SSL
	 * or other distinct extensions or EKU.
	 *
	 * @param newPubKey
	 * @param caCert
	 * @param caPrivateKey
	 * @param hostname
	 * @return
	 * @throws CertificateParsingException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws CertificateExpiredException
	 * @throws CertificateNotYetValidException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	@SuppressWarnings({ "deprecation", "unused" })
    public static X509Certificate generateStdSSLServerCertificate(
			final PublicKey newPubKey,
			final X509Certificate caCert,
			final PrivateKey caPrivateKey,
			final String subject)
	throws 	CertificateParsingException,
			SignatureException,
			InvalidKeyException,
			CertificateExpiredException,
			CertificateNotYetValidException,
			CertificateException,
			NoSuchAlgorithmException,
			NoSuchProviderException
	{
		X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();

		v3CertGen.setSubjectDN(new X500Principal(subject));
		v3CertGen.setSignatureAlgorithm(CertificateCreator.SIGN_ALGO);
		v3CertGen.setPublicKey(newPubKey);
		v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 30L * 60 * 60 * 24 * 30 * 12));
		v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30 *12));
		v3CertGen.setIssuerDN(caCert.getSubjectX500Principal());

		// Firefox actually tracks serial numbers within a CA and refuses to validate if it sees duplicates
		// This is not a secure serial number generator, (duh!) but it's good enough for our purposes.
		v3CertGen.setSerialNumber(new BigInteger(Long.toString(System.currentTimeMillis())));

		v3CertGen.addExtension(
				X509Extensions.BasicConstraints,
				true,
				new BasicConstraints(false) );

		v3CertGen.addExtension(
				X509Extensions.SubjectKeyIdentifier,
				false,
				new SubjectKeyIdentifierStructure(newPubKey));


		v3CertGen.addExtension(
				X509Extensions.AuthorityKeyIdentifier,
				false,
				new AuthorityKeyIdentifierStructure(caCert.getPublicKey()));

// 		Firefox 2 disallows these extensions in an SSL server cert.  IE7 doesn't care.
//		v3CertGen.addExtension(
//				X509Extensions.KeyUsage,
//				false,
//				new KeyUsage(KeyUsage.dataEncipherment | KeyUsage.digitalSignature ) );


		DEREncodableVector typicalSSLServerExtendedKeyUsages = new DEREncodableVector();

		typicalSSLServerExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.serverAuth));
		typicalSSLServerExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.clientAuth));
		typicalSSLServerExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.netscapeServerGatedCrypto));
		typicalSSLServerExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.msServerGatedCrypto));

		v3CertGen.addExtension(
				X509Extensions.ExtendedKeyUsage,
				false,
				new DERSequence(typicalSSLServerExtendedKeyUsages));

//  Disabled by default.  Left in comments in case this is desired.
//
//		v3CertGen.addExtension(
//				X509Extensions.AuthorityInfoAccess,
//				false,
//				new AuthorityInformationAccess(new DERObjectIdentifier(OID_ID_AD_CAISSUERS),
//						new GeneralName(GeneralName.uniformResourceIdentifier, "http://" + subject + "/aia")));

//		v3CertGen.addExtension(
//				X509Extensions.CRLDistributionPoints,
//				false,
//				new CRLDistPoint(new DistributionPoint[] {}));



		X509Certificate cert = v3CertGen.generate(caPrivateKey, "BC");

		return cert;
	}

	/**
	 * This method creates an X509v3 certificate based on an an existing certificate.
	 * It attempts to create as faithful a copy of the existing certificate as possible
	 * by duplicating all certificate extensions.
	 *
	 * If you are testing an application that makes use of additional certificate
	 * extensions (e.g. logotype, S/MIME capabilities) this method will preserve those
	 * fields.
	 *
	 * You may optionally include a set of OIDs not to copy from the original certificate.
	 * The most common reason to do this would be to remove fields that would cause inconsistency,
	 * such as Authority Info Access or Issuer Alternative Name where these are not defined for
	 * the MITM authority certificate.
	 *
	 * OIDs 2.5.29.14 : Subject Key Identifier and 2.5.29.35 : Authority Key Identifier,
	 * are never copied, but generated directly based on the input keys and certificates.
	 *
	 * You may also optionally include maps of custom extensions which will be added to or replace
	 * extensions with the same OID on the original certificate for the the MITM certificate.
	 *
	 * FUTURE WORK: JDK 1.5 is very strict in parsing extensions.  In particular, known extensions
	 * that include URIs must parse to valid URIs (including URL encoding all non-valid URI characters)
	 * or the extension will be rejected and not available to copy to the MITM certificate.  Will need
	 * to directly extract these as ASN.1 fields and re-insert (hopefully BouncyCastle will handle them)
	 *
	 *
	 * @param originalCert  The original certificate to duplicate.
	 * @param newPubKey     The new public key for the MITM certificate.
	 * @param caCert        The certificate of the signing authority fot the MITM certificate.
	 * @param caPrivateKey  The private key of the signing authority.
	 * @param extensionOidsNotToCopy  An optional list of certificate extension OIDs not to copy to the MITM certificate.
	 * @return The new MITM certificate.
	 * @throws CertificateParsingException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws CertificateExpiredException
	 * @throws CertificateNotYetValidException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static X509Certificate mitmDuplicateCertificate(final X509Certificate originalCert,
			final PublicKey newPubKey,
			final X509Certificate caCert,
			final PrivateKey caPrivateKey,
			Set<String> extensionOidsNotToCopy)
	throws 	CertificateParsingException,
			SignatureException,
			InvalidKeyException,
			CertificateException,
			NoSuchAlgorithmException,
			NoSuchProviderException
	{
		if(extensionOidsNotToCopy == null)
		{
			extensionOidsNotToCopy = new HashSet<String>();
		}

		X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();

		v3CertGen.setSubjectDN(originalCert.getSubjectX500Principal());
		v3CertGen.setSignatureAlgorithm(CertificateCreator.SIGN_ALGO); // needs to be the same as the signing cert, not the copied cert
		v3CertGen.setPublicKey(newPubKey);
		v3CertGen.setNotAfter(originalCert.getNotAfter());
		v3CertGen.setNotBefore(originalCert.getNotBefore());
		v3CertGen.setIssuerDN(caCert.getSubjectX500Principal());
		v3CertGen.setSerialNumber(originalCert.getSerialNumber());

		// copy other extensions:
		Set<String> critExts = originalCert.getCriticalExtensionOIDs();

		// get extensions returns null, not an empty set!
		if(critExts != null) {
			for (String oid : critExts) {
				if(!clientCertOidsNeverToCopy.contains(oid)
						&& !extensionOidsNotToCopy.contains(oid)) {
					v3CertGen.copyAndAddExtension(new DERObjectIdentifier(oid), true, originalCert);
				}
			}
		}
		Set<String> nonCritExs = originalCert.getNonCriticalExtensionOIDs();

		if(nonCritExs != null) {
			for(String oid: nonCritExs) {

				if(!clientCertOidsNeverToCopy.contains(oid)
						&& !extensionOidsNotToCopy.contains(oid)){
					v3CertGen.copyAndAddExtension(new DERObjectIdentifier(oid), false, originalCert);
				}
			}
		}

		v3CertGen.addExtension(
				X509Extensions.SubjectKeyIdentifier,
				false,
				new SubjectKeyIdentifierStructure(newPubKey));


		v3CertGen.addExtension(
				X509Extensions.AuthorityKeyIdentifier,
				false,
				new AuthorityKeyIdentifierStructure(caCert.getPublicKey()));

		X509Certificate cert = v3CertGen.generate(caPrivateKey, "BC");

		// For debugging purposes.
		//cert.checkValidity(new Date());
		//cert.verify(caCert.getPublicKey());

		return cert;
	}

	/**
	 * Convenience method for the most common case of certificate duplication.
	 *
	 *  This method will not add any custom extensions and won't copy the extensions 2.5.29.8 : Issuer Alternative Name,
	 * 	2.5.29.18 : Issuer Alternative Name 2, 2.5.29.31 : CRL Distribution Point or 1.3.6.1.5.5.7.1.1 : Authority Info Access, if they are present.
	 *
	 * @param originalCert
	 * @param newPubKey
	 * @param caCert
	 * @param caPrivateKey
	 * @return
	 * @throws CertificateParsingException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws CertificateExpiredException
	 * @throws CertificateNotYetValidException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static X509Certificate mitmDuplicateCertificate(final X509Certificate originalCert,
			final PublicKey newPubKey,
			final X509Certificate caCert,
			final PrivateKey caPrivateKey)
	throws CertificateParsingException, SignatureException, InvalidKeyException, CertificateExpiredException, CertificateNotYetValidException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException
	{
		return mitmDuplicateCertificate(originalCert, newPubKey, caCert, caPrivateKey, clientCertDefaultOidsNotToCopy);
	}

	/**
	 * Creates a typical Certification Authority (CA) certificate.
	 * @param keyPair
	 * @throws SecurityException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	@SuppressWarnings("deprecation")
    public static X509Certificate createTypicalMasterCert(final KeyPair keyPair)
	throws SignatureException, InvalidKeyException, SecurityException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException
	{

		X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();

        // BEGIN ODO CHANGES
        // Added the Common Name "CN=CyberVillains CA" to the X.509 Distinguished Name below.
        // This was added to work around a bug in iOS where certificates that lack Common Name's
        // do not show up in the list of CA certificates found in Settings / General / About / Certificate Trust Settings.
        // We needed this CA certificate to show up in this list so that we could manually trust it and therefore
        // avoid the App Transport Security "Untrusted root certificate" errors.
		X509Principal issuer=new X509Principal("CN=CyberVillains CA,OU=CyberVillains Certification Authority,O=CyberVillains.com,C=US");
		// END ODO CHANGES

		// Create
		v3CertGen.setSerialNumber(BigInteger.valueOf(1));
		v3CertGen.setIssuerDN(issuer);
		v3CertGen.setSubjectDN(issuer);

		//Set validity period
		v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 12 /* months */ *(1000L * 60 * 60 * 24 * 30)));
		v3CertGen.setNotAfter (new Date(System.currentTimeMillis() + 240 /* months */ *(1000L * 60 * 60 * 24 * 30)));

		//Set signature algorithm & public key
		v3CertGen.setPublicKey(keyPair.getPublic());
		v3CertGen.setSignatureAlgorithm(CertificateCreator.SIGN_ALGO);

		// Add typical extensions for signing cert
		v3CertGen.addExtension(
				X509Extensions.SubjectKeyIdentifier,
				false,
				new SubjectKeyIdentifierStructure(keyPair.getPublic()));

		v3CertGen.addExtension(
				X509Extensions.BasicConstraints,
				true,
				new BasicConstraints(0));

		v3CertGen.addExtension(
				X509Extensions.KeyUsage,
				false,
				new KeyUsage(KeyUsage.cRLSign | KeyUsage.keyCertSign) );

		DEREncodableVector typicalCAExtendedKeyUsages = new DEREncodableVector();

		typicalCAExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.serverAuth));
		typicalCAExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.OCSPSigning));
		typicalCAExtendedKeyUsages.add(new DERObjectIdentifier(ExtendedKeyUsageConstants.verisignUnknown));

		v3CertGen.addExtension(
				X509Extensions.ExtendedKeyUsage,
				false,
				new DERSequence(typicalCAExtendedKeyUsages));

		X509Certificate cert = v3CertGen.generate(keyPair.getPrivate(), "BC");

		cert.checkValidity(new Date());

		cert.verify(keyPair.getPublic());

		return cert;
	}

}
