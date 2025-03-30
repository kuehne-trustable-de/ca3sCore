package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.exception.ResendProhibitException;
import de.trustable.ca3s.core.exception.SMSSendingFaiedException;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
public class SMSService {

    final private ProtectedContentUtil protectedContentUtil;
    final private BPMNUtil bpmnUtil;

    Random random = new SecureRandom();
    public SMSService(ProtectedContentUtil protectedContentUtil, BPMNUtil bpmnUtil) {
        this.protectedContentUtil = protectedContentUtil;
        this.bpmnUtil = bpmnUtil;
    }

    public String sendPIN_SMS(User user){

        if( user.getPhone() == null || user.getPhone().isEmpty()){
            throw new SMSSendingFaiedException("user #{} has no phone number defined");
        }

        List<ProtectedContent> protectedContents = protectedContentUtil.retrieveProtectedContent(
            ProtectedContentType.SECRET,
            ContentRelationType.SMS_PIN,
            user.getId());

        Instant now = Instant.now();
        Instant resendProhibitInstant = now.minus(1, ChronoUnit.MINUTES);
        for( ProtectedContent pc: protectedContents){
            if(pc.getCreatedOn().isAfter(resendProhibitInstant)){
                throw new ResendProhibitException();
            }
        }

        String msg = String.format("%06d", random.nextInt(999999));
        protectedContentUtil.createProtectedContent(
            msg,
            ProtectedContentType.SECRET,
            ContentRelationType.SMS_PIN,
            user.getId(),
            5,
            now.plus(5, ChronoUnit.MINUTES));

        bpmnUtil.startSMSProcess(user.getPhone(), msg);

        return msg;
    }

    public void checkSMS(User user, final String smsValue) {
        try {
            Long.parseLong(smsValue);
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid format of SMS token");
        }
        if( verifySMS(user, smsValue)){
            return;
        }
        throw new BadCredentialsException("Invalid SMS token");
    }

    public boolean verifySMS(User user, final String smsValue){

        List<ProtectedContent> protectedContents = protectedContentUtil.retrieveProtectedContent(
            ProtectedContentType.SECRET,
            ContentRelationType.SMS_PIN,
            user.getId());

        Instant now = Instant.now();
        for( ProtectedContent pc: protectedContents){
            if( (pc.getLeftUsages() > 0 ) && (pc.getValidTo().isAfter(now))){
                if( protectedContentUtil.unprotectString(pc.getContentBase64()).trim().equals(smsValue.trim())){
                    pc.setLeftUsages(0);
                    return true;
                }
            }
        }
        return false;
    }
}
