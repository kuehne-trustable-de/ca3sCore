# Admin Help

## **C**ertificate **A**utomation, **A**uthorization and **A**dministration **S**ervice (_ca3s_)

This certificate management system has a number of basic options within the application.yml file. Once configured and with the application up and running, the majority of options can be administered using the web UI and an administrator account. This document gives an idea of the building blocks and their configuration.

## Help content for the administration forms

### General Preferences

#### <a id="preference.check-crl"></a> Check CRL

Enable the proactive CRL retrieval for all active certificates. Ths ensures the revocation information of the certificate set is up-to-date.

#### <a id="preference.max-next-update-crl"></a> Max time until next check

This numeric input field allows to set a maximum number of hours before an update of a CRL will be performed. This value limits the 'next update' property of the CRL.

#### <a id="preference.server-side-allowed"></a> Enable server side key creation

Enable the option to create a key pair in the ca3s instance and instantly build a certificate request. Ths option eases the process of certificate creation for inexperienced users.
A notable downside of this option is that the private key is available within ca3s. It is stored in the database as an encrypted blob. Also, the private key needs to be transported to the target system (as a PKCS#12 container).
The user must provide a passphrase for the container during the request. No one will be able to retrieve this key later on (neither users nor RA officer nor admins). No other user will be able to download the PKCS#12 container.

Nevertheless: The best approach in terms of security is to NOT store the private key on ANY other system and to NOT transfer it over ANY network! Please consider possible risks when enableing this option.

#### <a id="preference.delete-key-after-days"></a><a id="preference.delete-key-after-usess"></a> server side key limits

To mitigate the risk for keys created on the server on behalf of a user the keys will be dropped after a configurable time (in days) and downloads

#### <a id="preference.http-01-callback-timeout"></a> ACME HTTP01 callback timeout

This numeric input field allows to set a timeout of ACME http requests (in millisecs).

#### <a id="preference.http-01-callback-ports"></a> ACME HTTP01 callback ports

This numeric input fields allow the ports checked for the ACME HTTP01 challenge. The ACME standard defines port 80 as a callback port, only. But in some environments it may be handy to define port beyond port 1000. Please consider possible security risks when allowing additional / unprivileged ports.

#### <a id="preference.hash"></a> Allowed hash algorithms

This listbox allows the selection of hash (digest) algorithms. Please consider your security requirements to select an appropriate set of algorithms. Establish a reminder (e.g. once a year) to re-evaluate the set.
Use Ctrl-Click to select multiple entries.

#### <a id="preference.algo"></a> Allowed encryption algorithms

This listbox allows the selection of encryption algorithms. Please consider your security requirements to select an appropriate set of algorithms. Establish a reminder (e.g. once a year) to re-evaluate the set.
Use Ctrl-Click to select multiple entries.

### Administration of CA connectors

#### <a id="ca-connector.name"></a> Connector Name

Provide a descriptive name of this connector. It is recommended to provide relevant information as to what type of certificate will be processed by this connector.

#### <a id="ca-connector.type"></a> Connector Type

Select a type of connector. Depending on the connector type the set of optional and required settings for the connector will vary. The different types are

- 'CMP' identifies a connection to a CA based on the CMP standard well established in the as a certificate management interface. It is defined in [RFC4210](https://tools.ietf.org/html/rfc4210) .
- 'ADCS' selects a connection to the popular Windows CA. This ADCS connector expects an instance of the ADCSProxy to be installed on the target server.
- 'ADCS certificate inventory' offers a way to import certificates issued by an ADCS instance. This ensures that certificates requested by other means than ca3s can be imported too.
- 'Directory' import certificates from a given directory or website, recursively.
- 'Internal CA' uses a simple CA embedded with ca3s. Intended for testing purposes and MUST NOT be used for production environments.

#### <a id="ca-connector.ca-url"></a> CA Url

Provide a location of the CA or a webserver. For certificate import this field accepts a directory path.

#### <a id="ca-connector.default-ca"></a> Default CA

Check this checkbox with the CA that is intended to provide certificate for ca3s. Only one CA can be selected as default.

#### <a id="ca-connector.active"></a> Active

Activate or deactivate this connector.

#### CMP specific settings

##### <a id="ca-connector.selector"></a> Selector

Provide the identifier of the CMP endpoint. In the ejbca administration UI its called 'CMP Alias'.
If a CMP connector does not use this term you may leave it blank.

##### <a id="ca-connector.cmp.tls-client-id"></a> TLS client certificate

A CA may require a TLS client authentication to establish a connection with its CMP endpoint. Provide the certificate id of the client certificate. Leave this field if TLS client authentication is not required.

##### <a id="ca-connector.cmp.message-protection-by-passphrase"></a> Message protection by passphrase

A CA may require a passphrase or a certificate authentication to authorize requests at its CMP endpoint. By selecting this checkbox the passphrase mode is choosen.

#### <a id="ca-connector.cmp.message-protection-passphrase"></a> Message protection passphrase

If the target CA expects a passphrase to authorize incoming request at its CMP endpoint, this is the field to enter it. It will be stored in encrypted form in the ca3s database.

##### <a id="ca-connector.cmp.message-protection-certificate-id"></a> Message protection certificate id

If the target CA requires a certificate authentication at its CMP endpoint, this is the field to enter its id. Consult the documentation of the CA's CMP endpoint for details on the certificate creation or registration.

##### <a id="ca-connector.cmp.issuer-name"></a> Issuer name

If the target CA requires an issuer name to be included in the CMP message, this is the filed to provide it. Consult the documentation of the CA's CMP endpoint for details regarding appropriate values.

##### <a id="ca-connector.cmp.multiple-messages"></a> Multiple messages

CAs may offer the option the process multiple requests contained within a single CMP message. ca3s does not support multiple requests. Nevertheless the message structure differs and the checkbox must be sett according the CA's CMP expectations.

##### <a id="ca-connector.cmp.implicit-confirm"></a> Implicit confirm

CAs may require the 'implicit confirm' flag to be set to process a CMP request.

##### <a id="ca-connector.cmp.message-content-type"></a> Message content type

CAs may require a specific message content type value provided as HTTP header with the CMP request. Consult the documentation of the CA's CMP endpoint whether this header is required and wht the appropriate values are.

##### <a id="ca-connector.cmp.server-name-indication"></a> Server Name Indication

This entry field offers the option to provide a specific SNI value to support the routing to the correct CA instance. May only be required in complex network setups.

##### <a id="ca-connector.cmp.disable-host-name-verifier"></a> Disable HostNameVerifier

This flag offers the option to disable the host name validation. This option disables a relevant security feature of TLS and allows Man-in-the-middle attacks. May only be required in complex network setups.

#### ADCS specific settings

##### <a id="ca-connector.template"></a> Template

Provide the name of the template the ADCS server should use. Check the list of available certificate templates in your certsrv UI.

##### <a id="ca-connector.passphrase"></a> Passphrase

The ADCS Proxy instance installed on your ADCS server uses this passphrase to authorize incoming requests. It is specific to the ADCS Proxy instance, not the template.

#### Importing connectors

The following connector import certificates created elsewhere into the ca3s database.

##### <a id="ca-connector.interval"></a> Interval

Select a polling interval for certificate retrieval.

#### Directory specific settings

##### <a id="ca-connector.trust-self-signed-certificates"></a> Trust self-signed certificates

All self-signed certificates imported by this connector will be marked as 'trusted'. This is a critical option. Use it for tightly controlled set of trust anchors.

#### ADCS certificate inventory

##### <a id="ca-connector.polling-offset"></a> Polling Offset

The certificate import job maintains a polling offset. It refers to the numeric identifier of the certificates created by the ADCS. On every import schedule all certificates with an internal id bigger than the current offset.
Resetting the polling offset to zero forces a complete reimport of all issued certificates. This does not cause any harm to already known certificates.

### Administration of Pipelines

#### <a id="pipeline.name"></a> Pipeline Name

Provide a descriptive name of this pipeline. It is recommended to provide relevant information what type of certificate will be processed by this pipeline.

#### <a id="pipeline.active"></a> Active

Activate or deactivate this pipeline.

#### <a id="pipeline.approval-required"></a> Approval required

Activate this checkbox if an approval required by an RA officer for issuing a certificate requested by this pipeline.
An approval is quite common for web based requests, deactivate it only e.g. for testing environments, where an authorization is not required.
On the other hand it is unusual to request an approval for auto-enrollment protocols like ACME and SCEP.

#### <a id="pipeline.type"></a> Request type

Select a mode of certificate request. Depending on the request type the set of optional and required settings for the pipeline will vary. The different types are

- 'WEB' activates a web interface for the requestor to enter a request in different ways .
- 'ACME' selects the ACME certificate enrollment protocol defined in [RFC 8555](https://datatracker.ietf.org/doc/html/rfc8555).
- 'SCEP' selects the SCEP certificate enrollment protocol based on [RFC 8894](https://datatracker.ietf.org/doc/html/rfc8894). The protocol definition is a veteran of auto enrollment specification and many variants are implemented.

#### <a id="pipeline.url-part"></a> URL part

To support a specific protocol in different variants ( defined by pipeline) ca3s offers different endpoints. This input field offers the suffix for the protocols URLS:

- ACME: https://{host}:{port}/acme/{url-part}/directory
- SCEP: http://{host}:{port}/scep/{url-part}
- WEB: The web interface does not use this field. All web-style pipelines are accessible thru the start page.

#### <a id="pipeline.description"></a> Pipeline Description

This multiline input field offers a way to provide the web user with detailed information about this pipeline. It will be shown on the certificate request page.
For auto enrollment pipelines this field is a documentation for the administrator.

#### <a id="pipeline.list-order"></a> List order

If there is a long list of 'WEB' typed pipelines, for users the retrieval of the appropriate entry maybe be tricky. This numeric field allows to change the order of the web UI selection list. The ordering is ascending, a low number means a top position.

#### <a id="pipeline.ca-connector"></a> CA Connector

This listbox allows the selection of the appropriate CA for request processing.

#### <a id="pipeline.dn-cardinality"></a> DN part's cardinalities

This listbox allows to define the cardinality of the parts of the distinguished name:

- NOT_ALLOWED: This element MUST not appear in the request.
- ZERO_OR_ONE: This element may be present once or not all.
- ONE_OR_SAN: This element is specific for the 'common name' element. A common name or at least one SAN element must present in the request.
- ONE: This element MUST appear exactly once in the request.
- ZERO_OR_MANY: This element may appear in any number, including none.
- ONE_OR_MANY: This element may appear at least once or more.

#### <a id="pipeline.template"></a> DN part's template

Define a preselected value for this part of the distinguished name.

#### <a id="pipeline.regex"></a> DN part's regular expression

Define a regular expression that the value of this part of the distinguished name must match.

#### <a id="pipeline.san.restrictions"></a> SAN restrictions

Define restriction on the subject alternative names (SAN) of the request analog to the distinguished name.

#### <a id="pipeline.san.restrictions"></a> Additional request attribute

Define additional attribute required for processing of this pipeline. The aspects defined here are:

- Name: Define a name identifying this information chunk.
- Template: A preselected value.
- Regular Expression: A regular expression that this attribute name must match.
- Attribute Required: Is this attribute required or just informative.
- Attribute Comment: A hint for the user to provide the expected value.

#### <a id="pipeline.pending-on-failure"></a> Pending on failure

This checkbox allows the option for auto enrollment requests to enter the 'PENDING' state instead of 'FAILED'. This may allow the RA officer to confirm the request manually.

#### <a id="pipeline.ip-as-subject"></a> Allow an IP address as subject

This checkbox allows the request's 'common name' to be an IP address.

#### <a id="pipeline.ip-as-san"></a> Allow an IP address as Subject Alternative Name (SAN)

This checkbox allows the request's SANs to include IP address.

#### <a id="pipeline.csr-usage"></a> Key usages (server side creation, only)

This listbox allows the selection of key usages and extended key usages for certificate requests. It applies for the 'serverside key creation', only. It does not apply to incoming CSRs and does not apply tonthe rules the CA applies to the requests.

#### <a id="pipeline.domain-ra"></a> Domain RA

This listbox allows the selection of users to assign the role of a domain specific RA officer. The user can act as an RA officer for requests processed by this pipeline. The assignements are useful for web type pipelines, especially.

#### <a id="pipeline.notify-ra-on-pending-request"></a> Notify RA officers on pending request

This checkbox enables an email notification to be sent for incoming requests that require re officer interaction. It may be useful for time critical requests but may cause a flooding of the ra officer's inbox.

#### <a id="pipeline.additional-email-recipients"></a> Define additional email recipients

This input field allows the definition of additional email recipients that will be notified on certificate creation by this pipeline.

#### <a id="pipeline.process.create"></a> BPMN process 'Create'

This listbox allows the selection of a BPMN process that will be invoked for certificate creation by this pipeline.

#### <a id="pipeline.process.revoke"></a> BPMN process 'Revoke'

This listbox allows the selection of a BPMN process that will be invoked for certificate revocation by this pipeline.

#### <a id="pipeline.process.create"></a> BPMN process 'Notify'

This listbox allows the selection of a BPMN process that will be invoked after successful certificate creation by this pipeline. As the certificate is already created this BPMN process has no option to veto on it. This is useful to forward the certificate ( and / or certificate information) to e.g. resource management systems.
The process is also invoked on revocations.

#### SCEP specific settings

##### <a id="pipeline.scep.secret"></a> SCEP secret

This password entry field allows the definition of the client secret required for an initial enrollment.

##### <a id="pipeline.scep.secret-valid-to"></a> SCEP secret 'valid to'

This date entry field defines the end of the validity period of the SCEP secret.

##### <a id="pipeline.scep.recipient-dn"></a> SCEP's recipient certificate DN

Current SCEP implementations doe NOT use the CA certificate for transport encryption but a dedicated 'recipient' certificate.
This entry field allows the definition of the distinguished name of the recipient certificate.

##### <a id="pipeline.scep.ca-connector-recipient"></a> Connector for issuing the recipient certificate

This listbox allows the selection of the appropriate CA connector for issuing the recipient certificate.This must not be identical to the connector issuing the certificates requested by the SCEP client.

#### ACME specific settings

##### <a id="pipeline.acme.allow-challenge-http01"></a> Allow HTTP-01 Challenge

This checkbox allows the use of the most common HTTP-01 challenge. The client is required to allow incoming validation requests to port 80 and the path '/.wellknown/acme-challenge/\*'. Make sure that the usual 'redirect to HTTPS'-rule does not apply to this path.
The details of this challenge are available at [RFC 8555-8.3](https://datatracker.ietf.org/doc/html/rfc8555#section-8.3) .

##### <a id="pipeline.acme.allow-challenge-alpn"></a> Allow ALPN Challenge

This checkbox allows the use of the ALPN challenge. The distinct advantage of this challenge is that it does not required an additional port for incoming validation requests. But it requires to present a specifically crafted certificate as the response to the challenge. This isn't a problem on the initial setup but may lead to irritations for users as it may cause security warnings while performing the renewal process. The details of this challenge are available at [RFC 8737](https://www.rfc-editor.org/rfc/rfc8737.html).

##### <a id="pipeline.acme.pipeline.acme.allow-challenge-dns"></a> Allow DNS Challenge

This checkbox allows the use of the DNS challenge. This challenge type has the outstanding feature of validating wildcard certificate requests.
The client has no requirements to open any ports but requires write access to the relevant DNS server.
The details of this challenge are available at [RFC 8555-8.4](https://datatracker.ietf.org/doc/html/rfc8555#section-8.4) .

##### <a id="pipeline.acme.allow-wildcards"></a> Allow wildcards

This checkbox allows requesting wildcard certificate. It can only be used with the DNS Challenge.
The major use case of this checkbox is to prohibit wildcards when DNS Challenge is allowed.

##### <a id="pipeline.acme.check-caa"></a> Check CAA record

This checkbox allows requesting the check the 'DNS Certification Authority Authorization (CAA)' record for allowed CAs.
If this CA is not included in the CAA record the request will be rejected.
The details of this record are available at [RFC 6488](https://datatracker.ietf.org/doc/html/rfc6844) .

##### <a id="pipeline.acme.ca-name-caa"></a> CAA record name

This entry field defines the ca name that MUST be included in the CAA if the 'Check CAA record' flag is active.

##### <a id="pipeline.acme.request-proxies"></a> ACME request proxies

This listbox allows the selection of ACME request proxies that should be serving this pipeline. Use Ctrl-Click to select multiple entries.

### ACME Request Proxy settings

ACME was designed with the internet in mind, where every resource is directly accessible. In an intranet this is usually not the case as there can be several more or less strictly separated network segments.
Nevertheless, the use of ACME offers big advantages. With ACME enabled, ca3s can take advantage of 'request proxies'. These are separate units that reside in network segments and enable the required communication with ca3s to provide the ACME protocol. The request proxy forwards the ACME calls to ca3s (like a usual HTTP would do) and signs the requests. But the important task is the resolution of the challenges. The network segmentation prohibits these calls so the request proxy polls for pending challenges and resolves in the intended target network.

The ACME request proxy can be downloaded as an executable jar from [maven central](https://mvnrepository.com/artifact/de.trustable.ca3s.acmeproxy/ca-3-s-acme-proxy), the source code is available at [github](https://github.com/kuehne-trustable-de/acmeProxy) .

#### <a id="requestProxyConfig.id"></a> Request proxy ID

This is a unique id serving as an identifier for this request proxy. It is required in the configuration of the request proxy to identify it.

#### <a id="requestProxyConfig.name"></a> Request proxy name

Provide a descriptive name of this proxy. It is recommended to provide relevant information about its location / network segment it is supposed to support. These names occur in the pipeline configuration form, see [Pipeline section](#pipeline.acme.request-proxies) .

#### <a id="requestProxyConfig.proxy-url"></a> Request proxy url

This entry field defines the URL where ca3s is able to access the request proxy.

#### <a id="requestProxyConfig.active"></a> Active

Activate or deactivate this request proxy.

#### <a id="requestProxyConfig.secret"></a> Secret passphrase

This password field requires the secret passphrase. This is used by the request proxy to authenticate itself and to sign the transferred data.

### BPMN Process Info

The certificate creation and management task needs integration into the existing environment, either to check with information sources to validate a request or e.g. to forward certificate information to management systems.
ca3s is designed to support the major CA systems. Nevertheless, there may be the need to adapt to a CA that does not support CMP (or the license for such an option is too expensive).
In this the BPMN process may serve as a adaptor to SOAP or REST endpoints. Alternatively, it may invoke a Java library or simply execute a command line program.

Internally ca3s uses the [camunda engine](https://docs.camunda.io/) and we recommend the [camunda modeler](https://camunda.com/de/download/modeler/).

#### <a id="bpmn.name"></a> Secret passphrase

Provide a descriptive name of this process. It is recommended to provide relevant information about its performed task and its prerequisites. These names occur in the pipeline configuration form, see [Pipeline section](#pipeline.process.create) and the two following selections.

#### <a id="bpmn.type"></a> BPMN process type

The internal workflow expect several occasions where BPN process can be inserted. The different types are:

- Certificate notification: A process called after a certificate is issued or revoked.
- Certificate creation: A process performing the certificate creation.
- Certificate revocation: A process performing the certificate revocation.
- Request authorization: A validation step in the ACME protocol to ensure only valid accounts will be registered.

Choose the appropriate type for your BPMN process. The types ensures that the registered BPMN processes can only be used at places they are intended for.

#### <a id="bpmn.upload"></a> Upload of a BPMN process file

Select a BPMN process file to be uploaded into the ca3s database.

#### <a id="bpmn.checkBpmn"></a> Check a BPMN process

This button starts the test run of this BPMN process. Depending on the process type, a certificate or csr id must be provided in the input fields. Especially for the revocation processes make your you are in a test environment or select non-productive certificates!
