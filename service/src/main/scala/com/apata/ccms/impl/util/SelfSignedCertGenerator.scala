package com.apata.ccms.impl.util

import java.security.cert.Certificate
import java.security.KeyStore
import java.security.cert.X509Certificate
import sun.security.tools.keytool.CertAndKeyGen
import sun.security.x509.X500Name


//TODO: If time permits, modify to not use sun.* tools, otherwise don't use https for this
object SelfSignedCertGenerator {
  def createSelfSignedCertificate(keyStore: KeyStore, alias: String, password: Array[Char]): Unit = {
    // Generate a key pair
    val keyGen = new CertAndKeyGen("RSA", "SHA256withRSA", null)
    keyGen.generate(2048)
    val privateKey = keyGen.getPrivateKey

    // Create a self-signed certificate
    val x500Name = new X500Name("CN=Apata")
    val certificate: X509Certificate = keyGen.getSelfCertificate(x500Name, 365 * 24 * 60 * 60) // 1 year validity

    // Add the certificate and private key to the keystore
    keyStore.setKeyEntry(alias, privateKey, password, Array[Certificate](certificate))
  }
}
