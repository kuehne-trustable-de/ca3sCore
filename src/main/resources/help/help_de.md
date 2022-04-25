# ![Hilfe](../../images/ca3s-72x72.png) Hilfe

[Download as Markdown](./help-de.md)

## **C**ertificate **A**utomation, **A**uthorization and **A**dministration **S**ervice (_ca3s_)

ca3s ist ein CA Unterstützungssystem mit einem flexiblen RA-Modul, welches BPMN mit dem Ziel benutzt, möglichst viel zu automatisieren. Zu diesem Zweck, werden zusätzlich zur normalen Web-Variante, ACME und SCEP Schnittstellen bereitgestellt. Außerdem werden Zertifikatsbestände aus verschiedenen Quellen aggregiert und es werden CMP-angebundene CAs oder ADCS-Instanzen für die Zertifikatserstellung benutzt.

- Verwaltet verschiedene CA Instanzen (CMP und ADCS)
- Erlaubt den Überblick über die Ablaufdaten aller relevanten Zertifikate aus allen Quellen.
- Analysiert die Schlüssel-Algorithmen, Schlüssellänge, Hash and Padding-Algorithmen in Nutzung
- Ein nutzerfreundliches Webinterface für Antragsteller und Registrierungsbeauftragten (auch als 'RA-Officer' bezeichnet).

Die wichtigsten Punkte für eine zuverlässige PKI Infrastruktur:

- Weitgehende Automatisierung des Ausstellens und Erneuerns
- Die Nutzung von BPMN um organisations-spezifische Regeln aufzustellen
- Bietet gut etablierte Schnittstellen an (ACME und SCEP) für eine erleichterte Automatisierung

Das Projekt steht unter einer Open-Source-Lizenz [EUPL](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12) und kann bei [github](https://github.com/kuehne-trustable-de/ca3sCore) heruntergeladen werden.

### Baustein-Übersicht

Die folgende Abbildung gibt einen groben Überblick über die ca3s Architektur. Oben sind die verschiedenen Typen von Clients, die sich mit dem ca3s-Server verbinden, abgebildet. Unten sind die externen Dienste, mit denen sich ca3s verbindet, gezeigt.

![Architekturübersicht](../../images/birds_view.jpg)

Architekturübersicht

#### Anfragen Adapter (Oberer Bereich)

Zertifikatsanfragen können gemäß ACME- und SCEP-Protokoll oder mittels einer Web-Oberfläche übermittelt werden. Zusätzliche Protokolle können einfach hinzugefügt werden.

#### CA Adapter (Unterer Bereich)

Verschiedene CA-Instanzen können mit ca3s verbunden werden. Ein weit verbreitetes Protokoll für CAs ist CMP, unterstützt von z.B. ejbca und vielen Weiteren. Zusätzlich dazu existiert ein separates Projekt (adcsCert) um sich mit dem Microsoft ADCS CA zu verbinden.

#### Zertifikatsdatenbank

Ein zentraler Zertifikatsspeicher enthält ausgegebene und anderweitig abgerufene Zertifikate, um so eine zentrale Anlaufstelle für den Zertifikatsstatus und die Berichterstellung, z.B. für eine Ablaufübersicht, zu erhalten.

#### Ausstellungsworkflow

Abhängig von der Konfiguration können Zertifikatsanforderungen von einem menschlichen Registrierungsbeauftragten zugelassen werden. Ergänzende automatisierte Workflows können so konfiguriert werden, dass der Registrierungsbeauftragte (auch RA-Officer genannt) von ermüdenden Aufgaben entlastet wird und selbst komplexe Prozesse automatisiert ausgeführt werden. Die Prozesse können in BPMN definiert und bestimmten Verarbeitungspipelines flexibel zugewiesen werden.

## Erklärungen der Optionen

### <a id="pkcsxx.upload.pipeline"></a> Verarbeitungs-Pipeline

Zertifikate können für eine Vielzahl von Anwendungsfällen ausgestellt werden. Diese Anwendungsfälle erfordern möglicherweise unterschiedliche ausstellende Zertifizierungsstellen, unterschiedliche Vertrauensstufen, unterschiedliche Verwendung und Gültigkeit sowie Ausstellungsanforderungen. Im folgenden Auswahlfeld werden verschiedene Verarbeitungspipelines angeboten. Zusätzliche Informationen werden möglicherweise unter der Auswahl angezeigt. Bitte stellen Sie sicher, dass Sie die richtige Pipeline für Ihre Bedürfnisse und Anforderungen auswählen, denn eine falsche Auswahl führt zur Ablehnung Ihrer Anfrage. Bei Fragen kontaktieren Sie den Registrierungsbeauftragten.

### <a id="pkcsxx.upload.creationMode.selection"></a> Erzeugungsmodus

- CSR available  
  Die einfachste Methode des Zertifikatsanforderungsprozesses besteht darin, eine CSR hochzuladen. Wählen Sie diese Option, wenn bereits ein CSR verfügbar ist, und fügen Sie ihn in den Textbereich unten ein. Alternativ können Sie auf Ihrem Computer nach einer CSR-Datei suchen.
- Serverside key creation  
   Wenn Ihre Sicherheitsrichtlinie diese Option zulässt, können Sie festlegen, dass der Server ein Schlüsselpaar für Sie erstellt. Sie können einen Schlüsseltyp und eine Schlüssellänge auswählen und die Details des Betreffs des Zertifikats definieren. Der Server erstellt einen Schlüssel- und Zertifikats-Container, der Ihren privaten Schlüssel schützt. Um Zugriff auf den privaten Schlüssel zu erhalten, müssen Sie ein Passwort für den Container angeben. Bewahren Sie dieses Passwort an einem sicheren Ort auf! Wenn das Passwort verloren geht, ist der geheime Schlüssel nicht mehr verfügbar. Es gibt keine Möglichkeit, das Passwort vom ca3s-Server abzurufen. Eine Kompromittierung des Passworts wirkt sich auf den privaten Schlüssel und das Zertifikat aus! Verwenden Sie keinen kompromittierten Schlüssel! Widerrufen Sie das Zertifikat in diesem Fall sofort!
  **Es wird dringend empfohlen, CSRs oder ein automatisiertes Zertifikatsverwaltungsprotokoll zu verwenden, z.B. ACME! Der private Schlüssel sollte das nutzende System nicht verlassen!**

### CSR-Erzeugung auf der Kommandozeile

Mit der Befehlszeilenoption können Sie die verschiedenen verfügbaren Tools für die CSR-Erstellung je nach Betriebssystem und Zielanwendung verwenden.

### <a id="pkcsxx.upload.creationTool.selection"></a> Tool-Auswahl

- Java keytool  
  Das Schlüssel- und Zertifikatsverwaltungstool von Java ist in der Java-Laufzeitverteilung unter Windows und Linux enthalten.
- OpenSSL > 1.1.0  
  Eine relativ neue Version des weit verbreiteten OpenSSL-Tools. Wenn Sie sich nicht sicher sind, überprüfen Sie Ihre Version mit 'openssl version'.
- OpenSSL  
  Eine Befehlszeilen-Skript-Version, die mit allen OpenSSL-Tools kompatibel ist.

### <a id="ca3SApp.help.subtitle.commandline"></a> CSR Erzeugung auf der Kommandozeile

Unter Sicherheitsgesichtspunkten empfiehlt es sich, das Schlüsselpaar auf dem Zielsystem (z. B. einem Webserver) zu erstellen und eine Zertifikats-Signierungs-Anforderung (certificate signing request, abgekürzt CSR) zu erstellen. Ein CSR enthält öffentliche Informationen (z.B. den Domänennamen des Webservers) und den öffentlichen Schlüssel. Der private Schlüssel verlässt das Zielsystem nicht, wenn das Zertifikat von der Zertifizierungsstelle angefordert wird. Um eine CSR zu erstellen, öffnen Sie bitte eine Eingabeaufforderung in Ihrem System:

- Windows: Command Prompt ('cmd.exe') or PowerShell
- Linux: Shell

Füllen Sie die erforderlichen Felder im ca3s-Webformular aus (Betreff, Organisation, SAN, ...). Wechseln Sie in das Verzeichnis, in dem der Schlüssel erstellt werden soll. In der Produktdokumentation Ihrer Anwendung finden Sie das entsprechende Verzeichnis.

![Zertifikatsparameter](../../images/sslold3_de.png)

Zertifikatsparameter

Überprüfen Sie anhand Ihrer Dokumentation, welche Art von "store" erwartet wird. Java-Anwendungen erwarten normalerweise einen PKCS12-Keystore. Apache-Webserver verarbeiten möglicherweise separate PEM-Dateien.

Mit Hilfe 'Werkzeugauswahl' wählen Sie zwischen den verschiedenen Werkzeuge aus:

- Java 'keytool'
  Für das Keytool von Java sind zwei Befehle erforderlich, um den Schlüssel und die CSR zu erstellen.
- OpenSSL > 1.1.0
  Diese aktuelle Version erstellt alle erforderlichen Dateien in einem Befehl.
- OpenSSL
  Die alten Versionen von OpenSSL erfordern eine Konfigurationsdatei und einen Shell-Befehl.
- Windows 'certreq'
  Für die Nutzung des 'certreq'-Tools ist eine Konfigurationsdatei und ein Kommandozeilenbefehl notwendig.

Kopieren Sie den generierten Wert aus dem Textfeld in Ihre Kommandozeile:

#### Linux

![Zertifikatsparameter](../../images/sslnew4.png)

Aufruf-Beispiel

#### Windows

![Zertifikatsparameter](../../images/java4shell.png)

Aufruf-Beispiel

#### Kopieren

Die Felder mit den generierten Kommandozeilen haben einen 'Copy'-Button. Alternativ kann der Text selektiert und kopiert werden.

![Copy](../../images/java_3_5.png)

Copy-Button

#### <a id="pkcsxx.upload.creationTool.cn.as.san"></a> Common Name als SAN anfügen

Für einige Anwendungsfälle (TLS Server) kann es sinnvoll sein, den Common Name zusätzlich als SAN anzugeben. Falls die aktuelle Pipeline die Nutzung von SANs erlaubt, ist diese Option verfügbar.
Ist der Common Name bereits als SAN vorhanden, hat diese Option keine Auswirkung.

#### <a id="pkcsxx.upload.machine.key.set"></a> Auswahl des Windows 'Machine Key Set' (bei certreq)

Diese Option erlaubt die Auswahl des zu nutzenden Keystores. Bitte konsultieren Sie die Dokumentation Ihrer Anwendung, welcher Store-Typ zu verwenden ist.
Falls aktiviert, wird durch diese Option der 'Machine Key Set' ausgewählt und der Nuzer, der das 'certreq'-Kommando ausführt, muss Administrator-Berechtigungen besitzen.

#### Dateinamen und FriendlyName (bei certreq)

Die in den Kommandozeilen erzeugte oder genutzte Dateien bestehen aus dem Common Name, dem aktuellen Datum und der Endung gemäss des Dateityps. So wird ein unbeabsichtigtes Überschreiben relevanter Dateien anderer Requests verhindert. Selbstverständlich können die Dateinamen gemäß der jeweiligen Anforderungen angepasst werden.

#### <a id="pkcsxx.upload.creationTool.cmdline"></a> Kommandozeilen-Beispiele

### Java keytool

Die generierte Java-Keytool-Befehlszeile besteht aus zwei Teilen:

Der erste Befehl generiert ein neues Schlüsselpaar mit der ausgewählten Schlüssellänge in einem PKCS12-Schlüsselspeicher (endet auf '.p12') mit dem Alias 'keyAlias'.

![](../../images/java4shell.png)

Informationen zum erforderlichen Keystore-Dateinamen und zum erwarteten Alias finden Sie in Ihrer Anwendungsdokumentation. Stellen Sie sicher, dass vorhandene Dateien nicht überschrieben werden.

Der zweite Befehl erstellt den CSR (Certificate Signing Request).
![](../../images/java5shell.png)

Der CSR liegt nun in der Dateiendung '.csr' vor.
Wechseln Sie nun in der Auswahlbox 'Erzeugungsmodus' auf 'CSR verfügbar' und laden Sie diese Datei hoch, um sie von der Zertifizierungsstelle bearbeiten zu lassen.

#### openSSL Version (< 1.1.1)

Die Information, welche Version von 'openSSL' auf Ihrem System installiert ist, lässt sich mit 'openssl version' schnell ermitteln.

![](../../images/sslold version.png)

##### <a id="pkcsxx.upload.creationTool.req.conf"></a> OpenSSL Konfigurationsdatei

Öffnen Sie einen Texteditor (in diesem Beispiel 'vi') auf Ihrem System. Kopieren Sie den generierten Wert aus dem Textfeld in Ihren Editor. Speichern Sie die Datei in dem Verzeichnis, in dem der Schlüssel erstellt werden soll.

![](../../images/sslold4.5.png)

#### OpenSSL Schlüssel- und CSR-Erzeugungesbefehl

Kopieren Sie den erzeugten Befehl aus dem Formularfeld (mittels Copy-Button oder Strg-C) in die Kommandozeile (Strg-V oder rechte / mittlere Maustaste) und führen Sie ihn aus.

Der CSR liegt nun in der Datei endend auf '.csr' vor.
Wechseln Sie nun in der Auswahlbox 'Erzeugungsmodus' auf 'CSR verfügbar' und laden Sie diese Datei hoch, um sie von der Zertifizierungsstelle bearbeiten zu lassen.

#### openSSL Version (>= 1.1.1)

Bei den neueren Versionen von 'openSSL' kann auf eine zusätzliche 'request.conf'-Datei verzichtet werden, ale relevanten Parameter werden als Parameter des openSSL-Aufrufs übergeben.

Kopieren Sie den erzeugten Befehl in ihre Kommandozeile und führen Sie sie aus.
![](../../images/sslold5.png)

Der CSR liegt nun in der Datei endend auf '.csr' vor.
Wechseln Sie nun in der Auswahlbox 'Erzeugungsmodus' auf 'CSR verfügbar' und laden Sie diese Datei hoch, um sie von der Zertifizierungsstelle bearbeiten zu lassen.

#### Windows 'certreq'

Auf Windows-Systemen ist das Kommandozeilen-Tool 'certreq' vorhanden, mit dem einfach Zertifikatsanforderungen für den lokalen Rechner erzeugt werden können. Der private Schlüssel wird dabei im Windows-eignem Speicher abgelegt.

##### <a id="pkcsxx.upload.creationTool.req.inf"></a> certreq Konfigurationsdatei (requestconfig.inf)

Die Konfigurationsdatei definiert neben den Zertifikatsparametern auch den 'Friendly Name' (aus Common Name und dem aktuellen Datum), unter dem der private Schlüssel im Windows-Store abgelegt wird. Dieser Name kann problemlos gemäß der Anforderungen des nutzenden Systems angepasst werden.
Öffnen Sie einen Texteditor (z. B. 'Editor') auf Ihrem System. Kopieren Sie die erzeugte Konfiguration aus dem Textfeld in Ihren Editor. Speichern Sie die Datei in dem Verzeichnis, in dem das certreq-Kommando ausgeführt und die CSR-Datei erzeugt werden soll.

![](../../images/editrequestconfig.inf.png)

##### certreq Schlüssel- und CSR-Erzeugungsbefehl

Kopieren Sie den erzeugten Befehl aus dem Formularfeld (mittels Copy-Button oder Strg-C) in die Kommandozeile (Strg-V oder rechte / mittlere Maustaste) und führen Sie ihn aus.

Der CSR liegt nun in der Datei mit der Endung '.csr' vor. Wechseln Sie nun in der Auswahlbox 'Erzeugungsmodus' auf 'CSR verfügbar' und laden Sie diese Datei hoch, um sie von der Zertifizierungsstelle bearbeiten zu lassen.

##### 'certreq -accept' Importieren des Zertifikats

Mit der Option '-accept' und dem Dateinamen des erzeugten Zertifikats wird das Zertifikat zum Schlüssel in den entsprechenden Windows-Store eingefügt und kann genutzt werden.

### Zertifikats-Anforderung

Wurde ein CSR hochgeladen oder eine serverseitige Schlüsselerzeugung angefordert, so befindet sich Ihre Anfrage in der internen Prüfung, im Erfolgsfall mit anschließender Erzeugung des angeforderten Zertifikats. Dieser Prozess kann automatisiert erfolgen oder eine manuelle Freigabe erfordern.
Entweder Sie werden sofort zum Zertifikats-Download weitergeleitet oder Sie werden durch eine eMail über die Zertifikatsaustellung informiert. Das Zertifikat wird in verschiedenen Formaten zum Download angeboten:

Konsultieren Sie die Dokumentation der Anwendung, die das Zertifikat nutzen soll. Befolgen Sie die dort angegeben Empfehlungen bzgl. Zertifikats-Format und -Dateinamen.
Ggf.muss das erzeugte Zertifikat auch in die oben per Kommandozeile erzeugte Container eingefügt werden.

#### <a id="ca3SApp.certificate.download.PKCS12"></a> PKCS12 Container

Falls Sie 'serverseitige Schlüsselerzeugung' ausgewählt haben, ist zusätzlich die Download-Option 'PKCS12-Container' vorhanden.

Ein PKCS12 Container enthält sowohl Zertifikate als auch private Schlüssel. Um Ihr erzeugtes Zertifikat auszuwählen, müssen Sie hier im Eingabefeld eine Bezeichnung ('Alias') eingeben. Abhängig von Ihrer Anwendung können Sie die Bezeichnung frei wählen oder müssen einen vordefinierten Wert angeben. Mit dem Link rechts können Sie den PKCS12 Container herunterladen. Der Dateiname muss ggf. an Applikationsvorgaben angepasst werden. Um auf den privaten Schlüssel innerhalb des Containers zugreifen zu können, müssen Sie das Passwort angeben, das Sie bei der Zertifikats-Anforderungen festgelegt haben. Es gibt keine Möglichkeit, das Passwort wieder anzuzeigen. Sollten Sie es vergessen haben, rufen Sie das aktuelle Zertifikat zurück und fordern Sie ein Neues an.

#### <a id="ca3SApp.certificate.download.PKIX"></a> Download eines Zertifikats im Binärformat

Download des angeforderten Zertifikats im Binärformat (aka PKIX / DER form). Schauen Sie in die Dokumentation ihrer Anwendung, ob dieses Format unterstützt wird.

#### <a id="ca3SApp.certificate.download.PEM"></a> Download eines Zertifikats im PEM-Format

Download des erzeugten Zertifikats im Textformat (PEM). Schauen Sie in die Dokumentation ihrer Anwendung, ob dieses Format unterstützt wird.

#### <a id="ca3SApp.certificate.download.revocationReason"></a> Rückrufgründe

Falls ein Zertifikat zurückgerufen werden muss, wählen Sie hier den passenden Grund.

#### <a id="ca3SApp.certificate.comment"></a> Rückrufkommentar

Geben Sie hier zusätzlich Informationen zum Zertifikatsrückruf an. Das kann nützlich für den RA Officer sein und / oder bei einer späteren Analyse helfen.
