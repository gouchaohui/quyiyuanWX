package publicservice;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

/**
 * ${DESCRIPTION}
 *
 * @author Administrator
 * @create 2017-11-13 19:55
 **/
public class Test {
    public static void main(String [] args){

        String eventPara = "qrscene_tests".substring(90);
        try {
            String encodingAesKey="llsdf790234lf02340s0fH2l40slah2002K4a720fj4";
            byte[] aesKey = Base64.decodeBase64(encodingAesKey + "=");
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
