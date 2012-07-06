package crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** This utility class implements the HMAC algorithm (using SHA1) as described by RFC 2104. */
public class HMacSha1
{
  /** Algorithm name */
  private static final String ALGORITHM = "HmacSHA1";

  /**
   * Performs a HMAC using SHA1, output is encoded in hexadecimal.
   * @param secretKey the secret key used for hashing the data.
   * @param data the data to securely hash.
   * @return a hexadecimal digest.
   * @throws IllegalArgumentException if secret key is empty.
   */
  public static String asHexDigest(String secretKey, String data)
  {
    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
    Mac mac;
    try
    {
      mac = Mac.getInstance(ALGORITHM);
    }
    catch (NoSuchAlgorithmException nsae)
    {
      /* Won't happen since HmacSHA1 is supported */
      throw new RuntimeException(nsae);
    }
    try
    {
      mac.init(secretKeySpec);
    }
    catch (InvalidKeyException ike)
    {
      /*
       * Won't happen, new SecretKeySpec will already throw IllegalArgumentException or
       * NullPointerException.
       */
      throw new IllegalArgumentException("Unsuitable secret key", ike);
    }
    byte[] rawHmac = mac.doFinal(data.getBytes());
    return Hex.encode(rawHmac);
  }
}
