package com.apata.ccms.impl.util

import java.security.KeyStore
import javax.crypto.{KeyGenerator, SecretKey}

object InMemoryKeystore {

  //TODO: Make this configurable through app conf

  def createKeystore(password: String): KeyStore = {
    val keyStore = KeyStore.getInstance("JCEKS")
    keyStore.load(null, null) //Not nice, find a cleaner way of ensuring keystore initialisation

    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(128)
    val secretKey = keyGenerator.generateKey()

    keyStore.setEntry(
      "ccmsSecretKey",
      new KeyStore.SecretKeyEntry(secretKey),
      new KeyStore.PasswordProtection(password.toCharArray)
    )

    keyStore
  }

  def getKey(keyStore: KeyStore, alias: String, password: String): SecretKey = {
    keyStore.getKey(alias, password.toCharArray).asInstanceOf[SecretKey]
  }

}
