package org.jdkstack.jdkserver.tcp.core.ssl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Arrays;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class PrivateKeyParser {
  private static final byte[] OID_RSA_PUBLIC_KEY = {
    0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01
  };
  private static final byte[] OID_EC_PUBLIC_KEY = {
    0x2A, (byte) 0x86, 0x48, (byte) 0xCE, 0x3D, 0x02, 0x01
  };

  public static String getPKCS8EncodedKeyAlgorithm(byte[] encodedKey) {
    DerParser parser = new DerParser(encodedKey);
    Asn1Object sequence = parser.read();
    if (sequence.getType() != DerParser.SEQUENCE) {
      throw new StudyException("Invalid PKCS8 encoding: not a sequence");
    }
    parser = sequence.getParser();
    BigInteger version = parser.read().getInteger();
    if (version.intValue() != 0) {
      throw new StudyException("Unsupported version, expected 0 but found " + version.intValue());
    }
    sequence = parser.read();
    if (sequence.getType() != DerParser.SEQUENCE) {
      throw new StudyException("Invalid PKCS8 encoding: could not read Algorithm Identifier");
    }
    parser = sequence.getParser();
    byte[] algorithmIdentifier = parser.read().getObjectIdentifier();
    if (Arrays.equals(OID_RSA_PUBLIC_KEY, algorithmIdentifier)) {
      return "RSA";
    } else if (Arrays.equals(OID_EC_PUBLIC_KEY, algorithmIdentifier)) {
      return "EC";
    } else {
      throw new StudyException("Unsupported algorithm identifier");
    }
  }

  public static RSAPrivateCrtKeySpec getRSAKeySpec(byte[] keyBytes) throws StudyException {
    DerParser parser = new DerParser(keyBytes);

    Asn1Object sequence = parser.read();
    if (sequence.getType() != DerParser.SEQUENCE) {
      throw new StudyException("Invalid DER: not a sequence");
    }
    parser = sequence.getParser();
    parser.read();
    BigInteger modulus = parser.read().getInteger();
    BigInteger publicExp = parser.read().getInteger();
    BigInteger privateExp = parser.read().getInteger();
    BigInteger prime1 = parser.read().getInteger();
    BigInteger prime2 = parser.read().getInteger();
    BigInteger exp1 = parser.read().getInteger();
    BigInteger exp2 = parser.read().getInteger();
    BigInteger crtCoef = parser.read().getInteger();

    return new RSAPrivateCrtKeySpec(
        modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);
  }

  static class DerParser {
    private static final int UNIVERSAL = 0x00;
    private static final int APPLICATION = 0x40;
    private static final int CONTEXT = 0x80;
    private static final int PRIVATE = 0xC0;
    private static final int CONSTRUCTED = 0x20;
    private static final int ANY = 0x00;
    private static final int BOOLEAN = 0x01;
    private static final int INTEGER = 0x02;
    private static final int BIT_STRING = 0x03;
    private static final int OCTET_STRING = 0x04;
    private static final int NULL = 0x05;
    private static final int OBJECT_IDENTIFIER = 0x06;
    private static final int REAL = 0x09;
    private static final int ENUMERATED = 0x0a;
    private static final int SEQUENCE = 0x10;
    private static final int SET = 0x11;
    private static final int NUMERIC_STRING = 0x12;
    private static final int PRINTABLE_STRING = 0x13;
    private static final int VIDEOTEX_STRING = 0x15;
    private static final int IA5_STRING = 0x16;
    private static final int GRAPHIC_STRING = 0x19;
    private static final int ISO646_STRING = 0x1A;
    private static final int GENERAL_STRING = 0x1B;
    private static final int UTF8_STRING = 0x0C;
    private static final int UNIVERSAL_STRING = 0x1C;
    private static final int BMP_STRING = 0x1E;
    private static final int UTC_TIME = 0x17;
    private byte[] in;
    private int pos;

    DerParser(byte[] in) throws StudyException {
      this.in = in;
    }

    private int readByte() throws StudyException {
      if (pos + 1 >= in.length) {
        throw new StudyException("Invalid DER: stream too short, missing tag");
      }
      return in[pos++];
    }

    private byte[] readBytes(int len) throws StudyException {
      if (pos + len > in.length) {
        throw new StudyException("Invalid DER: stream too short, missing tag");
      }
      byte[] in1 = new byte[len];
      for (int i = pos; i < pos + len; i++) {
        in1[i] = in[i];
      }
      pos += len;
      return in1;
    }

    public Asn1Object read() throws StudyException {
      int tag = readByte();
      int length = getLength();
      byte[] value = readBytes(length);
      return new Asn1Object(tag, length, value);
    }

    private int getLength() throws StudyException {

      int i = readByte();

      // A single byte short length
      if ((i & ~0x7F) == 0) {
        return i;
      }

      int num = i & 0x7F;

      // We can't handle length longer than 4 bytes
      if (i >= 0xFF || num > 4) {
        throw new StudyException("Invalid DER: length field too big (" + i + ")");
      }

      byte[] bytes = readBytes(num);
      return new BigInteger(1, bytes).intValue();
    }
  }

  static class Asn1Object {
    protected final int type;
    protected final int length;
    protected final byte[] value;
    protected final int tag;

    public Asn1Object(int tag, int length, byte[] value) {
      this.tag = tag;
      this.type = tag & 0x1F;
      this.length = length;
      this.value = value;
    }

    public int getType() {
      return type;
    }

    public int getLength() {
      return length;
    }

    public byte[] getValue() {
      return value;
    }

    public boolean isConstructed() {
      return (tag & DerParser.CONSTRUCTED) == DerParser.CONSTRUCTED;
    }

    public DerParser getParser() throws StudyException {
      if (!isConstructed()) {
        throw new StudyException("Invalid DER: can't parse primitive entity");
      }

      return new DerParser(value);
    }

    public BigInteger getInteger() throws StudyException {
      if (type != DerParser.INTEGER) {
        throw new StudyException("Invalid DER: object is not integer");
      }

      return new BigInteger(value);
    }

    public byte[] getObjectIdentifier() throws StudyException {

      switch (type) {
        case DerParser.OBJECT_IDENTIFIER:
          return value;
        default:
          throw new StudyException("Invalid DER: object is not an Object Identifier");
      }
    }

    public String getString() throws StudyException {
      String encoding;
      switch (type) {
        case DerParser.NUMERIC_STRING:
        case DerParser.PRINTABLE_STRING:
        case DerParser.VIDEOTEX_STRING:
        case DerParser.IA5_STRING:
        case DerParser.GRAPHIC_STRING:
        case DerParser.ISO646_STRING:
        case DerParser.GENERAL_STRING:
          encoding = "ISO-8859-1";
          break;

        case DerParser.BMP_STRING:
          encoding = "UTF-16BE";
          break;

        case DerParser.UTF8_STRING:
          encoding = "UTF-8";
          break;

        case DerParser.UNIVERSAL_STRING:
          throw new StudyException("Invalid DER: can't handle UCS-4 string");

        default:
          throw new StudyException("Invalid DER: object is not a string");
      }

      try {
        return new String(value, encoding);
      } catch (UnsupportedEncodingException e) {
        throw new StudyException(e);
      }
    }
  }
}
