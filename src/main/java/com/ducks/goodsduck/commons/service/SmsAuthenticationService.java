package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.repository.SmsAuthenticationRepository;
import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsAuthenticationService {

    private final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();
    private String apiKey = jsonOfAwsSecrets.optString("coolsms.apikey", "");
    private String apiSecret = jsonOfAwsSecrets.optString("coolsms.apisecret", "");
    private String senderNumber = jsonOfAwsSecrets.optString("coolsms.sendernumber", "");

    private final SmsAuthenticationRepository smsAuthenticationRepository;

    public Boolean sendSmsOfAuthentication(String phoneNumber) throws Exception {
        if (jsonOfAwsSecrets.isEmpty()) {
            apiKey = PropertyUtil.getProperty("coolsms.apikey");
            apiSecret = PropertyUtil.getProperty("coolsms.apisecret");
            senderNumber = PropertyUtil.getProperty("coolsms.sendernumber");
        }

        Message coolsms = new Message(apiKey, apiSecret);
        String authenticationNumber = generateAuthenticationNumber();

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<>();
        params.put("to", phoneNumber);    // 수신전화번호
        params.put("from", senderNumber);    // 발신전화번호. 테스트시에는 발신,수신 둘다 본인 번호로 하면 됨
        params.put("type", "SMS");
        params.put("text", "굿즈덕(GOODSDUCK) 인증번호는 \"" + authenticationNumber + "\" 입니다.");
        params.put("app_version", "goodsduck app 1.0"); // application name and version

        try {
            org.json.simple.JSONObject sendedMessage = coolsms.send(params);
            log.debug("Send message from CoolSMS: {}", sendedMessage.toString());
            if (!sendedMessage.get("error_count").equals(0L)) {
                return false;
            }
            smsAuthenticationRepository.saveKeyAndValue(phoneNumber, authenticationNumber);
        } catch (CoolsmsException e) {
            throw new CoolsmsException("Exception of CoolSMS: " + e.getMessage(), e.getCode());
        } catch (Exception e) {
            throw new Exception("Exception occurred in sending CoolSms: " + e.getMessage());
        }
        return true;
    }

    public Boolean authenticate(String phoneNumber, String authenticationNumber) {
        if (smsAuthenticationRepository.hasKey(phoneNumber)) {
            if (smsAuthenticationRepository.getValueByPhoneNumber(phoneNumber).equals(authenticationNumber)) {
                smsAuthenticationRepository.removeKeyAndValue(phoneNumber);
                return true;
            }
        }

        return false;
    }

    public String generateAuthenticationNumber() {
        Random random  = new Random();
        String randomNumbers = "";
        for(int i=0; i<6; i++) {
            String randomNumber = Integer.toString(random.nextInt(10));
            randomNumbers = randomNumbers.concat(randomNumber);
        }
        return randomNumbers;
    }
}
