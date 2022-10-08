package org.aion.tetryon;

import java.math.BigInteger;

/**
 * @Author archer_oneee
 * @Description //TODO None
 * @Date @date 2022-08-11 09:49
 **/
public class Gt {


    public static GtPoint gtPow(GtPoint point, BigInteger exp) throws Exception {
        byte[] gdata = Util.serializeGt(point);
        byte[] resultData = AltBn128.gtPow(gdata, exp);
        GtPoint result = Util.deserializeGt(resultData);
        return result;
    }



}
