# Hilfe

## **C**ertificate **A**utomation, **A**uthorization and **A**dministration **S**ervice (_ca3s_)

ca3s ist ein CA Unterstützungssystem mit einem flexiblen RA Teil, welcher BPM benutzt, mit dem Ziel so viel wie möglich zu automatisieren. Zu diesem Zweck, werden zusätzlich zur normalen Web-Variante, ACME und SCEP Schnittstellen bereitgestellt. Außerdem werden Zertifikatsbestände aus verschiedenen Quellen aggregiert und es werden CMP-angebundene CAs oder ADCS-Instanzen für die Zertifikatserstellung benutzt.

- Verwaltet verschiedene CA Instanzen (CMP und ADCS)
- Erlaubt den Überblick über die Ablaufdaten aller relevanten Zertifikate aus allen Quellen.
- Analysiere die Schlüssel-Algorithmen, Schlüssellänge, Hash and Padding-Algorithmen in Nutzung
- Ein nutzerfreundliches Webinterface für Antragsteller und Registrierungsbeauftragten (auch als 'RA-Officer' bezeichnet).

Die wichtigsten Punkte für eine zuverlässige PKI Infrastruktur:

- Weitgehende Automatisierung des Ausstellens und Erneuerns
- Die Nutzung von BPMN um organisations-spezifische Regeln aufzustellen
- Bietet gut etablierte Schnittstellen an (ACME und SCEP) für eine erleichterte Automatisierung

Das Projekt steht unter einer Open-Source-Lizenz [EUPL](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12) und kann bei [github](https://github.com/kuehne-trustable-de/ca3sCore) heruntergeladen werden.

### Building Bocks

Die folgende Abbildung gibt einen groben Überblick über die ca3s Architektur. Oben sind die verschiedenen Typen von Clients, die sich mit dem ca3s-Server verbinden, abgebildet. Unten sind die externen Dienste, mit denen sich ca3s verbindet, gezeigt.

![Architekturübersicht](../../images/birds_view.jpg)

Architekturübersicht

#### Anfragen Adapter (Oberer Bereich)

Zertifikatsanfragen können gemäß ACME- und SCEP-Protokoll and mittels einer Web-user Schnittstelle übermittelt werden. Zusätzliche Protokolle können einfach hinzugefügt werden.

#### CA Adapter (Unterer bereich)

Verschiedene CA-Instanzen können mit ca3s verbunden werden. Ein weit verbreitetes Protokoll für CAs ist CMP, unterstützt von z.B. ejbca und vielen Weiteren. Zusätzlich dazu existiert ein separates Projekt (adcsCert) um sich mit dem Microsoft ADCS CA zu verbinden.

#### Zertifikatsdatenbank

Ein zentraler Zertifikatsspeicher enthält ausgegebene und anderweitig abgerufene Zertifikate, um so eine zentrale Anlaufstelle für den Zertifikatsstatus und die Berichterstellung, z.B. für eine Ablaufübersicht, zu erhalten.

#### Ausstellungsworkflow

Abhängig von der Konfiguration können Zertifikatsanforderungen von einem menschlichen Registrierungsbeauftragten zugelassen werden. Ergänzende automatisierte Workflows können so konfiguriert werden, dass der Registrierungsbeauftragte von ermüdenden Aufgaben entlastet wird und selbst komplexe Prozesse automatisiert ausgeführt werden. Die Prozesse können in BPMN definiert und bestimmten Verarbeitungspipelines flexibel zugewiesen werden.

## Erklärungen der Optionen

### Processing pipeline

Zertifikate können für eine Vielzahl von Anwendungsfällen ausgestellt werden. Diese Anwendungsfälle erfordern möglicherweise unterschiedliche ausstellende Zertifizierungsstellen, unterschiedliche Vertrauensstufen, unterschiedliche Verwendung und Gültigkeit sowie Ausstellungsanforderungen. Im folgenden Auswahlfeld werden verschiedene Verarbeitungspipelines angeboten. Zusätzliche Informationen werden möglicherweise unter der Auswahl angezeigt. Bitte stellen Sie sicher, dass Sie den richtigen Artikel für Ihre Bedürfnisse auswählen. Eine falsche Pipeline führt zur Ablehnung Ihrer Anfrage.

### Creation mode

- CSR available
  Die einfachste Methode des Zertifikatsanforderungsprozesses besteht darin, eine CSR hochzuladen. Wählen Sie diese Option, wenn bereits ein CSR verfügbar ist, und fügen Sie ihn in den Textbereich unten ein. Alternativ können Sie auf Ihrem Computer nach einer CSR-Datei suchen.
- Serverside key creation
  Wenn Ihre Sicherheitsrichtlinie diese Option zulässt, können Sie festlegen, dass der Server ein Schlüsselpaar für Sie erstellt. Sie können einen Schlüsseltyp und eine Schlüssellänge auswählen und die Details des Betreffs des Zertifikats definieren.

Der Server erstellt einen Schlüssel- und Zertifikats-Container, der Ihren privaten Schlüssel schützt. Um Zugriff auf den privaten Schlüssel zu erhalten, müssen Sie ein Passwort für den Container angeben.

Bewahren Sie dieses Passwort an einem sicheren Ort auf! Wenn das Passwort verloren geht, ist der geheime Schlüssel nicht mehr verfügbar. Es gibt keine Möglichkeit, das Passwort vom ca3s-Server abzurufen.

Eine Kompromittierung des Passworts wirkt sich auf den privaten Schlüssel und das Zertifikat aus! Verwenden Sie keinen kompromittierten Schlüssel. Widerrufen Sie das Zertifikat in diesem Fall sofort!

**Es wird strengstens empfohlen, CSRs oder ein automatisiertes Zertifikatsverwaltungsprotokoll zu verwenden, z.B. ACME! \*** CSR generation command lines
Mit der Befehlszeilenoption können Sie die verschiedenen verfügbaren Tools für die CSR-Erstellung je nach Betriebssystem und Zielanwendung verwenden.

### Tool selection

- Java keytool
  Das Schlüssel- und Zertifikatsverwaltungstool von Java ist in der Java-Laufzeitverteilung unter Windows und Linux enthalten.
- OpenSSL > 1.1.0
  Eine relativ neue Version des weit verbreiteten OpenSSL-Tools. Wenn Sie sich nicht sicher sind, überprüfen Sie Ihre Version mit 'openssl version'.
- OpenSSL
  Eine Befehlszeilen-Skript-Version, die mit allen OpenSSL-Tools kompatibel ist.

### CSR command line creation

Unter Sicherheitsgesichtspunkten empfiehlt es sich, das Schlüsselpaar auf dem Zielsystem (z. B. einem Webserver) zu erstellen und eine Zertifikats-Signierungs-Anforderung (certificate signing request, abgekürzt CSR) zu erstellen. Ein CSR enthält öffentliche Informationen (z.B. den Domänennamen des Webservers) und den öffentlichen Schlüssel. Der private Schlüssel verlässt das Zielsystem nicht, wenn das Zertifikat von der Zertifizierungsstelle angefordert wird. Um eine CSR zu erstellen, öffnen Sie bitte eine Eingabeaufforderung in Ihrem System:

- Windows: Command Prompt ('cmd.exe') or PowerShell
- Linux: Shell

Wechseln Sie in das Verzeichnis, in dem der Schlüssel erstellt werden soll. In der Produktdokumentation Ihrer Anwendung finden Sie das entsprechende Verzeichnis. Füllen Sie die erforderlichen Felder im ca3s-Webformular aus (Betreff, Organisation, SAN, ...).

![Zertifikatsparameter](../../images/sslnew4.png)

Zertifikatsparameter

Überprüfen Sie anhand Ihrer Dokumentation, welche Art von "store" erwartet wird. Java-Anwendungen erwarten normalerweise einen PKCS12-Keystore. Apache-Webserver verarbeiten möglicherweise separate PEM-Dateien.

Mit Hilfe 'Werkzeugauswahl' wählen Sie zwischen den verschiedenen Werkzeuge aus:

- Java keytool
  Für das Keytool von Java sind zwei Befehle erforderlich, um den Schlüssel und die CSR zu erstellen.
- OpenSSL > 1.1.0
  Diese aktuelle Version erstellt alle erforderlichen Dateien in einem Befehl.
- OpenSSL
  Die alten Versionen von OpenSSL erfordern eine Konfigurationsdatei und einen Shell-Befehl.

Kopieren Sie den generierten Wert aus dem Textfeld in Ihre Kommandozeile:

#### Linux

![Zertifikatsparameter](../../images/java4shell.png)

Zertifikatsparameter

#### Windows

![Zertifikatsparameter](../../images/sslold3.png)

Zertifikatsparameter

#### Kopieren

Die Felder mit den generierten Kommandozeilen haben einen 'Copy'-Button. Alternativ kann der Text selektiert und kopiert werden.

![Copy-Button](../../images/stop_sign.png)

Copy-Button

#### Command line

Die generierte Java-Keytool-Befehlszeile besteht aus zwei Teilen:

Der erste Befehl generiert ein neues Schlüsselpaar mit der ausgewählten Schlüssellänge in einem PKCS12-Schlüsselspeicher ('test.p12') mit dem Alias 'keyAlias'.

![](../../images/stop_sign.png) Informationen zum erforderlichen Keystore-Dateinamen und zum erwarteten Alias finden Sie in Ihrer Anwendungsdokumentation. Stellen Sie sicher, dass vorhandene Dateien nicht überschrieben werden.

Der zweite Befehl erstellt eine CSR (Certificate Signing Request). Diese Datei (im generierten Befehl 'server.csr' genannt) wird auf die Zertifizierungsstelle hochgeladen.

![](../../images/stop_sign.png)

#### OpenSSL request configuration file

Öffnen Sie einen Texteditor auf Ihrem System. Kopieren Sie den generierten Wert aus dem Textfeld in Ihren Editor. Speichern Sie die Datei in dem Verzeichnis, in dem der Schlüssel erstellt werden soll.

![](../../images/stop_sign.png)

#### PKCS12 Container

Ein PKCS12 Container enthält sowohl Zertifikate als auch private Schlüssel. Um Ihr erzeugtes Zertifikat auszuwählen, müssen Sie hier im Eingabefeld eine Bezeichnung ('Alias') eingeben. Abhängig von Ihrer Anwendung können Sie die Bezeichnung frei wählen oder müssen einen vordefinierten Wert angeben. Mit dem Link rechts können Sie den PKCS12 Container herunterladen. Der Dateiname muss ggf. an Applikationsvorgaben angepasst werden. Um auf den privaten Schlüssel innerhalb des Containers zugreifen zu können, müssen Sie das Passwort angeben, das Sie bei der Zertifikats-Anforderungen festgelegt haben. Es gibt keine Möglichkeit, das Passwort wieder anzuzeigen. Sollten Sie es vergessen haben, rufen Sie das aktuelle Zertifikat zurück und fordern Sie ein Neues an.

#### Download certificate as Binary

Download des angeforderten Zertifikats im Binärformat (aka PKIX / DER form). Schauen Sie in die Dokumentation ihrer Anwendung, ob dieses Format unterstützt wird.

#### Download certificate as PEM

Download des erzeugten Zertifikats im Textformat (PEM). Schauen Sie in die Dokumentation ihrer Anwendung, ob dieses Format unterstützt wird.

#### Revocation Reason

Falls dieses Zertifikat zurückgerufen werden muss, wählen Sie hier den passenden Grund.

#### Certificate Comment

Geben Sie hier zusätzlich Informationen zum Zertifikatsrückruf an. Das kann nützlich für den RA Officer sein und / oder bei einer späteren Analyse helfen.
