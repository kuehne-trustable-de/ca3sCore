# ca3s

## Mission

ca3s is a CA support system with a flexible RA part using BPM aiming to automate as much as possible.
Therefore providing ACME and SCEP interfaces in addition to the usual web form. Aggregating certificate sets
from different sources and using CMP-connected CAs or ADCS instances for certificate creation.</span>

### The feature list

- Manage all your CA instances (CMP and ADCS)

- Keep track of expiration of all your relevant certificates from all sources

- Analyze the key algorithms, key length, hash and padding algorithms in use

- Offer a convenient web interface for the requestors and the RA officers

- Use of the <a href="https://badkeys.info/">badkeys</a> project (if installed locally) to check keys for known weaknesses

And most important for a reliable PKI infrastructure:

- Automate issuance and renewal as far as possible

- Use BPMN to define organization specific rules

- Offer well established interfaces (ACME and SCEP) for easy automation

The project is open sourced under <a href="https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12" target="_blank">EUPL</a> and can be found at <a href="https://github.com/kuehne-trustable-de/ca3sCore" target="_blank">github</a>.

Please file bug reports, questions and feature proposals at github's <a href="https://github.com/kuehne-trustable-de/ca3sCore/issues" target="_blank">issue tracker</a>.
For professional support please contact <a href="mailto:info@trustable.de">trustable's support</a>.

## Installation

For a quick start there are ready-build package available at [maven central](https://mvnrepository.com/artifact/de.trustable.ca3s.core/ca-3-s). Just download the latest version.
Install a recent version of the Java runtime (Version >= 11) and run

    java -jar .\target\ca-3-s-{version}.jar

This command starts a ca3s instance with a local h2 database and it's web interface available at http://localhost:8080

You can login with e.g. user/user or admin/admin to get a first impression.

### Configuration

For a more useful setup you need to setup a database (or use a schema in an existing database) and insert the corresponding setting in a configuration file. A convenient way is to make a copy of
/src/main/resources/config/application-prod.xml to your application directory. Check the settings in this configuration file and adapt it where required (e.g. the database settings).

    java -jar .\target\ca-3-s-{version}.jar --spring.config.location=file://{path}/application-prod.xml

With a database configuration ca3s will create the required tables (thanks to liquibase).

## Development

This application was generated using JHipster 6.5.1, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v6.5.1](https://www.jhipster.tech/documentation-archive/v6.5.1).

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

    npm install

We use npm scripts and [Webpack][] as our build system.

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    ./mvnw
    npm start

Npm is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `npm update` and `npm install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `npm help update`.

The `npm run` command will list all of the scripts available to run for this project.

### Using vue-cli

You can also use [Vue CLI][] to display the project using vue UI.

For example, the following command:

    vue ui

will generate open Vue Project Manager. From there, you'll be able to manage your project as any other Vue.js projects.

## Building for production

### Packaging as jar

To build the final jar and optimize the ca3s application for production, run:

    ./mvnw -Pprod clean verify

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar target/*.jar

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

    ./mvnw -Pprod,war clean verify

## Testing

To launch your application's tests, run:
./mvnw verify

### Client tests

Unit tests are run by [Jest][] and written with [Jasmine][]. They're located in [src/test/javascript/](src/test/javascript/) and can be run with:
npm test

For more information, refer to the [Running tests page][].

### Code quality

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker-compose -f src/main/docker/sonar.yml up -d
```

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the maven plugin.
Then, run a Sonar analysis:

```
./mvnw -Pprod clean verify sonar:sonar
```

If you need to re-run the Sonar phase, please be sure to specify at least the `initialize` phase since Sonar properties are loaded from the sonar-project.properties file.

```
./mvnw initialize sonar:sonar
```

or
For more information, refer to the [Code quality page][].

## Tooling

We are using IntelliJ IDEA. Many thanks to [JetBrain's open source support](https://www.jetbrains.com/community/opensource/#support) !

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 6.5.1 archive]: https://www.jhipster.tech/documentation-archive/v6.5.1
[using jhipster in development]: https://www.jhipster.tech/documentation-archive/v6.5.1/development/
[using docker and docker-compose]: https://www.jhipster.tech/documentation-archive/v6.5.1/docker-compose
[using jhipster in production]: https://www.jhipster.tech/documentation-archive/v6.5.1/production/
[running tests page]: https://www.jhipster.tech/documentation-archive/v6.5.1/running-tests/
[code quality page]: https://www.jhipster.tech/documentation-archive/v6.5.1/code-quality/
[setting up continuous integration]: https://www.jhipster.tech/documentation-archive/v6.5.1/setting-up-ci/
[node.js]: https://nodejs.org/
[yarn]: https://yarnpkg.org/
[webpack]: https://webpack.github.io/
[vue cli]: https://cli.vuejs.org/
[browsersync]: https://www.browsersync.io/
[jest]: https://facebook.github.io/jest/
[jasmine]: https://jasmine.github.io/2.0/introduction.html
[protractor]: https://www.protractortest.org/
[leaflet]: https://leafletjs.com/
[definitelytyped]: https://definitelytyped.org/
