package de.trustable.ca3s.core.service.util;

import org.bouncycastle.asn1.x509.GeneralName;
import org.junit.jupiter.api.Test;

import javax.naming.InvalidNameException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CertificateUtilTest {


    @Test
	public void testNameNormalization() throws InvalidNameException {

        String a = CertificateUtil.getNormalizedName("C=DE,O=T-Systems International GmbH,OU=T-Systems Trust Center,CN=TeleSec Business CA 1");
        String b = CertificateUtil.getNormalizedName("CN=TeleSec Business CA 1,OU=T-Systems Trust Center,O=T-Systems International GmbH,C=DE");

//        System.out.println("Normalized name: " + a);
//        System.out.println("Normalized name: " + b);

        assertEquals("normalizing names expected to be identical ", a, b);
	}


    @Test
    public void testNameHandling() {

        GeneralName[] generalNames = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", null);
        assertEquals(" expected to see 3 GeneralNames ", 3, generalNames.length);

        GeneralName[] generalNames1 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", "  ");
        assertEquals(" expected to see 3 GeneralNames ", 3, generalNames1.length);

        GeneralName[] generalNames2 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", "foo.de");
        assertEquals(" expected to see 3 GeneralNames ", 3, generalNames2.length);

        GeneralName[] generalNames3 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de", "foo.eu");
        assertEquals(" expected to see 4 GeneralNames ", 4, generalNames3.length);

        GeneralName[] generalNames4 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de, 127.0.0.1", "foo.de");
        assertEquals(" expected to see 4 GeneralNames ", 4, generalNames4.length);
//        for( GeneralName gn:generalNames4){ System.out.println("4: " + gn);}
        assertEquals(" expected to see 1 GeneralName of type DNS", 3,
            Arrays.stream(generalNames4).filter(n -> n.getTagNo() == GeneralName.dNSName).count());
        assertEquals(" expected to see 1 GeneralName of type IP", 1,
            Arrays.stream(generalNames4).filter(n -> n.getTagNo() == GeneralName.iPAddress).count());

        GeneralName[] generalNames5 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de, foo.de ", " 127.0.0.1 ");
        assertEquals(" expected to see 4 GeneralNames ", 4, generalNames5.length);
//        for( GeneralName gn:generalNames5){ System.out.println("5: " + gn);}

        assertEquals(" expected to see 1 GeneralName of type DNS", 3,
            Arrays.stream(generalNames5).filter(n -> n.getTagNo() == GeneralName.dNSName).count());
        assertEquals(" expected to see 1 GeneralName of type IP", 1,
            Arrays.stream(generalNames5).filter(n -> n.getTagNo() == GeneralName.iPAddress).count());

        GeneralName[] generalNames6 = CertificateUtil.splitSANString(" foo.de, bar.de , baz.de, foo.de ", " 2001:0db8:85a3:08d3:1319:8a2e:0370:7344 ");
        assertEquals(" expected to see 4 GeneralNames ", 4, generalNames6.length);
//        for( GeneralName gn:generalNames6){ System.out.println("5: " + gn);}
        assertEquals(" expected to see 1 GeneralName of type DNS", 3,
            Arrays.stream(generalNames6).filter(n -> n.getTagNo() == GeneralName.dNSName).count());
        assertEquals(" expected to see 1 GeneralName of type IP", 1,
            Arrays.stream(generalNames6).filter(n -> n.getTagNo() == GeneralName.iPAddress).count());

    }
}
