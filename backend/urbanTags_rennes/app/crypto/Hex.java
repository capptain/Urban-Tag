package crypto;

public final class Hex
{
  private Hex()
  {
  }

  public static String encode(byte[] bytes)
  {
    StringBuilder hex = new StringBuilder(bytes.length * 2);
    for (byte aByte : bytes)
    {
      if ((aByte & 0xff) < 0x10)
        hex.append("0");
      hex.append(Integer.toString(aByte & 0xff, 16));
    }
    return hex.toString();
  }

  public static byte[] decode(CharSequence data)
  {
    int len = data.length();
    byte[] out = new byte[len >> 1];
    for (int i = 0, j = 0; j < len; i++)
    {
      int f = Character.digit(data.charAt(j), 16) << 4;
      j++;
      f = f | Character.digit(data.charAt(j), 16);
      j++;
      out[i] = (byte) (f & 0xff);
    }
    return out;
  }
}
