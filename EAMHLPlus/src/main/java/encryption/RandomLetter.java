package encryption;

import org.apache.commons.lang.RandomStringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RandomLetter {
    /**
     * @return the base64 encoded time hash data
     */
    public static String randomLetter(int len) {
        try {

            String letters = RandomStringUtils.randomAlphanumeric(len);
            String resutStr1 = Base64.getEncoder().encodeToString(letters.getBytes(StandardCharsets.UTF_8));
            return letters;


            //Random rand = new Random();
            //String nowTime = String.valueOf(System.currentTimeMillis() + rand.nextInt(9999999));
            //// 创建一个MessageDigest实例:
            //MessageDigest md = MessageDigest.getInstance("SHA-512");
            //// 反复调用update输入数据:
            //md.update(nowTime.getBytes("UTF-8"));
            //byte[] result = md.digest();
            //String resutStr1 = Base64.getEncoder().encodeToString(new BigInteger(1, result).toString(2).getBytes(StandardCharsets.UTF_8));
            //
            //
            //nowTime = String.valueOf(System.currentTimeMillis() + rand.nextInt(9999999));
            //md.update(nowTime.getBytes("UTF-8"));
            //result = md.digest();
            //String resutStr2 = Base64.getEncoder().encodeToString(new BigInteger(1, result).toString(2).getBytes(StandardCharsets.UTF_8));
            //String finalStr = resutStr1 + resutStr2;
            //System.out.println(finalStr);
            //return finalStr;

        } catch (Exception e) {
            System.out.println("sha512 error!");
            return null;
        }

    }
}
