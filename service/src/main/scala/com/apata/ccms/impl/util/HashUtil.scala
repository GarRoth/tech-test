package com.apata.ccms.impl.util

import java.security.MessageDigest
import java.util.Base64

/**
 * Utility object for hashing related methods
 */
object HashUtil {

  /**
   * Generate a "SHA-256" hash of the provided data.
   * @param data The data to be hashed
   * @return The successfully hashed data
   */
  def hash(data: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashedBytes = digest.digest(data.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(hashedBytes)
  }

}

