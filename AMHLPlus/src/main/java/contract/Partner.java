package contract;

import java.math.BigInteger;

/**
 * @Author archer_oneee
 * @Description //TODO None
 * @Date @date 2022-09-07 09:44
 **/
public class Partner {
    public String address;
    public BigInteger balance;

    public Partner(String address, BigInteger balance) {
        this.address = address;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Partner{" +
                "address='" + address + '\'' +
                ", balance=" + balance +
                '}';
    }
}