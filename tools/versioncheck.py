#!/usr/bin/python3

import subprocess
import re
from packaging import version
import sys
import os

class VersionChecker:
    def __init__(self):
        self.versions = {}
        modelBasename=os.environ['MODEL_BASENAME']
        toolchainDir=os.environ['TOOLCHAINDIR']
        self.cmdLine = "zenta-xslt-runner -xsl:{1}/tools/versions.xslt -s:$(pwd)/{0}.zenta".format(modelBasename,toolchainDir)
        self.extractVersions()

    def extractVersions(self):
        with open('shippable.yml') as shippableFile:
            shippable = shippableFile.read()
        imageVersion=re.sub('^build_image:[^:]*:(.*)$[\s\S]*','\\1',shippable,flags=re.MULTILINE)
        self.versions['imageVersion'] = imageVersion
        modelVersions = subprocess.getoutput(self.cmdLine)
        for line in modelVersions.split('\n'):
            splat = line.split("=")
            if len(splat)==2:
                self.versions[splat[0]] = splat[1]

    def checkVersion(self, actualVersion, minimumVersion):
        actualVersionParsed = version.parse(self.versions[actualVersion])
        minimumVersionParsed = version.parse(self.versions[minimumVersion])
        if actualVersionParsed < minimumVersionParsed:
            print("{0}({2}) is less than {1}({3})".format('imageVersion','minimumToolchainVersion',actualVersionParsed,minimumVersionParsed))
            sys.exit(-1)

checker = VersionChecker()
print(checker.versions)
checker.checkVersion('modelVersion', 'failBelow')
checker.checkVersion('imageVersion', 'minimumToolchainVersion')
