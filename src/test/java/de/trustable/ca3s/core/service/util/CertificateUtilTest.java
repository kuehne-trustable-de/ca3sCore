package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import org.bouncycastle.asn1.x509.GeneralName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.naming.InvalidNameException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class CertificateUtilTest {


    @Test
    public void testNameNormalization() throws InvalidNameException {

        String a = CertificateUtil.getNormalizedName("C=DE,O=T-Systems International GmbH,OU=T-Systems Trust Center,CN=TeleSec Business CA 1");
        String b = CertificateUtil.getNormalizedName("CN=TeleSec Business CA 1,OU=T-Systems Trust Center,O=T-Systems International GmbH,C=DE");

//        System.out.println("Normalized name: " + a);
//        System.out.println("Normalized name: " + b);

        Assertions.assertEquals(a, b, "normalizing names expected to be identical ");
    }


    @Test
    public void testNameHandling() {

        GeneralName[] generalNames = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", null);
        Assertions.assertEquals(3, generalNames.length, " expected to see 3 GeneralNames ");

        GeneralName[] generalNames1 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", "  ");
        Assertions.assertEquals(3, generalNames1.length, " expected to see 3 GeneralNames ");

        GeneralName[] generalNames2 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", "foo.de");
        Assertions.assertEquals(3, generalNames2.length, " expected to see 3 GeneralNames ");

        GeneralName[] generalNames3 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", "foo.eu");
        Assertions.assertEquals(4, generalNames3.length, " expected to see 4 GeneralNames ");

        GeneralName[] generalNames4 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de, 127.0.0.1", "foo.de");
        Assertions.assertEquals(4, generalNames4.length, " expected to see 4 GeneralNames ");
//        for( GeneralName gn:generalNames4){ System.out.println("4: " + gn);}
        Assertions.assertEquals(3, Arrays.stream(generalNames4).filter(n -> n.getTagNo() == GeneralName.dNSName).count(), " expected to see 1 GeneralName of type DNS");
        Assertions.assertEquals(1, Arrays.stream(generalNames4).filter(n -> n.getTagNo() == GeneralName.iPAddress).count(), " expected to see 1 GeneralName of type IP");

        GeneralName[] generalNames5 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de, foo.de ", " 127.0.0.1 ");
        Assertions.assertEquals(4, generalNames5.length, " expected to see 4 GeneralNames ");
//        for( GeneralName gn:generalNames5){ System.out.println("5: " + gn);}

        Assertions.assertEquals(3, Arrays.stream(generalNames5).filter(n -> n.getTagNo() == GeneralName.dNSName).count(), " expected to see 1 GeneralName of type DNS");
        Assertions.assertEquals(1, Arrays.stream(generalNames5).filter(n -> n.getTagNo() == GeneralName.iPAddress).count(), " expected to see 1 GeneralName of type IP");

        GeneralName[] generalNames6 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de, foo.de ", " 2001:0db8:85a3:08d3:1319:8a2e:0370:7344 ");
        Assertions.assertEquals(4, generalNames6.length, " expected to see 4 GeneralNames ");
//        for( GeneralName gn:generalNames6){ System.out.println("5: " + gn);}
        Assertions.assertEquals(3, Arrays.stream(generalNames6).filter(n -> n.getTagNo() == GeneralName.dNSName).count(), " expected to see 1 GeneralName of type DNS");
        Assertions.assertEquals(1, Arrays.stream(generalNames6).filter(n -> n.getTagNo() == GeneralName.iPAddress).count(), " expected to see 1 GeneralName of type IP");

    }

    @Test
    public void testGetCnOrFirstSan() {
        CertificateAttribute cnAttr = new CertificateAttribute();
        cnAttr.setName(CertificateAttribute.ATTRIBUTE_RDN_CN);
        cnAttr.setValue("foo.de");

        CertificateAttribute dnsSanAttr = new CertificateAttribute();
        dnsSanAttr.setName(CsrAttribute.ATTRIBUTE_TYPED_SAN);
        dnsSanAttr.setValue("DNS:foo.com");

        CertificateAttribute dnsSanAttr2 = new CertificateAttribute();
        dnsSanAttr2.setName(CsrAttribute.ATTRIBUTE_TYPED_SAN);
        dnsSanAttr2.setValue("DNS:bar.de");

        CertificateAttribute otherSanAttr = new CertificateAttribute();
        otherSanAttr.setName(CsrAttribute.ATTRIBUTE_TYPED_SAN);
        otherSanAttr.setValue("URI:baz.de");

        String value = CertificateUtil.getCnOrFirstSan( Collections.emptySet());
        Assertions.assertEquals("(noCommonName)", value, " expected to see null ");

        final Set<CertificateAttribute> certificateAttributeListCNOnly = Set.of(cnAttr);
        value = CertificateUtil.getCnOrFirstSan(certificateAttributeListCNOnly);
        Assertions.assertEquals("foo.de", value, " expected to see 'foo.de' ");

        final Set<CertificateAttribute> certificateAttributeListCNAndSANs = Set.of(cnAttr, dnsSanAttr, dnsSanAttr2, otherSanAttr);
        value = CertificateUtil.getCnOrFirstSan(certificateAttributeListCNAndSANs);
        Assertions.assertEquals("foo.de", value, " expected to see 'foo.de' ");

        final Set<CertificateAttribute> certificateAttributeListSANs = Set.of(dnsSanAttr, dnsSanAttr2, otherSanAttr);
        value = CertificateUtil.getCnOrFirstSan(certificateAttributeListSANs);
        Assertions.assertTrue("foo.com".equals(value) || "bar.de".equals(value), " expected to see 'foo.com' ");

        final Set<CertificateAttribute> certificateAttributeListSANs1 = Set.of(dnsSanAttr2, dnsSanAttr, otherSanAttr);
        value = CertificateUtil.getCnOrFirstSan(certificateAttributeListSANs1);
        Assertions.assertTrue("foo.com".equals(value) || "bar.de".equals(value), " expected to see 'foo.com' ");

        final Set<CertificateAttribute> certificateAttributeListSANs2 = Set.of(dnsSanAttr2, otherSanAttr);
        value = CertificateUtil.getCnOrFirstSan(certificateAttributeListSANs2);
        Assertions.assertEquals("bar.de", value, " expected to see 'bar.de' ");

        final Set<CertificateAttribute> certificateAttributeListSANs3 = Set.of(otherSanAttr);
        value = CertificateUtil.getCnOrFirstSan(certificateAttributeListSANs3);
        Assertions.assertEquals("URI:baz.de", value, " expected to see 'URI:baz.de' ");


    }
}
