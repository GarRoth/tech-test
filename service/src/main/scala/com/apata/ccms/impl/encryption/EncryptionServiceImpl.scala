package com.apata.ccms.impl.encryption

import com.apata.ccms.KeystoreConfig
import com.apata.ccms.encryption.EncryptionService
import com.apata.ccms.impl.util.InMemoryKeystore

import java.security.KeyStore
import java.util.Base64
import javax.crypto.{Cipher, SecretKey}

class EncryptionServiceImpl(keystoreConfig: KeystoreConfig) extends EncryptionService {

  private val keystore: KeyStore = InMemoryKeystore.createKeystore(keystoreConfig.password)
  private val secretKey = InMemoryKeystore.getKey(keystore, keystoreConfig.secretKeyAlias, keystoreConfig.password)

  def encrypt(data: String): String = {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    Base64.getEncoder.encodeToString(cipher.doFinal(data.getBytes("UTF-8")))
  }

  def decrypt(data: String): String = {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    new String(cipher.doFinal(Base64.getDecoder.decode(data)), "UTF-8")
  }
}
