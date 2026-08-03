package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.service.dto.ARAContentType;
import de.trustable.ca3s.core.service.dto.ARARestriction;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.WebConfigItems;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    @Test
    void addSplittedEMailAddress() {

        List<String> testList = new ArrayList<>();
        NotificationService.addSplittedEMailAddress(testList, null);
        NotificationService.addSplittedEMailAddress(testList, "");
        NotificationService.addSplittedEMailAddress(testList, "    ");

        assertEquals(0, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "foo@bar.com");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "foo@bar.com ");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "FOO@bar.com ");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "bla foo 123456 root@localhost");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, ", foo@bar.com , ");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "foo@bar.com , test@ca3s.org");
        assertEquals(2, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "semi@bar.com ; semi@ca3s.org");
        assertEquals(4, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "blank@bar.com blank@ca3s.org");
        assertEquals(6, testList.size());


        testList = new ArrayList<>();
        NotificationService.addSplittedEMailAddress(testList, "kuehne@trustable.de, kuehne@klup.de");
        assertEquals(2, testList.size());

    }

    @Test
    void getMaxListEntry() {
        List<Integer> notificationDayList = new ArrayList<>();
        notificationDayList.add(42);
        notificationDayList.add(28);
        notificationDayList.add(14);
        notificationDayList.add(7);
        int maxExpiry = notificationDayList.stream().max(Integer::compareTo).orElse(40);


        assertEquals(42, maxExpiry);
    }


    @Test
    void findARAEmailRecipientsFromCertificate() {
        PipelineView pipelineView = new PipelineView();
        Certificate cert = new Certificate();

        ARARestriction attEmailAddress1 = buildARARestrictionAsEmailAddress("attEmailAddress1");
        ARARestriction attEmailAddress2 = buildARARestrictionAsEmailAddress("attEmailAddress2");
        ARARestriction araRestrictionNonEmail = new ARARestriction();
        araRestrictionNonEmail.setName("NonEmailAttributeName");

        ARARestriction[] araRestrictionArr = {attEmailAddress1, attEmailAddress2, araRestrictionNonEmail};

        pipelineView.setAraRestrictions(new ARARestriction[0]);
        cert.setCertificateAttributes(new HashSet<>());
        List<String> resultList = NotificationService.findARAEmailRecipients(pipelineView, cert);
        assertEquals(0, resultList.size());


        CertificateAttribute certAttrEmail1 = new CertificateAttribute();
        certAttrEmail1.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress1");
        certAttrEmail1.setValue("email1@foo.de");

        CertificateAttribute certAttrEmail1_0 = new CertificateAttribute();
        certAttrEmail1_0.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress1");
        certAttrEmail1_0.setValue("email1@foo.de");

        CertificateAttribute certAttrEmail1_1 = new CertificateAttribute();
        certAttrEmail1_1.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress1");
        certAttrEmail1_1.setValue("email1_1@foo.de");

        CertificateAttribute certAttrEmail2 = new CertificateAttribute();
        certAttrEmail2.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress2");
        certAttrEmail2.setValue("email2@foo.de");

        CertificateAttribute certAttrEmail9 = new CertificateAttribute();
        certAttrEmail9.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress9");
        certAttrEmail9.setValue("email9@foo.de");

        pipelineView.setAraRestrictions(araRestrictionArr);
        cert.setCertificateAttributes(Set.of(certAttrEmail1, certAttrEmail2));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, cert);
        assertEquals(2, resultList.size());

        // ensure no effect by attributes nor marked as email
        cert.setCertificateAttributes(Set.of(certAttrEmail1, certAttrEmail2, certAttrEmail9));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, cert);
        assertEquals(2, resultList.size());

        // ensure no duplicates
        cert.setCertificateAttributes(Set.of(certAttrEmail1, certAttrEmail1_0, certAttrEmail2));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, cert);
        assertEquals(2, resultList.size());

        // handle multiple ARA with same name
        cert.setCertificateAttributes(Set.of(certAttrEmail1, certAttrEmail1_1, certAttrEmail2));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, cert);
        assertEquals(3, resultList.size());

    }

    @Test
    void findARAEmailRecipientsFromCSR() {
        PipelineView pipelineView = new PipelineView();
        CSR csr = new CSR();

        pipelineView.setAraRestrictions(new ARARestriction[0]);
        WebConfigItems webConfigItems = new WebConfigItems();
        webConfigItems.setAdditionalEMailRecipients("");
        pipelineView.setWebConfigItems(webConfigItems);

        csr.setCsrAttributes(new HashSet<>());
        List<String> resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(0, resultList.size());


        ARARestriction attEmailAddress1 = buildARARestrictionAsEmailAddress("attEmailAddress1");
        ARARestriction attEmailAddress2 = buildARARestrictionAsEmailAddress("attEmailAddress2");
        ARARestriction araRestrictionNonEmail = new ARARestriction();
        araRestrictionNonEmail.setName("NonEmailAttributeName");

        ARARestriction[] araRestrictionArr = {attEmailAddress1, attEmailAddress2, araRestrictionNonEmail};

        CsrAttribute csrAttributeEmail1 = new CsrAttribute();
        csrAttributeEmail1.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress1");
        csrAttributeEmail1.setValue("email1@foo.de");

        CsrAttribute csrAttributeEmail1_0 = new CsrAttribute();
        csrAttributeEmail1_0.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress1");
        csrAttributeEmail1_0.setValue("email1@foo.de");

        CsrAttribute csrAttributeEmail1_1 = new CsrAttribute();
        csrAttributeEmail1_1.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress1");
        csrAttributeEmail1_1.setValue("email1_1@foo.de");

        CsrAttribute csrAttributeEmail2 = new CsrAttribute();
        csrAttributeEmail2.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress2");
        csrAttributeEmail2.setValue("email2@foo.de");

        CsrAttribute csrAttributeEmail9 = new CsrAttribute();
        csrAttributeEmail9.setName(CsrAttribute.ARA_PREFIX + "attEmailAddress9");
        csrAttributeEmail9.setValue("email9@foo.de");

        pipelineView.setAraRestrictions(araRestrictionArr);
        csr.setCsrAttributes(Set.of(csrAttributeEmail1, csrAttributeEmail2));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(2, resultList.size());

        // ensure no effect by attributes nor marked as email
        csr.setCsrAttributes(Set.of(csrAttributeEmail1, csrAttributeEmail2, csrAttributeEmail9));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(2, resultList.size());

        // ensure no duplicates
        csr.setCsrAttributes(Set.of(csrAttributeEmail1, csrAttributeEmail1_0, csrAttributeEmail2));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(2, resultList.size());

        // handle multiple ARA with same name
        csr.setCsrAttributes(Set.of(csrAttributeEmail1, csrAttributeEmail1_1, csrAttributeEmail2));
        resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(3, resultList.size());


        // ignore duplicate
        webConfigItems.setAdditionalEMailRecipients("email1@foo.de");
        resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(3, resultList.size());

        // split sting and insert additional entry
        webConfigItems.setAdditionalEMailRecipients("email1@foo.de; email99@foo.de");
        resultList = NotificationService.findARAEmailRecipients(pipelineView, csr);
        assertEquals(4, resultList.size());


    }

    public ARARestriction buildARARestrictionAsEmailAddress(String attributeName){
        ARARestriction araRestriction = new ARARestriction();
        araRestriction.setName(attributeName);
        araRestriction.setContentType(ARAContentType.EMAIL_ADDRESS);

        return araRestriction;
    }
}
