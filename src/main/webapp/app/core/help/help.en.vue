<template>
    <div>
        <h1 class="jh-entity-heading">
          <span v-text="$t('ca3SApp.help.title')">Help</span>
        </h1>

        <h2><b>C</b>ertificate <b>A</b>utomation, <b>A</b>uthorization and <b>A</b>dministration <b>S</b>ervice (<i>ca3s</i>)</h2>
        <div>
            ca3s is a CA support system with a flexible RA part using BPM aiming to automate as much as possible. Therefore providing ACME and SCEP interfaces in addition to the usual web form. Aggregating certificate sets from different sources and using CMP-connected CAs or ADCS instances for certificate creation.
            <ul>
                <li>
                    Manage all your CA instances (CMP and ADCS)
                </li>
                <li>
                    Keep track of expiration of all your relevant certificates from all sources
                </li>
                <li>
                    Analyze the key algorithms, key length, hash and padding algorithms in use
                </li>
                <li>
                    Offer a convenient web interface for the requestors and the RA officers
                </li>
            </ul>

            But most important for a reliable PKI infrastructure

            <ul>
                <li>
                    Automate issuance and renewal as far as possible
                </li>
                <li>
                    Use BPMN to define organization specific rules
                </li>
                <li>
                    Offer well established interfaces (ACME and SCEP) for easy automation
                </li>
            </ul>

            The project is open sourced under <a href="https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12">EUPL</a> and can be found at <a href="https://github.com/kuehne-trustable-de/ca3sCore">github</a>.
        </div>
        <p/>

        <h3>Building Bocks</h3>
        <div>
            The following picture gives a coarse overview of the ca3s architecture. On top there are the different types of clients that connect to the ca3s server. On the bottom the external services ca3s connects to are shown.
            <p/>
            <figure>
                <img src="../../../content/images/birds_view.jpg" alt="ca3s architecture overview">
                <figcaption>ca3s architecture overview</figcaption>
            </figure>
            <!--img class="box" src="../../../content/images/birds_view.jpg" /-->
        </div>
        <p/>

        <h4>Request Adapter (top section)</h4>
        <div>
          Certificate requests can be delivered according to the ACME and SCEP protocol and via a web user interface. Additional protocols can be added easily.
        </div>
        <p/>
        <h4>CA Adapter (bottom section)</h4>
        <div>
        Different CA instances maybe connected to ca3s. A quite common protocol for CAs is CMP, supported e.g. by the ejbca and many other. In addition there is a separate project (<a href="https://github.com/kuehne-trustable-de/adcsCert">adcsCert</a>) to connect to a Microsoft ADCS CA.
        </div>
        <p/>
        <h4>Certificate database</h4>
        <div>
        A central certificate store holds issued and certificates retrieved otherwise to have a one-stop-shop for certificate status and reporting, e.g. expiry reports.
        </div>
        <p/>
        <h4>Issuance workflow</h4>
        <div>
        Depending on the configuration certificate requests maybe admitted by a human registration officer. Complementary automated workflows can be configured to unload registration officer from tedious tasks and to perform even complex processes. The processes can be defined in BPMN and assigned to specific processing pipelines.
        </div>
        <p/>


        <h2>Forms explainations</h2>

        <h3 class="jh-entity-heading">
            <span id="pkcsxx.upload.pipeline" v-text="$t('pkcsxx.upload.pipeline')">Processing pipeline</span>
        </h3>

        <div>
            Certificates maybe issued for a broad variety of use cases. These use cases may require different issuing CAs, different trust levels, different usages, validities and issuance requirements.
            Different processing pipelines are offered in the following selection box. Additional information maybe displayed beneath the selection. Please be sure you select the right item for needs. A wrong
            pipeline will cause a rejection of your request.
        </div>
        <p/>

        <h3 class="jh-entity-heading">
            <span id="pkcsxx.upload.creationMode.selection" v-text="$t('pkcsxx.upload.creationMode.selection')">Creation mode</span>
        </h3>

        <div>
            <ul>
                <li v-text="$t('pkcsxx.upload.creationMode.csrAvailable')">CSR available</li>
                The straight forward mode of the certificate request process is to upload a CSR. Select this option if you got the CSR already available and paste it into the text area below. Alternatively you may search for a CSR file on your computer.
                <li v-text="$t('pkcsxx.upload.creationMode.serversideKeyCreation')">Serverside key creation</li>
                If your security policy allows this option you may select to let the server create a key pair for you. You can selct a key type and length and define the details of the certificate subject.<br><br>
                The server will create a key and certificate container which protects your private key. To get access to the private key you must define a secret for the container. <br><br>
                Keep this secret in a safe place! If the secret is lost the secret key will become unavailable. There is no option to retrieve the secret from the ca3s server. <br><br>
                A compromise of the secret affects the private key and the certificate! Don't use a compromised key. Revoke the certificate immediately!<br><br>
                <b>It is strongly recommended to use CSRs or an automated certificate management protocol, e.g. ACME!</b>
                <li v-text="$t('pkcsxx.upload.creationMode.commandLineTool')">command line tool</li>
                The command line option helps you use the different available tools for CSR creation depending on the operating system and target application.
            </ul>
        </div>
        <p/>

        <h3 class="jh-entity-heading">
            <span id="pkcsxx.upload.creationTool.selection" v-text="$t('pkcsxx.upload.creationTool.selection')">Processing pipeline</span>
        </h3>

        <div>
            <ul>
                <li>Java keytool</li>
                Java's key and certificate management tool included within the Java runtime distribution both on Windows and Linux.
                <li>OpenSSL > 1.1.0</li>
                A fairly recent version of the widely used OpenSSL tool. If your unsure check your version using 'openssl version'.
                <li>OpenSSL</li>
                A command line script version compatible with all OpenSSL tool.
            </ul>
        </div>
        <p/>



        <h3 class="jh-entity-heading">
          <span id="ca3SApp.help.subtitle.commandline">CSR command line creation</span>
        </h3>
        <div>
            From the security point of view its best practise to create the key pair on the target system (e.g. a web server) and create a certificate signing request (csr). A csr contains public information (e.g. the domain name of the web server) and the public key.
            The private key does not leave the target system when requesting the certificate from the CA!

            to create a csr please open a command shell in your system:
            <ul>
                <li>Windows: Command Prompt ('cmd.exe') or PowerShell</li>
                <li>Linux: Shell</li>
            </ul>
            Change to the directory where the key should be created. Consult the product documentation of your application for the appropriate dirctory.
            Fill out the required fields in the ca3s web form (subject, organization, SAN, ...).<br><br>
            Check with your documentation which type of 'store' is expected. Java applications usually expect a PKCS12 keystore, Apache web servers may process separate PEM files.<br><br>
            <figure>
                <img src="../../../content/images/java2 parameter.png" alt="Certificate parameter">
                <figcaption>Certificate parameter</figcaption>
            </figure>
            <p/>

            The 'tool selection' chooses the different tools:
            <ul>
                <li>Java keytool</li>
                Java's keytool requires two commands to create the key and the CSR.
                <li>OpenSSL > 1.1.0</li>
                This recent version creates all required files in one command.
                <li>OpenSSL</li>
                The old versions of OpenSSL requires a configuration file and a shell command.
            </ul>

            <p/>
            <h4>Linux</h4>
            <figure>
                <img src="../../../content/images/sslnew4.png" alt="Linux command shell">
                <figcaption>Linux command shell</figcaption>
            </figure>

            <p/>
            <h4>Windows</h4>
            <figure>
                <img src="../../../content/images/java4shell.png" alt="Windows command prompt">
                <figcaption>Windows command prompt</figcaption>
            </figure>

            <p/>
            <h4>Copy</h4>
            Copy the generated value from the text box to your input shell. The text boxes got a 'Copy'-button but of course it's possible to select and copy the texts manually.
            <figure>
                <img src="../../../content/images/sslold3.png" alt="Copy-Button">
                <figcaption>Copy-Button</figcaption>
            </figure>

        </div>
        <p/>

        <h4 class="jh-entity-heading">
          <span v-text="$t('pkcsxx.upload.creationTool.cmdline')" id="pkcsxx.upload.creationTool.cmdline">CSR command line 'Java keytool'</span>
        </h4>
        <div>
          The generated Java keytool command line consists of two parts: <br><br>
          The first command generates a new keypair with the selected key length in a PKCS12 keystore ('test.p12') with the alias 'keyAlias'.
          Consult your application documentation regarding the required keystore file name and the expected alias. Make sure existing files
          may not be overridden.<br><br>
          The second command creates a certificate signing request (CSR). This file (named 'server.csr' in the generated command) will be
          uploaded to the CA.<br><br>
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.help.subtitle.request.conf')" id="pkcsxx.upload.creationTool.req.conf">OpenSSL request config</span>
        </h4>
        <div>Open a text editor on your system.
            Copy the generated value from the text box into your editor.
            Save the file in the directory where the key should be created.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.download.PKCS12')" id="ca3SApp.certificate.download.PKCS12">Download PKCS12 Container</span>
        </h4>
        <div>The PKCS12 container format contains both certificates and the private keys. To identify the end entity certificate
            in the container please provide an alias. The correct value may depend on the settings of your application. Please consult
            the manual regarding alias values.
            Using the link on the right you may download the PKCS12 container. The filename maybe adopted to application requirements.
            To access the private key it is necessary to know the 'secret' provided when requesting the key & certificate pair. There is no way to
            recover the secret value. If it is lost, please revoke this certificate and request an additional certificate.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.download.PKIX')" id="ca3SApp.certificate.download.PKIX">Download of the certificate in the binary form (PKIX)</span>
        </h4>
        <div>Download the created certificate in the binary form (aka PKIX / DER form).
            Consult the documentation of your application whether this format is understood.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.download.PEM')" id="ca3SApp.certificate.download.PEM">Download of the certificate in the textual form (PEM)</span>
        </h4>
        <div>Download the created certificate in the textual form (aka PEM form).
            Consult the documentation of your application whether this format is understood.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.revocationReason')" id="ca3SApp.certificate.download.revocationReason">Select a revocation reason</span>
        </h4>
        <div>In case a certificate needs to be revoked please select an appropriate reason for revocation.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.comment')" id="ca3SApp.certificate.comment">Provide a commment regarding the revocation (optional)</span>
        </h4>
        <div>Provide additional information for the reasoning of the certificate revocation. This maybe useful for the RA officers and for later analysis.
        </div>
        <p/>


    </div>
</template>

<script lang="ts" src="./help.component.ts">
</script>

<style src="./help.css">
</style>
