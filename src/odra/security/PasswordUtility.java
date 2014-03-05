package odra.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;
import sun.misc.CharacterEncoder;

public final class PasswordUtility {

   private PasswordUtility() {
   }

   public static String getPasswordDigest(String password) throws NoSuchAlgorithmException,
         UnsupportedEncodingException {
      MessageDigest md = MessageDigest.getInstance("SHA");
      md.update(password.getBytes("UTF-8"));
      CharacterEncoder encoder = new BASE64Encoder();
      return encoder.encode(md.digest());
   }
}
