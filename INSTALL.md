Getting Started with DCM4CHEE ARR 4.3.0-SNAPSHOT
==================================================

Requirements
------------
-   Java SE 6 or later - tested with [OpenJDK](http://openjdk.java.net/)
    and [Oracle JDK](http://java.com/en/download)

-   [JBoss Application Server 7.1.1.Final](http://www.jboss.org/jbossas/downloads)
    or [JBoss Enterprise Application Platform 6.x](http://www.jboss.org/jbossas/downloads)

-   Supported SQL Database:
    - [MySQL 5.6](http://dev.mysql.com/downloads/mysql)
    - [PostgreSQL 9.2.1](http://www.postgresql.org/download/)
      (not yet tested!)
    - [Firebird 2.5.1](http://www.firebirdsql.org/en/firebird-2-5-1/)
      (not yet tested!)
    - [DB2 10.1](http://www-01.ibm.com/software/data/db2/express/download.html)
      (not yet tested!)
    - [Oracle 11g](http://www.oracle.com/technetwork/products/express-edition/downloads/)
    - [Microsoft SQL Server](http://www.microsoft.com/en-us/download/details.aspx?id=29062)
      (not yet tested!)

-   LDAP Server - tested with
    - [Apache DS 2.0.0-M8](http://directory.apache.org/apacheds/2.0/downloads.html).

-   LDAP Browser - [Apache Directory Studio 1.5.3](http://directory.apache.org/studio/)

    *Note*: Both LDAP and Java Preferences can be used to configure
    the ARR, however you need to do the configuration changes manually,
    either in the LDAP tree or in the Preferences XML file.
    Also be careful to load the Preferences XML file in case you will use
    Preferences as your configuration.
    A tool for loading preferences XML is available in the dcm4che library [xml2prefs, xml2prefs.bat]
    (http://sourceforge.net/projects/dcm4che/files/dcm4che3/3.3.1/dcm4che-3.3.1-bin.zip)

Download and extract binary distribution package
------------------------------------------------
DCM4CHEE Audit Record Repository 4.x binary distributions for different databases can be obtained
from [Sourceforge](https://sourceforge.net/projects/dcm4che/files/dcm4chee-arr4/).
Extract (unzip) your chosen download to the directory of your choice.

Initialize Database
-------------------

### MySQL

1. Enable remote access by commenting out `skip-networking` in configuration file `my.conf`.

2. Create database and grant access to user

        > mysql -u root -p<root-password>
        mysql> CREATE DATABASE <database-name>;
        mysql> GRANT ALL ON <database-name>.* TO '<user-name>' IDENTIFIED BY '<user-password>';
        mysql> quit

3. Create tables and indexes
       
        > mysql -u <user-name> -p<user-password> < create-table-mysql.ddl


### PostgreSQL

1. Create user with permission to create databases 

        > createuser -U postgres -P -d <user-name>
        Enter password for new role: <user-password> 
        Enter it again: <user-password> 

2. Create database

        > createdb -U <user-name> <database-name>

3. Create tables and indexes
       
        > psql -U <user-name> < create-table-psql.ddl


### Firebird

1. Define database name in configuration file `aliases.conf`:

        <database-name> = <database-file-path>

2. Create user

        > gsec -user sysdba -password masterkey \
          -add <user-name> -pw <user-password>

3. Create database, tables and indexes

        > isql 
        Use CONNECT or CREATE DATABASE to specify a database
        SQL> CREATE DATABASE 'localhost:<database-name>'
        CON> user '<user-name>' password '<user-password>';
        SQL> IN create-table-firebird.ddl;
        SQL> EXIT;

        
### DB2

1. Create database and grant authority to create tables to user
   (must match existing OS user)

        > sudo su db2inst1
        > db2
        db2 => CREATE DATABASE <database-name> PAGESIZE 16 K
        db2 => connect to <database-name>
        db2 => GRANT CREATETAB ON DATABASE TO USER <user-name>
        db2 => terminate
 
2. Create tables and indexes

        > su <user-name>
        Password: <user-password>
        > db2 connect to <database-name>
        > db2 -t < create-table-db2.ddl
        > db2 terminate
        

### Oracle 11g 

1. Install the oracle 11g database server
2. during the install you will be prompted to create a new Database
   instance create a new one database-name
3. keep the connection information you are given at the end
4. Connect to Oracle and create a new tablespace

        $ sqlplus / as sysdba
        SQL> CREATE BIGFILE TABLESPACE <tablespace-name> DATAFILE '<data-file-location>' SIZE <size>;

        Tablespace created.

5. Create a new user with privileges for the new tablespace

        $ sqlplus / as sysdba
        SQL> CREATE USER <user-name> 
        2  IDENTIFIED BY <user-password>
        3  DEFAULT TABLESPACE <tablespace-name>
        4  QUOTA UNLIMITED ON <tablespace-name>
        5  QUOTA 50M ON SYSTEM;

        User created.

        SQL> GRANT CREATE SESSION TO <user-name>;
        SQL> GRANT CREATE TABLE TO <user-name>;
        SQL> GRANT CREATE ANY INDEX TO <user-name>;
        SQL> GRANT CREATE SEQUENCE TO <user-name>;
        SQL> exit

6. Create tables and indexes

        $ sqlplus <user-name>/<user-password>
        SQL> @create-table-oracle.ddl

Setup LDAP Server
-----------------


### OpenDJ

1.  Copy LDAP schema files for OpenDJ from DCM4CHEE ARR distribution to
    OpenDJ schema configuration directory:

        > cp ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/ldap/opendj/* $OPENDJ_HOME/config/schema/ [UNIX]
        > copy .\dcm4chee-arr-4.0.0-SNAPSHOT-mysql\ldap\opendj\* %OPENDJ_HOME%\config\schema\ [Windows]

2.  Run OpenDJ GUI based setup utility

        > $OPENDJ_HOME/setup
    
    Log the values choosen for
    -  LDAP Listener port (1389)
    -  Root User DN (cn=Directory Manager)
    -  Root User Password (secret)
    -  Directory Base DN (dc=example,dc=com)

    needed for the LDAP connection configuration of DCM4CHEE ARR.

4. After initial setup, you may start and stop OpenDJ by

        > $OPENDJ_HOME/bin/start-ds
        > $OPENDJ_HOME/bin/stopt-ds


### OpenLDAP

OpenLDAP binary distributions are available for most Linux distributions and
for [Windows](http://www.userbooster.de/en/download/openldap-for-windows.aspx).

OpenLDAP can be alternatively configured by

- [slapd.conf configuration file](http://www.openldap.org/doc/admin24/slapdconfig.html)
- [dynamic runtime configuration](http://www.openldap.org/doc/admin24/slapdconf2.html)

See also [Converting old style slapd.conf file to cn=config format][1]

[1]: http://www.openldap.org/doc/admin24/slapdconf2.html#Converting%20old%20style%20{{slapd.conf}}%285%29%20file%20to%20{{cn=config}}%20format

#### OpenLDAP with slapd.conf configuration file

1.  Copy LDAP schema files for OpenLDAP from the assembly zip to
    OpenLDAP schema configuration directory:

        > cp ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/ldap/schema/* /etc/openldap/schema/ [UNIX]
        > copy .\dcm4chee-arr-4.0.0-SNAPSHOT-mysql\ldap\schema\* \Program Files\OpenLDAP\schema\ [Windows]

2.  Add references to schema files in `slapd.conf`, e.g.:

        include         /etc/openldap/schema/core.schema
        include         /etc/openldap/schema/dicom.schema
        include         /etc/openldap/schema/dcm4che.schema
        include         /etc/openldap/schema/dcm4chee-arr.schema

3.  You may also change the default values for 

        suffix          "dc=my-domain,dc=com"
        rootdn          "cn=Manager,dc=my-domain,dc=com"
        rootpw          secret
   
    in `slapd.conf`.


#### OpenLDAP with dynamic runtime configuration

1.  Import LDAP schema files for OpenLDAP runtime configuration, binding as
    root user of the config backend, using OpenLDAP CL utility ldapadd, e.g.:

        > ldapadd -xW -Dcn=config -f ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/ldap/slapd/dicom.ldif
        > ldapadd -xW -Dcn=config -f ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/ldap/slapd/dcm4che.ldif
        > ldapadd -xW -Dcn=config -f ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/ldap/slapd/dcm4chee-arr.ldif

    If you don't know the root user and its password of the config backend, you may
    look into `/etc/openldap/slap.d/cn=config/olcDatabase={0}config.ldif`:

        olcRootDN: cn=config
        olcRootPW:: VmVyeVNlY3JldA==

    and decode the base64 decoded password, e.g:

        > echo -n VmVyeVNlY3JldA== | base64 -d
        VerySecret

    If there is no `olcRootPW` entry, you may just add one.

    You may also specify the password in plan text, e.g:

        olcRootPW: VerySecret

2.  Directory Base DN and Root User DN can be modified by changing the values of
    attributes

        olcSuffix: dc=my-domain,dc=com
        olcRootDN: cn=Manager,dc=my-domain,dc=com

    of object `olcDatabase={1}hdb,cn=config` by specifing the new values in a 
    LDIF file (e.g. `modify-baseDN.ldif`)

        dn: olcDatabase={1}hdb,cn=config
        changetype: modify
        replace: olcSuffix
        olcSuffix: dc=example,dc=com
        -
        replace: olcRootDN
        olcRootDN: cn=Manager,dc=example,dc=com
        -

    and applying it using OpenLDAP CL utility ldapmodify, e.g.:

        > ldapmodify -xW -Dcn=config -f modify-baseDN.ldif

### Apache DS 2.0

1.  Install [Apache DS 2.0.0-M8](http://directory.apache.org/apacheds/2.0/downloads.html)
    on your system and start Apache DS.

2.  Install [Apache Directory Studio 1.5.3](http://directory.apache.org/studio/) and
    create a new LDAP Connection with:

        Network Parameter:
            Hostname: localhost
            Port:     10398
        Authentication Parameter:
            Bind DN or user: uid=admin,ou=system
            Bind password:   secret

3.  Import LDAP schema files for Apache DS:
    dcm4chee-arr-4.0.0-SNAPSHOT-mysql\ldap\schema\*
    imports are to be done in this order:
    
        1.dicom.ldif
        2.dcm4che.ldif
        3.dcm4chee-arr.ldif

    using the LDIF import function of Apache Directory Studio LDAP Browser.

4.  You may modify the default Directory Base DN `dc=example,dc=com` by changing
    the value of attribute 

        ads-partitionsuffix: dc=example,dc=com`

    of object

        ou=config
        + ads-directoryServiceId=default
          + ou=partitions
              ads-partitionId=example
    
    using Apache Directory Studio LDAP Browser.


Import sample configuration into LDAP Server
--------------------------------------------  

1.  If not alread done, install
    [Apache Directory Studio 1.5.3](http://directory.apache.org/studio/) and create
    a new LDAP Connection corresponding to your LDAP Server configuration, e.g:

        Network Parameter:
            Hostname: localhost
            Port:     1398
        Authentication Parameter:
            Bind DN or user: uid=admin,ou=system
            Bind password:   secret
        Browser Options:
            Base DN: dc=example,dc=com
            
2.    Import basic configuration template dcm4chee-arr-cdi-conf-ldap/src/main/config/
    
        1.init-config.ldif
        2.arrdevice-sample(no init).ldif
        3.AuditLogger_with_supress_criteria.ldif
            
2.  If you configured a different Directory Base DN than`dc=example,dc=com`,
    you have to replace all occurrences of `dc=example,dc=com` in LDIF files
    before import by your Directory Base DN, e.g.:

        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ init-config.ldif
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ arrdevice-sample(no init).ldif
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ AuditLogger_with_supress_criteria.ldif

Setup Java Preferences (LDAP alternative)
----------------
1.  Load the sample xml file dcm4chee-arr-sample.xml from dcm4chee-arr-cdi-conf-prefs/src/main/config/
    into your system properties.
2.    A tool for loading preferences XML is available in the dcm4che library [xml2prefs, xml2prefs.bat]
    (http://sourceforge.net/projects/dcm4che/files/dcm4che3/3.3.1/dcm4che-3.3.1-bin.zip)
        
        
Setup JBoss AS 7
----------------

1.  To configure the Audit Record Repository to use LDAP, put the following into
    the Jboss configuration/standalone.xml, and adjust the
    parameters according to the LDAP server installed:

        <system-properties>
            <property name="org.dcm4che.conf.storage" value="ldap" />
            <property name="org.dcm4che.conf.ldap.url" value="ldap://localhost:10389/dc=example,dc=com" />
            <property name="org.dcm4che.conf.ldap.principal" value="uid=admin,ou=system" />
            <property name="org.dcm4che.conf.ldap.credentials" value="secret" />
        </system-properties>

    Check the application log during startup to see which values are actually used.

To use preferences change the org.dcm4che.conf.storage value to "prefs"

3.  Install DCM4CHE {lib-version} libraries as Jboss modules:

        > cd  $JBOSS_HOME
        > unzip ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/jboss-module/dcm4che-jboss-modules-{lib-version}.zip

4.  Install QueryDSL 3.2.3 libraries as WildFly module:

        > cd  $JBOSS_HOME
        > unzip ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/jboss-module/querydsl-jboss-modules-3.2.3.zip

5.  Install JDBC Driver. DCM4CHEE Audit Record Repository 4.x binary distributions do not include
    a JDBC driver for the database for license issues. You may download it from:
    -   [MySQL](http://www.mysql.com/products/connector/)
    -   [PostgreSQL]( http://jdbc.postgresql.org/)
    -   [Firebird](http://www.firebirdsql.org/en/jdbc-driver/)
    -   [DB2](http://www-306.ibm.com/software/data/db2/java/), also included in DB2 Express-C
    -   [Oracle](http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.htm),
        also included in Oracle 11g XE)
    -   [Microsoft SQL Server](http://msdn.microsoft.com/data/jdbc/)

    The JDBC driver can be installed either as a deployment or as a core module.
    [See](https://docs.jboss.org/author/display/WFLY8/Developer+Guide#DeveloperGuide-InstalltheJDBCdriver)
    
    Installation as deployment is limited to JDBC 4-compliant driver consisting of **one** JAR.

    For installation as a core module, `./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/jboss-module/jdbc-jboss-modules-1.0.0-<database>.zip`
    already provides a module definition file `module.xml`. You just need to extract the ZIP file into
    $JBOSS_HOME and copy the JDBC Driver file(s) into the sub-directory, e.g.:

        > cd $JBOSS_HOME
        > unzip ./dcm4chee-arr-4.0.0-SNAPSHOT-mysql/jboss-module/jdbc-jboss-modules-1.0.0-db2.zip
        > cd $DB2_HOME/java
        > cp db2jcc4.jar db2jcc_license_cu.jar $JBOSS_HOME/modules/com/ibm/db2/main/

    Verify, that the actual JDBC Driver file(s) name matches the path(s) in the provided
    `module.xml`, e.g.:

         <?xml version="1.0" encoding="UTF-8"?>
         <module xmlns="urn:jboss:module:1.1" name="com.ibm.db2">
             <resources>
                 <resource-root path="db2jcc4.jar"/>
                 <resource-root path="db2jcc_license_cu.jar"/>
             </resources>
         
             <dependencies>
                 <module name="javax.api"/>
                 <module name="javax.transaction.api"/>
             </dependencies>
         </module>


6.  Start JBoss AS 7 or EAP 6.X in standalone mode with the Java EE 6 Full Profile configuration.
    To preserve the original JBoss AS 7 configuration you may copy the original
    configuration file for JavaEE 6 Full Profile:

        > cd $JBOSS_HOME/standalone/configuration/
        > cp standalone-full.xml dcm4chee-arr.xml

    and start JBoss AS 7 specifying the new configuration file:
        
        > $JBOSS_HOME/bin/standalone.sh -c dcm4chee-arr.xml [UNIX]
        > %JBOSS_HOME%\bin\standalone.bat -c dcm4chee-arr.xml [Windows]
   
    Verify, that JBoss AS 7 started successfully, e.g.:

        =========================================================================

          JBoss Bootstrap Environment

          JBOSS_HOME: /home/gunter/jboss7

          JAVA: /usr/lib/jvm/java-6-openjdk/bin/java

          JAVA_OPTS:  -server -XX:+UseCompressedOops -XX:+TieredCompilation ...

        =========================================================================

        13:01:48,788 INFO  [org.jboss.modules] JBoss Modules version 1.1.1.GA
        13:01:48,926 INFO  [org.jboss.msc] JBoss MSC version 1.0.2.GA
        13:01:48,969 INFO  [org.jboss.as] JBAS015899: JBoss AS 7.1.1.Final "Brontes" starting
        :
        13:01:51,239 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss AS 7.1.1.Final "Brontes" started ...
                
    Running JBoss AS 7 in domain mode should work, but was not yet tested.

7.  Add JDBC Driver into the server configuration using JBoss AS 7 CLI in a new console window:

        > $JBOSS_HOME/bin/jboss-cli.sh -c [UNIX]
        > %JBOSS_HOME%\bin\jboss-cli.bat -c [Windows]
        [standalone@localhost:9999 /] /subsystem=datasources/jdbc-driver=<driver-name>:add(driver-module-name=<module-name>,driver-name=<driver-name>)

    You may choose any `<driver-name>` for the JDBC Driver, `<module-name>` must match the name
    defined in the module definition file `module.xml` of the JDBC driver, e.g.:

        [standalone@localhost:9999 /] /subsystem=datasources/jdbc-driver=mysql:add(driver-module-name=com.mysql,driver-name=mysql)

8.  Create and enable a new Data Source bound to JNDI name `java:/arrDS` using JBoss AS 7 CLI:

        [standalone@localhost:9999 /] data-source add --name=arrDS \
        >     --driver-name=<driver-name> \
        >     --connection-url=<jdbc-url> \
        >     --jndi-name=java:/arrDS \
        >     --user-name=<user-name> \
        >     --password=<user-password>
        [standalone@localhost:9999 /] data-source enable --name=arrDS

    The format of `<jdbc-url>` is JDBC Driver specific, e.g.:
    -  MySQL: `jdbc:mysql://localhost:3306/<database-name>`
    -  PostgreSQL: `jdbc:postgresql://localhost:5432/<database-name>`
    -  Firebird: `jdbc:firebirdsql:localhost/3050:<database-name>`
    -  DB2: `jdbc:db2://localhost:50000/<database-name>`
    -  Oracle: `jdbc:oracle:thin:@localhost:1521:<database-name>`
    -  Microsoft SQL Server: `jdbc:sqlserver://localhost:1433;databaseName=<database-name>`

9.  By default, DCM4CHEE ARR will assume `dcm4chee-arr` as its Device Name, used to find its
    configuration in the configuration backend (LDAP Server or Java Preferences). You may specify a different
    Device Name by system property `org.dcm4chee.arr.deviceName` using JBoss AS 7 CLI:

        [standalone@localhost:9999 /] /system-property=org.dcm4chee.arr.deviceName:add(value=<device-name>)

        *Note*
        If you change the device name from the default you will have to change the dn of the sample configuration
        to match the new device name fom dcm4chee-arr to the new name.
        
10. DCM4CHEE ARR requires a keystore file for TLS as well as a truststore
     
     By default DCM4CHEE ARR will assume that both the keystore and the truststore are at ${jboss.server.config.url}/dcm4chee-arr/key.jks
     from the sample config provided and will assume such keystore is of type JKS and with password changeit,
     It will also assume that you have a key inside with a password changeit.
     
     It is up to you to change these configurations according to your needs, if you require proper TLS authentication,
     then you should provide a new configuration parameter in the ldap config with the following attributes :
     
     dcmTrustStoreType
     dcmTrustStoreURL
     dcmTrustStorePin
     
     You will also need to change the property dcmTLSNeedClientAuth in the audit-tls sample connection configuration
     to true, thus allowing the ARR to load the trust store and authenticate the incoming certificates.
     
     To generate your own key and keystore use the keytool supplied with the jdk as follows:
     
     
     keytool.exe -genkey -alias mynewkey -keyalg RSA -validity 365  -storetype JKS --keystore myserver.store
     
     
     you will be prompted for everything and in the end the file myserver.store will be generated.
     
     
     To extract your public key from the truststore use keytool as follows:
     
     
     keytool -export -alias mynewkey --keystore test.store -rfc -file mypublic.cert
     
     
     Next step is to create a truststore and add your public certificate to it.
     
     
     keytool -import -alias mynewkey -file mypublic.cert --storetype JKS --keystore server.truststore
     
     
     TOTEST: If you would prefer to have your certificates on LDAP without having to use a local trust store this is possible 
     using the following configuration scheme:
     1.    add a dicomAuthorizedNodeCertificateReference attribute to your device node with the value a node that is either a device or uses pkiUser ldap object class.
     2.    in the referenced node have a userCertificate attribute, specify it is binary and in the value you should add the certificate file 
     (this can be done wasily with the ldap browser, just point to your certificate file using load certificate when adding attribute value)
     
11.    Add Jms queue to the jboss configuration via CLI as follows:

    [standalone@localhost:9999 /] jms-queue add --queue-address=ARRIncoming --entries=queue/ARRIncoming
     
12. Deploy DCM4CHEE ARR using JBoss AS 7 CLI, e.g.:

        [standalone@localhost:9999 /] deploy dcm4chee-arr-cdi/dcm4chee-arr-cdi-war/target/dcm4chee-arr-cdi-war-4.3.0-SNAPSHOT-${db}.war

    Verify that DCM4CHEE ARR was deployed and started successfully, e.g.:


13. You may undeploy DCM4CHEE ARR at any time using JBoss AS 7 CLI, e.g.:

        [standalone@localhost:9999 /] undeploy dcm4chee-arr-cdi-war-4.3.0-SNAPSHOT-${db}.war
        
14. You can also use the provided web interface for querying the ARR in an organized and clean way.
    To install the web application just deploy it on jboss as follows:
        [standalone@localhost:9999 /] deploy dcm4chee-arr-cdi/dcm4chee-arr-web/target/dcm4chee-arr-web-4.3.0-SNAPSHOT-${db}.war

Testing DCM4CHEE ARR 4.3.0-SNAPSHOT
----------------------------
Test running:
to test your ARR service is running you can do this via restful service after deployment just point your browser
to http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/ctrl/running

Test Stop:
to test your ARR service stops you can do this via restful service after deployment just point your browser
to http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/ctrl/stop

Test Start:
to test your ARR service starts you can do this via restful service after deployment just point your browser
to http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/ctrl/start

Test Receive Audits:
A tool for sending Audit messages is available in the dcm4che library [syslog, syslog.bat]
    (http://sourceforge.net/projects/dcm4che/files/dcm4che3/3.3.1/dcm4che-3.3.1-bin.zip)

Here is a test message

<AuditMessage>
<EventIdentification EventActionCode=\"E\" EventDateTime=\"2014-03-05T13:35:46.881+01:00\" EventOutcomeIndicator=\"4\">
<EventID code=\"110114\" codeSystemName=\"DCM\" displayName=\"User Authentication\"/>
<EventTypeCode code=\"110122\" codeSystemName=\"DCM\" displayName=\"Login\"/>
</EventIdentification>
<ActiveParticipant UserID=\"admin\" NetworkAccessPointID=\"10.231.163.243\" NetworkAccessPointTypeCode=\"2\"/>
<AuditSourceIdentification AuditSourceID=\"SOMESOURCE\">
<AuditSourceTypeCode code=\"4\"/>
</AuditSourceIdentification>
</AuditMessage>
save it to an xml file and send this test message as follows:
syslog --udp -c ARR-ip:ARR-udp-port savedfile.xml

Test Query Audits:
To query for this received audit message just point your browser to 
http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/view/xmllist?UserID=admin&AuditSourceID=SOMESOURCE

Testing the Web application:
To query the ARR using the web application provided just point you browser to
http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr-web/

