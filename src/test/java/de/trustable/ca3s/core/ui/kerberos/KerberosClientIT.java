package de.trustable.ca3s.core.ui.kerberos;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.*;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.util.JCAManager;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.kerby.kerberos.kdc.impl.NettyKdcServerImpl;
import org.apache.kerby.kerberos.kerb.KrbException;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.security.kerberos.client.KerberosRestTemplate.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class KerberosClientIT {

    public final static String BASE_DN = "dc=example,dc=com";
    public final static String BASE_PEOPLE_DN = "ou=people,dc=example,dc=com";
    public final static String PASSWORD = "s3cr3t";
    public final static String NAME_ALICE = "alice";
    public final static String NAME_BOB = "bob";
    public final static String CN = "cn";
    public final static String SN = "sn";
    public final static String ROLE = "role";
    public final static String ROLE1 = "role1";
    public final static String ROLE2 = "role2";

    static final String REALM = "test.service.ca3s";
    private static SimpleKdcServer kdcServer;

    static String serverPrincipal = "HTTP/localhost";
    static String alicePrincipal = "alice/localhost";
    static String bobPrincipal = "bob/localhost";

    static String serverKeyTabLocation;
    static String aliceKeyTabLocation;
    static String bobKeyTabLocation;

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    String serverBaseURL;
    String userUrl;

    static int ldapPort; // random port for LDAP
    static String ldapBaseURL;
    static String ldapBindPrincipal;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        JCAManager.getInstance();
        setUpKerberos();
        setupLDAP();
    }

    static void setUpKerberos() throws Exception {

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Files.createTempDir().getAbsolutePath();
        }
        File workDir = new File(basedir + "/target");
        workDir.mkdirs();
        System.out.println("------ workDir at " + workDir.getAbsolutePath());

        kdcServer = new SimpleKdcServer();

        kdcServer.setKdcRealm(REALM);
        kdcServer.setAllowUdp(false);
        kdcServer.setAllowTcp(true);
        kdcServer.setWorkDir(workDir);

        kdcServer.setKdcHost("localhost");

        kdcServer.setInnerKdcImpl(new NettyKdcServerImpl(kdcServer.getKdcSetting()));

//        kdcServer.getKdcConfig().setString(KdcConfigKey.TOKEN_ISSUERS, "DoubleItSTSIssuer");
//        kdcServer.getKdcConfig().setString(KdcConfigKey.TOKEN_VERIFY_KEYS, "myclient.cer");
        kdcServer.init();

        // Create principals
        kdcServer.createPrincipal(serverPrincipal, "s3cr3t");
        File serverKeyTab = new File(workDir, "server.keytab");
        kdcServer.exportPrincipal(serverPrincipal, serverKeyTab);
        serverKeyTabLocation = serverKeyTab.getAbsolutePath();

        kdcServer.createPrincipal(alicePrincipal, "alice");
        File aliceKeyTab = new File(workDir, "alice.keytab");
        kdcServer.exportPrincipal(alicePrincipal, aliceKeyTab);
        aliceKeyTabLocation = aliceKeyTab.getAbsolutePath();

        kdcServer.createPrincipal(bobPrincipal, "bob");
        File bobKeyTab = new File(workDir, "bob.keytab");
        kdcServer.exportPrincipal(bobPrincipal, bobKeyTab);
        bobKeyTabLocation = bobKeyTab.getAbsolutePath();

        kdcServer.start();

        System.setProperty("sun.security.krb5.debug", "true");
//        System.setProperty("java.security.auth.login.config", workDir + "/test-classes/kerberos/kerberos.jaas");
        System.setProperty("java.security.krb5.conf", workDir + "/krb5.conf");
        System.out.println("------ krb5.conf: " + workDir + "/krb5.conf");

        System.setProperty("ca3s.auth.kerberos.service-principal", serverPrincipal);
        System.setProperty("ca3s.auth.kerberos.keytab-location", serverKeyTabLocation);
        System.out.println("------ server keytab: " + serverKeyTabLocation);

    }

    static void setupLDAP() throws LDAPException, FileNotFoundException {

        ldapPort = SocketUtils.findAvailableTcpPort(48000);

        final InMemoryDirectoryServerConfig dsConfig = new InMemoryDirectoryServerConfig(BASE_DN);
        dsConfig.setSchema(null);
        dsConfig.setEnforceAttributeSyntaxCompliance(false);
        dsConfig.setEnforceSingleStructuralObjectClass(false);
        dsConfig.setListenerConfigs(new InMemoryListenerConfig("myListener", null, ldapPort, null, null, null));

        ldapBindPrincipal = CN + "=" + NAME_ALICE + "," + BASE_PEOPLE_DN;
        dsConfig.addAdditionalBindCredentials(ldapBindPrincipal, PASSWORD);
        dsConfig.addAdditionalBindCredentials(CN + "=" + NAME_BOB + "," + BASE_PEOPLE_DN, PASSWORD);
        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(dsConfig);
        ds.importFromLDIF(true, ResourceUtils.getFile("classpath:ldif/users-import.ldif"));

        ds.startListening();

        ldapBaseURL = "ldap://localhost:" + ldapPort;

        System.setProperty("ca3s.auth.ldap.url", ldapBaseURL);
        System.setProperty("ca3s.auth.ldap.baseDN", BASE_DN);
        System.setProperty("ca3s.auth.ldap.principal", ldapBindPrincipal);
        System.setProperty("ca3s.auth.ldap.password", PASSWORD);

    }

    @BeforeEach
    void init() {
        serverBaseURL = "http://localhost:" + serverPort;

        userUrl = serverBaseURL + "/kerberos/authenticatedUser";
    }

    @AfterAll
    public static void tearDown() throws KrbException {
        if (kdcServer != null) {
            kdcServer.stop();
        }
    }

    @Test
    public void testEmbeddedLDAPSetup() throws NamingException {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapBaseURL + "/" + BASE_DN);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapBindPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, PASSWORD);

        DirContext ctx = new InitialDirContext(env);

        SearchControls controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE);
        ctx.search( "", "(objectclass=person)", controls);

        controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE);
        NamingEnumeration ne = ctx.search( "", "(objectclass=person)", controls);
        assertTrue( ne.hasMoreElements());


        controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE);
        ne = ctx.search( "ou=Users", "(&(objectclass=groupOfUniqueNames))", controls);

        //(uniqueMember=dn=cn=Santa Claus,ou=Users,dc=example,dc=com)

        for( ; ne.hasMoreElements();){
            Object obj = ne.nextElement();
            System.out.println("isMemberOf for 'Santa Claus' : " + obj);
        }

        controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE);
        ne = ctx.search( "ou=Users", "(&)", new String[]{"isMemberOf"}, controls);

        for( ; ne.hasMoreElements();){
            Object obj = ne.nextElement();
            System.out.println("isMemberOf for 'Micha Kops' : " + obj);
        }

    }

    @Disabled("Enable once kerberos security processing is enabled")
    @Test
    public void testSuccessfulTicketCreation() {

        Credentials credentials = new Credentials() {
            @Override
            public Principal getUserPrincipal() {
                return null;
            }
            @Override
            public String getPassword() {
                return null;
            }
        };

        final Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider> create().register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials( new AuthScope(null, -1, null), credentials);

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
        builder.setDefaultCredentialsProvider(credentialsProvider);

        CloseableHttpClient httpClient = builder.build();

        KerberosRestTemplate restTemplate = new KerberosRestTemplate(aliceKeyTabLocation, alicePrincipal, httpClient );
        ResponseEntity<String> isAuthenticated = restTemplate.getForEntity(userUrl, String.class);
        assertEquals("alice/localhost@test.service.ca3s", isAuthenticated.getBody());
    }


    @Test
    public void testUnauthorizedAccess() {

        RestTemplate restTemplate = new RestTemplate();
        try {
            // No support for Kerberos by default RestTemplate, bound to fail
            restTemplate.getForEntity(userUrl, String.class);
            fail("Unauthorized exception expected");
        } catch( HttpClientErrorException.Unauthorized unauthorized){
            // as expected
        }
    }

    @Test
    public void testUnauthorizedWithBrokenToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Negotiate YIICMwYGKwYBBQUCoIICJzCCAiOgDTALBgkqhkiG9xIBAgKhBAMCAXaiggIKBIICBmCCAgIGCSqGSIb3EgECAgEAboIB8TCCAe2gAwIBBaEDAgEOogcDBQAgAAAAo4IBBGGCAQAwgf2gAwIBBaETGxF0ZXN0LnNlcnZpY2UuY2Ezc6IcMBqgAwIBAKETMBEbBEhUVFAbCWxvY2FsaG9zdKOBwjCBv6ADAgERoQMCAQGigbIEga8RgqHfzQzzIYulkw5moa/fuRX9zHhlTyaAexkTbPId78F0DJQ1sMWFGbAxZuu0SH1CQLbWxehAMz3KNepUJ2MgIY5ccUZkiwbKjjrOyIC54TdYq0nnsF4OaSaziz5jD+mF9klbdXhbNdLOVMInSRF7Xzs4ukNgr2LHm+GetUGjEr/iPy6CNsx4prQjzTskC1xLixuaKgzRL0DysZos1QfJNWuxcAPvKL2iKZv0h/QwpIHPMIHMoAMCARGigcQEgcFykw6EnVSLFYF/71IrizlWMAWjCflDiEUpEwAaSlcwga7SFDqmBNFMiv3L81rYhTZ2xNWx6x63spEBIaNf9syn6loy/gbfEYyz+KO8/Op6jkHpUHI81vUz+KreGDlZo9TQqbvjDEmtrXhgrBQY7rTnWG1uvWNpkZ3ryKlt+q2WO+2ZY1MmSIQ9tqWSV5nx2/ZrBex+nNgwniWty4EoXpwvkWT6YjjVE2rRx9tzdgn9tcwOZ26dYnqbwHpOr+gpzP1h");
        HttpEntity entity = new HttpEntity(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForEntity(userUrl, String.class);
            restTemplate.exchange(userUrl, HttpMethod.GET, entity, String.class);
            fail("Unauthorized exception expected");
        } catch( HttpClientErrorException.Unauthorized unauthorized){
            unauthorized.printStackTrace();
        }
    }

}
