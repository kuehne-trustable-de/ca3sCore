package de.trustable.ca3s.core;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.bc.BcX509v3CertificateBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

public class TestBcPssCerts {

  public static void main(final String[] args) throws NoSuchAlgorithmException, IOException, OperatorCreationException, CertificateException,
      InvalidKeyException, NoSuchProviderException, SignatureException, CertException {

    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    final KeyPair kp = kpg.genKeyPair();
    final AsymmetricKeyParameter publicKeyAsymKeyParam = PublicKeyFactory.createKey(kp.getPublic().getEncoded());
    final AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(kp.getPrivate().getEncoded());

    // final AsymmetricCipherKeyPair pair = generateLongFixedKeys();
    // final AsymmetricKeyParameter privateKeyAsymKeyParam = pair.getPrivate();
    // final AsymmetricKeyParameter publicKeyAsymKeyParam = pair.getPublic();

    // final String algorithm = "SHA256WITHRSA";
    // final String algorithm = "MD2WITHRSA";
    final String algorithm = "SHA384WITHRSAANDMGF1";

    Security.addProvider(new BouncyCastleProvider());
    // ######################## BC CODE ################################
    final DefaultSignatureAlgorithmIdentifierFinder sigAlgFinder = new DefaultSignatureAlgorithmIdentifierFinder();
    final DefaultDigestAlgorithmIdentifierFinder digAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
    final AlgorithmIdentifier sigAlgId = sigAlgFinder.find(algorithm);
    final AlgorithmIdentifier digAlgId = digAlgFinder.find(sigAlgId);

    final ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);


    final X500NameBuilder builder = createStdBuilder();
    final BcX509v3CertificateBuilder certGen = new BcX509v3CertificateBuilder(builder.build(), BigInteger.valueOf(1),
        new Date(System.currentTimeMillis() - 50000), new Date(System.currentTimeMillis() + 50000), builder.build(), publicKeyAsymKeyParam);

    final BcX509ExtensionUtils extFact = new BcX509ExtensionUtils(new SHA1DigestCalculator());
    certGen.addExtension(Extension.authorityKeyIdentifier, true, extFact.createAuthorityKeyIdentifier(publicKeyAsymKeyParam));


    final X509CertificateHolder certificateHolder = certGen.build(sigGen);
    // ######################## BC CODE ################################

    Files.write(Paths.get("/tmp/test-sha-256.cert"), certificateHolder.getEncoded(), StandardOpenOption.CREATE);

    final boolean bcSignatureTest =
        certificateHolder.isSignatureValid(new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(publicKeyAsymKeyParam));
    System.out.println("BC-verification:  " + bcSignatureTest);

    final ContentVerifierProvider verifier = new JcaContentVerifierProviderBuilder().setProvider("BC").build(certificateHolder);
    final boolean signtureTest = certificateHolder.isSignatureValid(verifier);
    System.out.println("JCA-verification: " + signtureTest);

    final X509Certificate cert = bcToJava(certificateHolder);
    final Signature verifySig = Signature.getInstance(algorithm);
    verifySig.initVerify(kp.getPublic());
    verifySig.update(cert.getTBSCertificate());
    System.out.println("RAW-verification: " + verifySig.verify(cert.getSignature()));

  }

  public static X509Certificate bcToJava(final X509CertificateHolder bcCert) throws CertificateException {
    CertificateFactory fact;
    try {
      fact = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);

      final X509Certificate generatedCertificate = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(bcCert.getEncoded()));

      return generatedCertificate;
    } catch (CertificateException | NoSuchProviderException | IOException e) {
      throw new CertificateException(e.fillInStackTrace());
    }
  }

  private static X500NameBuilder createStdBuilder() {
    final X500NameBuilder builder = new X500NameBuilder(RFC4519Style.INSTANCE);

    builder.addRDN(RFC4519Style.c, "AU");
    builder.addRDN(RFC4519Style.o, "The Legion of the Bouncy Castle");
    builder.addRDN(RFC4519Style.l, "Melbourne");
    builder.addRDN(RFC4519Style.st, "Victoria");
    builder.addRDN(PKCSObjectIdentifiers.pkcs_9_at_emailAddress, "feedback-crypto@bouncycastle.org");

    return builder;
  }

  private static AsymmetricCipherKeyPair generateLongFixedKeys() {
    final RSAKeyParameters pubKeySpec = new RSAKeyParameters(false, new BigInteger(
        "a56e4a0e701017589a5187dc7ea841d156f2ec0e36ad52a44dfeb1e61f7ad991d8c51056ffedb162b4c0f283a12a88a394dff526ab7291cbb307ceabfce0b1dfd5cd9508096d5b2b8b6df5d671ef6377c0921cb23c270a70e2598e6ff89d19f105acc2d3f0cb35f29280e1386b6f64c4ef22e1e1f20d0ce8cffb2249bd9a2137",
        16), new BigInteger("010001", 16));

    final RSAKeyParameters privKeySpec = new RSAPrivateCrtKeyParameters(new BigInteger(
        "a56e4a0e701017589a5187dc7ea841d156f2ec0e36ad52a44dfeb1e61f7ad991d8c51056ffedb162b4c0f283a12a88a394dff526ab7291cbb307ceabfce0b1dfd5cd9508096d5b2b8b6df5d671ef6377c0921cb23c270a70e2598e6ff89d19f105acc2d3f0cb35f29280e1386b6f64c4ef22e1e1f20d0ce8cffb2249bd9a2137",
        16), new BigInteger("010001", 16),
        new BigInteger(
            "33a5042a90b27d4f5451ca9bbbd0b44771a101af884340aef9885f2a4bbe92e894a724ac3c568c8f97853ad07c0266c8c6a3ca0929f1e8f11231884429fc4d9ae55fee896a10ce707c3ed7e734e44727a39574501a532683109c2abacaba283c31b4bd2f53c3ee37e352cee34f9e503bd80c0622ad79c6dcee883547c6a3b325",
            16),
        new BigInteger("e7e8942720a877517273a356053ea2a1bc0c94aa72d55c6e86296b2dfc967948c0a72cbccca7eacb35706e09a1df55a1535bd9b3cc34160b3b6dcd3eda8e6443", 16),
        new BigInteger("b69dca1cf7d4d7ec81e75b90fcca874abcde123fd2700180aa90479b6e48de8d67ed24f9f19d85ba275874f542cd20dc723e6963364a1f9425452b269a6799fd", 16),
        new BigInteger("28fa13938655be1f8a159cbaca5a72ea190c30089e19cd274a556f36c4f6e19f554b34c077790427bbdd8dd3ede2448328f385d81b30e8e43b2fffa027861979", 16),
        new BigInteger("1a8b38f398fa712049898d7fb79ee0a77668791299cdfa09efc0e507acb21ed74301ef5bfd48be455eaeb6e1678255827580a8e4e8e14151d1510a82a3f2e729", 16),
        new BigInteger("27156aba4126d24a81f3a528cbfb27f56886f840a9f6e86e17a44b94fe9319584b8e22fdde1e5a2e3bd8aa5ba8d8584194eb2190acf832b847f13a3d24a79f4d", 16));

    return new AsymmetricCipherKeyPair(pubKeySpec, privKeySpec);
  }

}

class SHA1DigestCalculator implements DigestCalculator {
  private final ByteArrayOutputStream bOut = new ByteArrayOutputStream();

  @Override
  public AlgorithmIdentifier getAlgorithmIdentifier() {
    return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
  }

  @Override
  public OutputStream getOutputStream() {
    return this.bOut;
  }

  @Override
  public byte[] getDigest() {
    final byte[] bytes = this.bOut.toByteArray();

    this.bOut.reset();

    final Digest sha1 = new SHA1Digest();

    sha1.update(bytes, 0, bytes.length);

    final byte[] digest = new byte[sha1.getDigestSize()];

    sha1.doFinal(digest, 0);

    return digest;
  }
}

