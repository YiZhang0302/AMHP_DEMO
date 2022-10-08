package contract;

import java.math.BigInteger;

/**
 * @Author archer_oneee
 * @Description //TODO None
 * @Date @date 2022-09-07 09:44
 **/
public class ChannelState {
    int sequence;
    BigInteger amount;

    public Partner partnerA;
    public Partner partnerB;

    public ChannelState(Partner partnerA, Partner partnerB) {
        this.partnerA = partnerA;
        this.partnerB = partnerB;
        this.sequence = 0;
        this.amount = partnerA.balance.add(partnerB.balance);
    }

    public void changeState(BigInteger newPartnerABalance, BigInteger newPartnerBBalance){
        assert (this.amount.equals(newPartnerABalance.add(newPartnerBBalance)));

        this.sequence += 1;
        this.partnerA.balance = newPartnerABalance;
        this.partnerB.balance = newPartnerBBalance;
    }

    @Override
    public String toString() {
        return "ChannelState{" +
                "sequence=" + sequence +
                ", amount=" + amount +
                ", partnerA=" + partnerA +
                ", partnerB=" + partnerB +
                '}';
    }
}

