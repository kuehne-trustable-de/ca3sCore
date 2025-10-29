# Preparation for SPNego authentication

## definitions / sample names
hostname : 'ca3s.trustable.demo'

In this example the realm is 'TRUSTABLE.DEMO' which will be referred to as {realm}. Attention: It has to be UPPERCASE!
The domain is 'trustable' which will be referred to as {domain}. No case restriction here.

## procedure
- Logon to the DC as Administrator

- ensure that AES cipher is enabled for the participants in the kerberos protocol

- create a Windows user with a name different from the hostname

Create a User for the ca3s service to communicate with the KDC, let us use ca3sService

username : ca3sService
password : secret#123

- check the existing SPNs


    setspn -L HTTP/{hostname}
    setspn -L http/{hostname}

drop existing mappings

- map the user name and the host name


    ktpass -princ HTTP/{hostname}@{realm} -crypto AES256-SHA1 -pass {password} -ptype KRB5_NT_PRINCIPAL -mapuser {domain}\{user_name}

Hint: some pages recommend the option '-crypto ALL' . This doesn't seem to be supported anymore.

- check the Service Principal mappings


    setspn -L {username}

One or more mappings will be listed


- Logon to the service host

- download the keytab providing the service user's password


    ktab -k {path to keytab file} -a HTTP/{hostname}@{realm}

- verify the keytab content


    ktab -l -k {path to keytab file}

- configure the application to use the keytab file an the principal


    ca3s:
      auth:
        kerberos:
          service-principal: HTTP/{hostname}@{realm}
          keytab-location: {path to keytab file}
      ad-domain: {domain}


- configure the application to use the AD to retrieve user / role information

Provide the access data to the AD / LDAP.

Define the LDAP roles to be mapped into ca3s roles, e.g. a user being member of 'CN=ca3sUsers,DC=trustable,DC=demo' (present in the attribute 'memberOf') is assigned to role 'user'. Similar definitions exist for the other roles.
The special value '*' for user maps any user to this role.

The attributes sections defines a mapping of LDAP attributes to ca3s user attributes.

    ca3s:
      auth:
        ldap:
          host: dc.trustable.demo
          port: 636
          baseDN: DC=trustable,DC=demo
          principal: ca3sService@trustable.demo
          password: s3cr3t

          roles:
            user: 'CN=ca3sUsers,DC=trustable,DC=demo'
            domainra:
            ra: 'CN=ca3sRaOfficers,DC=trustable,DC=demo'
            admin: 'CN=ca3sAdmins,DC=trustable,DC=demo'
          attributes:
            firstName: givenName
            lastName: sn
            email: mail
            language:


Restart the server and make sure that the certificate of the AD / LDAP is trusted in ca3s. 
Now you are ready to test the single-sign-on functionality.

Troubleshooting:

!! Ensure the browser accepts ca3s servers's certificate as secure! Otherwise an NTLM ticket will be issued instead of a Kerberos ticket.
!! Don't try user authentication as 'Administrator' user, it won't work.

https://stackoverflow.com/questions/33927316/spring-security-kerberos-ad-checksum-fail
