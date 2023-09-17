package mk.ukim.finki.ibproekt.service;

import mk.ukim.finki.ibproekt.model.Totp;
import mk.ukim.finki.ibproekt.repository.TotpRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class TOTPService {
    private final TotpRepository totpRepository;


    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1,10,100,1000,10000,100000,1000000,10000000,100000000 };

    private static final String RETURN_DIGITS = "6";
    private static final String CRYPTO = "HmacSHA512";
    private static final long T0 = 0;
    private static final long X = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public TOTPService(TotpRepository totpRepository) {
        this.totpRepository = totpRepository;
    }


    private static String generateSeed() {
        int desiredLength = 128;

        StringBuilder randomString = new StringBuilder(desiredLength);

        for (int i = 0; i < desiredLength; i++) {
            int randomDigit = SECURE_RANDOM.nextInt(10);
            randomString.append(randomDigit);
        }

        return randomString.toString();
    }

    private static byte[] hmac_sha(String crypto, byte[] keyBytes,
                                   byte[] text){
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey =
                    new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }



    private static byte[] hexStr2Bytes(String hex){
        byte[] bArray = new BigInteger("10" + hex,16).toByteArray();

        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    public String generateTOTP(){
        String seed64 = generateSeed();
        long T = (SECURE_RANDOM.nextLong() - T0)/X;
        String steps = Long.toHexString(T).toUpperCase();

        int codeDigits = Integer.decode(RETURN_DIGITS);
        StringBuilder result;

        StringBuilder timeBuilder = new StringBuilder(steps);
        while (timeBuilder.length() < 16 )
            timeBuilder.insert(0, "0");
        steps = timeBuilder.toString();

        byte[] msg = hexStr2Bytes(steps);
        byte[] k = hexStr2Bytes(seed64);

        byte[] hash = hmac_sha(CRYPTO, k, msg);

        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.insert(0, "0");
        }

        totpRepository.save(new Totp(result.toString(), ZonedDateTime.now()));

        return result.toString();
    }

    private boolean verifyTime (ZonedDateTime dateIssued) {
        ZonedDateTime currentTimestamp = ZonedDateTime.now();

        Duration duration = Duration.between(dateIssued, currentTimestamp);


        return duration.toMinutes() <= 1;
    }

    public Map<String, String> verifyTOTP(String userTOTP) {
        Totp dbTOPT = totpRepository.findByTotp(userTOTP);
        if (dbTOPT != null && userTOTP.equals(dbTOPT.getTotp()) && verifyTime(dbTOPT.getDateIssued())) {
            return Map.of("message", "success");
        }

        return Map.of("message", "error");
    }
}
