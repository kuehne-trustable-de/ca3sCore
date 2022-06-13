# Help

## **C**ertificate **A**utomation, **A**uthorization and **A**dministration **S**ervice (_ca3s_)

ca3s is a CA support system with a flexible RA part using BPM aiming to automate as much as possible. Therefore providing ACME and SCEP interfaces in addition to the usual web form. Aggregating certificate sets from different sources and using CMP-connected CAs or ADCS instances for certificate creation.

- Manage all your CA instances (CMP and ADCS)
- Keep track of expiration of all your relevant certificates from all sources
- Analyze the key algorithms, key length, hash and padding algorithms in use
- Offer a convenient web interface for the requestors and the RA officers

But most important for a reliable PKI infrastructure

- Automate issuance and renewal as far as possible
- Use BPMN to define organization specific rules
- Offer well established interfaces (ACME and SCEP) for easy automation

The project is open sourced under [EUPL](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12) and can be found at [github](https://github.com/kuehne-trustable-de/ca3sCore).

### Building Bocks

The following picture gives a coarse overview of the ca3s architecture. On top there are the different types of clients that connect to the ca3s server. On the bottom the external services ca3s connects to are shown.

![ca3s architecture overview](../../images/birds_view.jpg)

ca3s architecture overview

#### Request Adapter (top section)

Certificate requests can be delivered according to the ACME and SCEP protocol and via a web user interface. Additional protocols can be added easily.

#### CA Adapter (bottom section)

Different CA instances maybe connected to ca3s. A quite common protocol for CAs is CMP, supported e.g. by the ejbca and many other. In addition there is a separate project ([adcsCert](https://github.com/kuehne-trustable-de/adcsCert)) to connect to a Microsoft ADCS CA.

#### Certificate database

A central certificate store holds issued and certificates retrieved otherwise to have a one-stop-shop for certificate status and reporting, e.g. expiry reports.

#### Issuance workflow

Depending on the configuration certificate requests maybe admitted by a human registration officer. Complementary automated workflows can be configured to unload registration officer from tedious tasks and to perform even complex processes. The processes can be defined in BPMN and assigned to specific processing pipelines.

## Forms explanations

### <a id="pkcsxx.upload.pipeline"></a> Processing pipeline

Certificates maybe issued for a broad variety of use cases. These use cases may require different issuing CAs, different trust levels, different usages, validities and issuance requirements. Different processing pipelines are offered in the following selection box. Additional information maybe displayed beneath the selection. Please be sure you select the right item for needs. A wrong pipeline will cause a rejection of your request.

### <a id="pkcsxx.upload.creationMode.selection"></a> Creation mode

- CSR available  
  The straight forward mode of the certificate request process is to upload a CSR. Select this option if you got the CSR already available and paste it into the text area below. Alternatively you may search for a CSR file on your computer.\* Serverside key creation
  If your security policy allows this option you may select to let the server create a key pair for you. You can selct a key type and length and define the details of the certificate subject.

The server will create a key and certificate container which protects your private key. To get access to the private key you must define a secret for the container.

Keep this secret in a safe place! If the secret is lost the secret key will become unavailable. There is no option to retrieve the secret from the ca3s server.

A compromise of the secret affects the private key and the certificate! Don't use a compromised key. Revoke the certificate immediately!

**It is strongly recommended to use CSRs or an automated certificate management protocol, e.g. ACME!\*** CSR generation command lines
The command line option helps you use the different available tools for CSR creation depending on the operating system and target application.

### <a id="pkcsxx.upload.creationTool.selection"></a> Tool selection

- Java keytool  
  Java's key and certificate management tool included within the Java runtime distribution both on Windows and Linux._ OpenSSL > 1.1.0
  A fairly recent version of the widely used OpenSSL tool. If your unsure check your version using 'openssl version'._ OpenSSL
  A command line script version compatible with all OpenSSL tool.

### <a id="ca3SApp.help.subtitle.commandline"></a> CSR command line creation

From the security point of view its best practise to create the key pair on the target system (e.g. a web server) and create a certificate signing request (csr). A csr contains public information (e.g. the domain name of the web server) and the public key. The private key does not leave the target system when requesting the certificate from the CA! to create a csr please open a command shell in your system:

- Windows: Command Prompt ('cmd.exe') or PowerShell
- Linux: Shell

Change to the directory where the key should be created. Consult the product documentation of your application for the appropriate dirctory. Fill out the required fields in the ca3s web form (subject, organization, SAN, ...).

Check with your documentation which type of 'store' is expected. Java applications usually expect a PKCS12 keystore, Apache web servers may process separate PEM files.

![Certificate parameter](../../images/sslnew4.png)

Certificate parameter

The 'tool selection' chooses the different tools:

- Java keytool  
  Java's keytool requires two commands to create the key and the CSR.
- OpenSSL > 1.1.0  
  This recent version creates all required files in one command.
- OpenSSL  
  The old versions of OpenSSL requires a configuration file and a shell command.
- Windows 'certreq'
  For the use of the Windows build-in 'certreq' tool it is required to provide a configuration file on the windows command line interface.

#### Linux

![Linux command shell](../../images/java4shell.png)

Linux command shell

#### Windows

![Windows command prompt](../../images/sslold3.png)

Windows command prompt

#### Copy

Copy the generated value from the text box to your input shell. The text boxes got a 'Copy'-button but of course it's possible to select and copy the texts manually.

![Copy-Button](../../images/java_3_5.png)

Copy-Button

#### <a id="pkcsxx.upload.creationTool.cn.as.san"></a> Ensure Common Name as SAN

For certain use cases (e.g. TLS Server) it is useful, recommended or even required to repeat the Common Name as a SAN entry. If the current pipeline allows the use of SANs, this option forces the Common Name to be inserted as a SAN. If a corresponding SAN entry is already present, this option has no effect.

#### <a id="pkcsxx.upload.machine.key.set"></a> Use Windows 'Machine Key Set' (using certreq)

This option allows to select the Windows keystore. Depending on your application it may be required to store the key and the certificate in a specific store (e.g. Machine store for IIS). Please consult yor application documentation regarding the appropriate store.
If this option is activated the certreq command will use the 'Machine Key Set' and therefore wil require the certreq command to be submitted by an 'Administrator'.

#### File name und FriendlyName (using certreq)

The generated file names consist of the Common Name, the current date and an extension representing the specific file type. This approach should avoid unintentional deletion or overwriting of relevant files by subsequent requests. Changing the file names according to specific needs should cause any problems.

#### 'certreq -accept' Importing of the created certificate

Using the option '-accept' and the filename of the created certificate advises the certreq tool to import the certificate into the store alongside the created key. After this import the key and certificate can be used by the application.

#### <a id="pkcsxx.upload.creationTool.cmdline"></a> Command line

The generated Java keytool command line consists of two parts:

The first command generates a new keypair with the selected key length in a PKCS12 keystore ('test.p12') with the alias 'keyAlias'. Consult your application documentation regarding the required keystore file name and the expected alias. Make sure existing files may not be overridden.

The second command creates a certificate signing request (CSR). This file (named 'server.csr' in the generated command) will be uploaded to the CA.

#### <a id="pkcsxx.upload.creationTool.req.conf"></a> OpenSSL request configuration file

Open a text editor on your system. Copy the generated value from the text box into your editor. Save the file in the directory where the key should be created.

#### <a id="ca3SApp.certificate.download.PKCS12"></a> PKCS12 Container

The PKCS12 container format contains both certificates and the private keys. To identify the end entity certificate in the container please provide an alias. The correct value may depend on the settings of your application. Please consult the manual regarding alias values. Using the link on the right you may download the PKCS12 container. The filename maybe adopted to application requirements. To access the private key it is necessary to know the 'secret' provided when requesting the key & certificate pair. There is no way to recover the secret value. If it is lost, please revoke this certificate and request an additional certificate.

#### <a id="ca3SApp.certificate.download.PKIX"></a> Download certificate as Binary

Download the created certificate in the binary form (aka PKIX / DER form). Consult the documentation of your application whether this format is understood.

#### <a id="ca3SApp.certificate.download.PEM"></a> Download certificate as PEM

Download the created certificate in the textual form (aka PEM form). Consult the documentation of your application whether this format is understood.

#### <a id="ca3SApp.certificate.download.revocationReason"></a> Revocation Reason

In case a certificate needs to be revoked please select an appropriate reason for revocation.

#### <a id="ca3SApp.certificate.comment"></a> Certificate Comment

Provide additional information for the reasoning of the certificate revocation. This maybe useful for the RA officers and for later analysis.
