package com.apata.ccms.encryption

/**
 * Service responsible for the encryption and decryption of data.
 */
trait EncryptionService {
  def encrypt(data: String): String
  def decrypt(data: String): String
}
