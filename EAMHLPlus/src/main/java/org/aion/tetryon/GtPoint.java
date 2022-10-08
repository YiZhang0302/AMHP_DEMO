package org.aion.tetryon;

import java.math.BigInteger;

/**
 * @Author archer_oneee
 * @Description //TODO None
 * @Date @date 2022-08-11 09:49
 **/
public class GtPoint {
    public final Fp[] ci;

    public GtPoint(Fp[] ci) {
        this.ci = ci;
    }

    @Override
    public String toString() {
        String s = new String();
        for (int i = 0; i < 12; i++) {
            Fp c_item = this.ci[i];
            s += (c_item.toString() + "\n");
        }
        return s;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GtPoint that = (GtPoint) o;
        for (int i = 0; i < 12; i++) {


            if (!this.ci[i].equals(that.ci[i]) ){
                return false;
            }

        }
        return true;
    }



}
