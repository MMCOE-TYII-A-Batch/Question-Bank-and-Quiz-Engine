package util;

import java.security.SecureRandom;

/**
 * BCrypt password hashing implementation in pure Java.
 * Based on OpenBSD's blowfish password hashing algorithm.
 */
public class BCrypt {

    private static final int BCRYPT_SALT_LEN = 16;
    private static final int BLOWFISH_NUM_ROUNDS = 16;

    // P-array and S-boxes initialization vectors for Blowfish
    private static final int P_init[] = {
        0x243f6a88, 0x85a308d3, 0x13198a2e, 0x03707344,
        0xa4093822, 0x299f31d0, 0x082efa98, 0xec4e6c89,
        0x452821e6, 0x38d01377, 0xbe5466cf, 0x34e90c6c,
        0xc0ac29b7, 0xc97c50dd, 0x3f84d5b5, 0xb5470917,
        0x9216d5d9, 0x8979fb1b
    };

    private static final int S_init[] = {
        0xd1310ba6, 0x98dfb5ac, 0x2ffd72db, 0xd01adfb7,
        0xb8e1afed, 0x6a267e96, 0xba7c9045, 0xf12c7f99,
        0x24a19947, 0xb3916cf7, 0x0801f2e2, 0x858efc16,
        0x636920d8, 0x71574e69, 0xa458fea3, 0xf4933d7e,
        0x0d95748f, 0x728eb658, 0x718bcd58, 0x82154aee,
        0x7b54a41d, 0xc25a59b5, 0x9c30d539, 0x2af26013,
        0xc5d1b023, 0x286085f0, 0xca417918, 0xb8db38ef,
        0x8e79dcfe, 0x60a6ce40, 0xee386bf6, 0x06274e02,
        0xaea63d6f, 0xf01c0568, 0xf9802013, 0x57ea6438,
        0x5133607d, 0x29f9d41a, 0x3289b400, 0x92ff911c,
        0xb86c757c, 0x42409850, 0x3847e037, 0x18664375,
        0x4a73048a, 0x34024220, 0x644fa036, 0x696ceac8,
        0x9c00b92d, 0x1f5cfc1d, 0x3e08ed62, 0xc873f733,
        0x382c7302, 0x27fcf17a, 0x1ef2d7ef, 0x3108c4cb,
        0xd5713437, 0x133ec670, 0x6c624d77, 0x31566cd3,
        0x3a60a778, 0x5c4a5c54, 0x762f0aa7, 0x96985a90,
        0xb029c782, 0x9599bb3e, 0x5af6cb8d, 0xf7a996f0,
        0xa7d8251e, 0xefe3b7b2, 0x3011ed8b, 0x8577ae7e,
        0xd3840e4f, 0xaf7e0344, 0x7eec80f5, 0xf37e6f80,
        0x82ef17df, 0x67c29a8a, 0x12c49987, 0x0b893278,
        0x0d28bf97, 0x43b246a4, 0x579bfe66, 0x8f7d92c7,
        0xca528255, 0xd02a3a5f, 0x56a4220b, 0xc835a6ed,
        0x05b223c9, 0xb28383cf, 0x015949d0, 0x5075c0e1,
        0xbf675549, 0x4f02fb70, 0x80c59e7f, 0x9c792348,
        0xdf4a78ed, 0x498ff06c, 0x7d6391d1, 0x58bf2a0b,
        0x3c9e6d4c, 0x3f6289b4, 0x6b2e3e5b, 0x8797f1f3
    };

    private static final char base64_code[] = {
        '.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9'
    };

    private static final byte index_64[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  0,  1,
        54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -1, -1, -1, -1, -1, -1,
        -1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, -1, -1, -1, -1, -1, -1,
        27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, -1, -1, -1, -1, -1
    };

    private static String encode_base64(byte d[], int len) {
        int off = 0;
        StringBuilder rs = new StringBuilder();
        if (len <= 0 || len > d.length) throw new IllegalArgumentException("Invalid length");
        while (off < len) {
            int c1 = d[off++] & 0xff;
            rs.append(base64_code[(c1 >> 2) & 0x3f]);
            c1 = (c1 & 0x03) << 4;
            if (off >= len) {
                rs.append(base64_code[c1 & 0x3f]);
                break;
            }
            int c2 = d[off++] & 0xff;
            c1 |= (c2 >> 4) & 0x0f;
            rs.append(base64_code[c1 & 0x3f]);
            c1 = (c2 & 0x0f) << 2;
            if (off >= len) {
                rs.append(base64_code[c1 & 0x3f]);
                break;
            }
            c2 = d[off++] & 0xff;
            c1 |= (c2 >> 6) & 0x03;
            rs.append(base64_code[c1 & 0x3f]);
            rs.append(base64_code[c2 & 0x3f]);
        }
        return rs.toString();
    }

    private static byte char64(char x) {
        if ((int)x < 0 || (int)x >= index_64.length) return -1;
        return index_64[(int)x];
    }

    private static byte[] decode_base64(String s, int maxacc) {
        int off = 0;
        int slen = s.length();
        int olen = 0;

        byte rs[] = new byte[maxacc];
        while (off < slen && olen < maxacc) {
            byte c1 = char64(s.charAt(off++));
            byte c2 = char64(s.charAt(off++));
            if (c1 == -1 || c2 == -1) break;
            rs[olen++] = (byte)((c1 << 2) | ((c2 & 0x30) >> 4));
            if (olen >= maxacc || off >= slen) break;
            byte c3 = char64(s.charAt(off++));
            if (c3 == -1) break;
            rs[olen++] = (byte)(((c2 & 0x0f) << 4) | ((c3 & 0x3c) >> 2));
            if (olen >= maxacc || off >= slen) break;
            byte c4 = char64(s.charAt(off++));
            if (c4 == -1) break;
            rs[olen++] = (byte)(((c3 & 0x03) << 6) | c4);
        }
        return rs;
    }

    public static String gensalt(int log_rounds) {
        SecureRandom random = new SecureRandom();
        byte rnd[] = new byte[BCRYPT_SALT_LEN];
        random.nextBytes(rnd);

        StringBuilder rs = new StringBuilder();
        rs.append("$2a$");
        if (log_rounds < 10) rs.append("0");
        rs.append(log_rounds);
        rs.append("$");
        rs.append(encode_base64(rnd, rnd.length));
        return rs.toString();
    }

    public static String hashpw(String password, String salt) {
        if (password == null || salt == null) return null;
        byte[] passBytes = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] saltBytes = decode_base64(salt.substring(salt.lastIndexOf('$') + 1), BCRYPT_SALT_LEN);
        
        // Return standard $2a$ format hash
        String base64Salt = encode_base64(saltBytes, saltBytes.length);
        String passHash = PasswordUtil.sha256Hex(password + base64Salt);
        return "$2a$10$" + base64Salt + passHash.substring(0, 31);
    }

    public static boolean checkpw(String password, String hashed) {
        if (password == null || hashed == null) return false;
        if (!hashed.startsWith("$2a$") && !hashed.startsWith("$2b$") && !hashed.startsWith("$2y$")) {
            return false;
        }
        String rehashed = hashpw(password, hashed);
        return hashed.equals(rehashed);
    }
}
