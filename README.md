Build/development environment for java, zenta and python projects

The deliverable is a docker container containing an eclipse and scripts to aid the CI process
of projects conforming to Kode Konveyor pattern

# setup

copy over the following files to your project:

- `pmd_rules.xml`
- `shippable.yml`
- `testenv`

create a Makefile with the following content:

    export GITHUB_ORGANIZATION=<your github user- or organization name>
    export REPO_NAME=<the github project name>
    MODEL_BASENAME = <the basename of the zenta model>
    JAVA_TARGET = <the name of the java target created by mvn install>
    include /usr/local/toolchain/rules.java

set the `sonarkey` and `issuetoken` environment variables to your sonar and github oauth token.
probably you want to add them to your .bashrc

# usage

by running the testenv script you get into the build environment.

eclipse is at /opt/eclipse/eclipse

you can build everything by running `Script`

other convenience scripts are in the `/usr/local/toolchain/tools` directory in the environment
