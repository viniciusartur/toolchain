Build/development environment for java, zenta and python projects

The deliverable is a docker container containing an eclipse and scripts to aid the CI process
of projects conforming to Kode Konveyor pattern

# setup

copy over the files from the `inproject` folder:

create a Makefile referencing rules.java. See https://github.com/edemo/PDEngine/blob/develop/Makefile for an example.

set the `issuetoken` environment variable to your github oauth token.
probably you want to add them to your .bashrc

# usage

by running the testenv script you get into the build environment.

eclipse is at /opt/eclipse/eclipse

you can build everything by running `Script`

other convenience scripts are in the `/usr/local/toolchain/tools` directory in the environment
