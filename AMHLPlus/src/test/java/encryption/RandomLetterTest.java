package encryption;


import org.junit.Test;

public class RandomLetterTest {
    @Test
    public void sha512Test() {
        String result = RandomLetter.randomLetter(10);
        System.out.println("result = " + result);
    }
}
