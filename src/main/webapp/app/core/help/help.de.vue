<template>
    <div>
        <h1 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.help.title')">Help</span>
        </h1>

        <h2><b>C</b>ertificate <b>A</b>utomation, <b>A</b>uthorization and <b>A</b>dministration <b>S</b>ervice (<i>ca3s</i>)</h2>
        <div>
            ca3s ist ein CA Unterstützungssystem mit einem flexiblen RA Teil, welcher BPM benutzt, mit dem Ziel so viel wie möglich zu automatisieren. Zu diesem Zweck, werden zusätzlich zur normalen Web-Variante, ACME und SCEP Schnittstellen bereitgestellt. Außerdem werden Zertifikatsets aus verschiedenen Quellen aggregiert und es werden CMP-verbundene CAs oder ADCS Instanzen für die zertifikat Erstellung benutzt.
            <ul>
                <li>
                    Verwalte alle deine CA Instanzen (CMP und ADCS)
                </li>
                <li>
                    Behalte den Überblick über die Ablaufdaten aller relevanten Zertifikate aus allen Quellen.
                </li>
                <li>
                    Analysiere die Schlüssel-Algorithmen, Schlüssellänge, Hash and Padding-Algorithmen in Nutzung
                </li>
                <li>
                    Ein nutzerfreundliches Webinterface für Antragsteller und RA-Officers.
                </li>
            </ul>

            Am wichtigsten aber für eine zuverlässige PKI Infrastruktur:

            <ul>
                <li>
                    Weitegehende Automatisierung des Ausstellens und Erneuerns
                </li>
                <li>
                    Die Nutzung von BPMN um organisationsspezifische Regeln aufzustellen
                </li>
                <li>
                    Bietet gutetablierte Schnittstellen an (ACME und SCEP) für eine erleichterte Automatisierung
                </li>
            </ul>

            Das Projekt ist open-source <a href="https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12">EUPL</a> und kann bei <a href="https://github.com/kuehne-trustable-de/ca3sCore">github</a> gefunden werden.
        </div>
        <p/>

        <h3>Building Bocks</h3>
        <div>
            Die folgende Abbildung gibt einen groben Überblick über die ca3s Architektur. Oben sind die verschiedenen Typen von Clients, die sich mit dem ca3s-Server verbinden, abgebildet. Unten sind die externen Dienste, mit denen sich ca3s vebindet, gezeigt.
            <p/>
            <img class="box" src="../../../content/images/birds_view.jpg" />
        </div>
        <p/>

        <h4>Anfragen Adapter (Oberer Bereich)</h4>
        <div>
            Zertifikatsanfragen können gemäß ACME- und SCEP-Protokoll and mittels einer Web-user Schnittstelle übermittelt werden. Zusätzliche Protokolle können einfach hinzugefügt werden.
        </div>
        <p/>
        <h4>CA Adapter (Unterer bereich)</h4>
        <div>
            Verschiedene CA-Instanzen können mit ca3s verbunden werden. Ein weit verbreitetes Protokoll für CAs ist CMP, unterstützt von z.B. ejbca und vielen Weiteren. Zusätzlich dazu existiert ein seperates Projekt (adcsCert) um sich mit dem Microsoft ADCS CA zu verbinden.
        </div>
        <p/>
        <h4>Zertifikat Datenbank</h4>
        <div>
            Ein zentraler Zertifikatspeicher enthält ausgegebene und anderweitig abgerufene Zertifikate, um so eine zentrale Anlaufstelle für den Zertifikatstatus und die Berichterstellung, z. B. Ablaufberichte, zu erhalten.
        </div>
        <p/>
        <h4>Ausstellungsworkflow</h4>
        <div>
            Abhängig von der Konfiguration können Zertifikatsanforderungen von einem menschlichen Registrierungsbeauftragten zugelassen werden. Ergänzende automatisierte Workflows können so konfiguriert werden, dass der Registrierungsbeauftragte von ermüdenden Aufgaben entlastet wird und selbst komplexe Prozesse automatisiert ausgeführt werden. Die Prozesse können in BPMN definiert und bestimmten Verarbeitungspipelines flexibel zugewiesen werden.
        </div>
        <p/>


        <h2>Erklärungen der Optionen</h2>

        <h3 class="jh-entity-heading">
            <span id="pkcsxx.upload.pipeline" v-text="$t('pkcsxx.upload.pipeline')">Processing pipeline</span>
        </h3>

        <div>
            Zertifikate können für eine Vielzahl von Anwendungsfällen ausgestellt werden. Diese Anwendungsfälle erfordern möglicherweise unterschiedliche ausstellende Zertifizierungsstellen, unterschiedliche Vertrauensstufen, unterschiedliche Verwendungen und Gültigkeiten sowie Ausstellungsanforderungen.
            Im folgenden Auswahlfeld werden verschiedene Verarbeitungspipelines angeboten. Zusätzliche Informationen werden möglicherweise unter der Auswahl angezeigt. Bitte stellen Sie sicher, dass Sie den richtigen Artikel für Ihre Bedürfnisse auswählen. Eine falsche Pipeline führt zur Ablehnung Ihrer Anfrage.
        </div>
        <p/>

        <h3 class="jh-entity-heading">
            <span id="pkcsxx.upload.creationMode.selection" v-text="$t('pkcsxx.upload.creationMode.selection')">Creation mode</span>
        </h3>

        <div>
            <ul>
                <li v-text="$t('pkcsxx.upload.creationMode.csrAvailable')">CSR available</li>
                Die einfachste Methode zur Zertifikatsanforderungsprozesses besteht darin, eine CSR hochzuladen. Wählen Sie diese Option, wenn die CSR bereits verfügbar ist, und fügen Sie sie in den Textbereich unten ein. Alternativ können Sie auf Ihrem Computer nach einer CSR-Datei suchen.
                <li v-text="$t('pkcsxx.upload.creationMode.serversideKeyCreation')">Serverside key creation</li>
                Wenn Ihre Sicherheitsrichtlinie diese Option zulässt, können Sie festlegen, dass der Server ein Schlüsselpaar für Sie erstellt. Sie können einen Schlüsseltyp und eine Schlüssellänge auswählen und die Details des Betreffs des Zertifikats definieren.<br><br>
                Der Server erstellt einen Schlüssel- und Zertifikatcontainer, der Ihren privaten Schlüssel schützt. Um Zugriff auf den privaten Schlüssel zu erhalten, müssen Sie ein Passwort für den Container angeben. <br><br>
                Bewahren Sie dieses Passwort an einem sicheren Ort auf! Wenn das Passwort verloren geht, ist der geheime Schlüssel nicht mehr verfügbar. Es gibt keine Möglichkeit, das Passwort vom ca3s-Server abzurufen. <br><br>
                Eine Kompromittierung des Passwortses wirkt sich auf den privaten Schlüssel und das Zertifikat aus! Verwenden Sie keinen kompromittierten Schlüssel. Widerrufen Sie das Zertifikat in diesem Fall sofort!<br><br>
                <b>Es wird strengstens empfohlen, CSRs oder ein automatisiertes Zertifikatverwaltungsprotokoll zu verwenden, z.B. ACME!</b>
                <li v-text="$t('pkcsxx.upload.creationMode.commandLineTool')">command line tool</li>
                Mit der Befehlszeilenoption können Sie die verschiedenen verfügbaren Tools für die CSR-Erstellung je nach Betriebssystem und Zielanwendung verwenden.
            </ul>
        </div>
        <p/>

        <h3 class="jh-entity-heading">
            <span id="pkcsxx.upload.creationTool.selection" v-text="$t('pkcsxx.upload.creationTool.selection')">Processing pipeline</span>
        </h3>

        <div>
            <ul>
                <li>Java keytool</li>
                Das Schlüssel- und Zertifikatverwaltungstool von Java ist in der Java-Laufzeitverteilung unter Windows und Linux enthalten.
                <li>OpenSSL > 1.1.0</li>
                Eine relativ neue Version des weit verbreiteten OpenSSL-Tools. Wenn Sie sich nicht sicher sind, überprüfen Sie Ihre Version mit 'openssl version'.
                <li>OpenSSL</li>
                Eine Befehlszeilenskriptversion, die mit allen OpenSSL-Tools kompatibel ist.
            </ul>
        </div>
        <p/>



        <h3 class="jh-entity-heading">
            <span id="ca3SApp.help.subtitle.commandline">CSR command line creation</span>
        </h3>
        <div>
            Unter Sicherheitsgesichtspunkten empfiehlt es sich, das Schlüsselpaar auf dem Zielsystem (z. B. einem Webserver) zu erstellen und eine Zertifikatsignierungsanforderung (csr) zu erstellen. Eine csr enthält öffentliche Informationen (z. B. den Domänennamen des Webservers) und den öffentlichen Schlüssel.
            Der private Schlüssel verlässt das Zielsystem nicht, wenn das Zertifikat von der Zertifizierungsstelle angefordert wird.

            Um eine CSR zu erstellen, öffnen Sie bitte eine Eingabeaufforderung in Ihrem System:
            <ul>
                <li>Windows: Command Prompt ('cmd.exe') or PowerShell</li>
                <li>Linux: Shell</li>
            </ul>
            Wechseln Sie in das Verzeichnis, in dem der Schlüssel erstellt werden soll. In der Produktdokumentation Ihrer Anwendung finden Sie das entsprechende Verzeichnis.
            Füllen Sie die erforderlichen Felder im ca3s-Webformular aus (Betreff, Organisation, SAN, ...).<br><br>
            Überprüfen Sie anhand Ihrer Dokumentation, welche Art von "store" erwartet wird. Java-Anwendungen erwarten normalerweise einen PKCS12-Keystore. Apache-Webserver verarbeiten möglicherweise separate PEM-Dateien.<br><br>

            Mit Hilfe 'Werkzeugauswahl' wählen Sie zwischen den verschiedenen Werkzeuge aus:
            <ul>
                <li>Java keytool</li>
                Für das Keytool von Java sind zwei Befehle erforderlich, um den Schlüssel und die CSR zu erstellen.
                <li>OpenSSL > 1.1.0</li>
                Diese aktuelle Version erstellt alle erforderlichen Dateien in einem Befehl.
                <li>OpenSSL</li>
                Die alten Versionen von OpenSSL erfordern eine Konfigurationsdatei und einen Shell-Befehl.
            </ul>

            Kopieren Sie den generierten Wert aus dem Textfeld in Ihre Eingabeshell.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('pkcsxx.upload.creationTool.cmdline')" id="pkcsxx.upload.creationTool.cmdline">CSR command line 'Java keytool'</span>
        </h4>
        <div>
            Die generierte Java-Keytool-Befehlszeile besteht aus zwei Teilen: <br><br>
            Der erste Befehl generiert ein neues Schlüsselpaar mit der ausgewählten Schlüssellänge in einem PKCS12-Schlüsselspeicher ('test.p12') mit dem Alias 'keyAlias'.
            <p/>
            <img class="box" src="../../../content/images/birds_view.jpg" />

            Informationen zum erforderlichen Keystore-Dateinamen und zum erwarteten Alias finden Sie in Ihrer Anwendungsdokumentation. Stellen Sie sicher, dass vorhandene Dateien nicht überschrieben werden.<br><br>
            Der zweite Befehl erstellt eine CSR (Certificate Signing Request). Diese Datei (im generierten Befehl 'server.csr' genannt) wird auf die Zertifizierungsstelle hochgeladen.<br><br>
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.help.subtitle.request.conf')" id="pkcsxx.upload.creationTool.req.conf">OpenSSL request config</span>
        </h4>
        <div>Öffnen Sie einen Texteditor auf Ihrem System.
            Kopieren Sie den generierten Wert aus dem Textfeld in Ihren Editor.
            Speichern Sie die Datei in dem Verzeichnis, in dem der Schlüssel erstellt werden soll.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.download.PKCS12')" id="ca3SApp.certificate.download.PKCS12">Download eines PKCS12 Containers</span>
        </h4>
        <div>Ein PKCS12 Container enthält sowohl Zertifikate als auch private Schlüssel. Um Ihr erzeugtes Zertifikat auszuwählen,
            müssen Sie hier im Eingabefeld eine Bezeichnung ('Alias') eingeben. Abhängig von Ihrer Anwendung können Sie die Bezeichnung
            frei wählen oder müssen einen vordefinierten Wert angeben

            Mit dem Link rechts können Sie den PKCS12 Container herunterladen. Der Dateiname muss ggf. an Applikationsvorhgaben angepasst werden.
            Um auf den privaten Schlüssel innerhalb des Containers zugeifen zu können, müssen Sie das Passwort angeben, das Sie bei der
            Zertifikats-Anforderungen festgelegt haben. Es gibt keine Möglichkeit, das Passwort wieder anzuzeigen. Sollten Sie es vergessen haben,
            rufen Sie das aktuelle Zertifikat zurück und fordern Sie ein Neues an.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.download.PKIX')" id="ca3SApp.certificate.download.PKIX">Download eines Zertifikats im Binärformat (PKIX)</span>
        </h4>
        <div>Download des angeforderten Zertifikats im Binärformat (aka PKIX / DER form).
            Schauen Sie in die Dokumentation ihrer Anwendung, ob dieses Format unterstützt wird.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.download.PEM')" id="ca3SApp.certificate.download.PEM">Download eines Zertifikats im Textformat (PEM)</span>
        </h4>
        <div>Download des erzeugten Zertifikats im Textformat (PEM).
            Schauen Sie in die Dokumentation ihrer Anwendung, ob dieses Format unterstützt wird.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.revocationReason')" id="ca3SApp.certificate.download.revocationReason">Auswahl eines Rückrufgrundes</span>
        </h4>
        <div>Falls diese Zertifikat zurückgerufen werden muss, wählen Sie hier den passenden Grund.
        </div>
        <p/>

        <h4 class="jh-entity-heading">
            <span v-text="$t('ca3SApp.certificate.comment')" id="ca3SApp.certificate.comment">Provide a commment regarding the revocation (optional)</span>
        </h4>
        <div>Geben Sie hier zusätzlich Informationen zum Zertifikatsrückruf an. Das kann nützlich für den RA Officer sein und / oder bei eines späteren Analyse helfen.
        </div>
        <p/>

    </div>
</template>

<script lang="ts" src="./help.component.ts">
</script>

<style src="./help.css">
</style>
