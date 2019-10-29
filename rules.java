#set MODEL_BASENAME to the basename of the zenta model
#set JAVA_TARGET to the filename of the file (jar or war in the target directory)

export TOOLCHAINDIR = /usr/local/toolchain

all: $(BEFORE_ALL) install

inputs/$(MODEL_BASENAME).issues.xml: shippable/$(MODEL_BASENAME)-implementedBehaviours.xml shippable/$(MODEL_BASENAME)-testcases.xml
	mkdir -p inputs
	$(TOOLCHAINDIR)/tools/getGithubIssues >inputs/$(MODEL_BASENAME).issues.xml


install: $(BEFORE_INSTALL) pomcheck compile checks shippable
	cp -rf $(MODEL_BASENAME)/* target/* shippable

pomcheck:
	$(TOOLCHAINDIR)/tools/pomchecker

checks: pmdcheck coveragecheck

cpdcheck: javadoc
	if grep -A 1 "<duplication" target/cpd.xml; then exit 1; fi

pmdcheck: javadoc
	if grep -A 1 "<violation" target/pmd.xml; then exit 1; fi

coveragecheck: target/coverage-report
	if grep ERROR target/coverage-report; then exit 1; fi

target/coverage-report: javadoc maven-build
	wget https://raw.githubusercontent.com/jacoco/jacoco/master/org.jacoco.report/src/org/jacoco/report/xml/report.dtd -O target/site/jacoco/report.dtd
	mutations=$$(ls $(PWD)/target/pit-reports/*/mutations.xml);zenta-xslt-runner -xsl:xslt/check_coverage.xslt -s:$$mutations -o:target/coverage-report

shippable:
	mkdir -p shippable

ifeq ($(IS_PULL_REQUEST),true)


createdocs:
	$(TOOLCHAINDIR)/tools/notCreatingDocumentationInPullRequest

else #IS_PULL_REQUEST

createdocs: $(MODEL_BASENAME).compiled codedocs checkdoc

checkdoc: $(MODEL_BASENAME).consistencycheck
	$(TOOLCHAINDIR)/tools/checkDocErrors
endif #IS_PULL_REQUEST

compile: $(BEFORE_COMPILE) zentaworkaround javabuild createdocs


codedocs: shippable/$(MODEL_BASENAME)-testcases.xml shippable/$(MODEL_BASENAME)-implementedBehaviours.xml shippable/$(MODEL_BASENAME)-implementedBehaviours.html shippable/bugpriorities.xml

shippable/$(MODEL_BASENAME)-testcases.xml: $(MODEL_BASENAME).richescape shippable
	zenta-xslt-runner -xsl:xslt/generate_test_cases.xslt -s $(MODEL_BASENAME).richescape outputbase=shippable/$(MODEL_BASENAME)-

shippable/$(MODEL_BASENAME)-implementedBehaviours.xml: buildreports shippable $(MODEL_BASENAME).rich
	zenta-xslt-runner -xsl:xslt/generate-behaviours.xslt -s target/test/javadoc.xml outputbase=shippable/$(MODEL_BASENAME)- modelbasename=$(MODEL_BASENAME)

CONSISTENCY_INPUTS=shippable/$(MODEL_BASENAME)-testcases.xml shippable/$(MODEL_BASENAME)-implementedBehaviours.xml

include /usr/share/zenta-tools/model.rules

$(MODEL_BASENAME).consistencycheck: $(MODEL_BASENAME).rich $(MODEL_BASENAME).check $(CONSISTENCY_INPUTS)
	zenta-xslt-runner -xsl:xslt/consistencycheck.xslt -s:$(basename $@).check -o:$@ >$(basename $@).consistency.stderr 2>&1
	sed 's/\//:/' <$(basename $@).consistency.stderr |sort --field-separator=':' --key=2

testenv:
	$(TOOLCHAINDIR)/tools/testenv

javabuild: $(BEFORE_JAVABUILD) maven buildreports
	touch javabuild


javadoc: $(BEFORE_JAVADOC)
	mkdir -p target/production target/test
	JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn -B javadoc:javadoc javadoc:test-javadoc site

maven: target/$(JAVA_TARGET) javadoc

target/$(JAVA_TARGET): maven-prepare keystore maven-build

maven-prepare: $(BEFORE_MAVEN_PREPARE)
	mvn -B build-helper:parse-version versions:set versions:commit -DnewVersion=\$${parsedVersion.majorVersion}.\$${parsedVersion.minorVersion}.\$${parsedVersion.incrementalVersion}-$$(/usr/local/toolchain/tools/getbranch|sed 'sA/A_Ag').$$(git rev-parse --short HEAD)
	mvn -B clean 

maven-build: $(BEFORE_MAVEN_BUILD)
	mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent install org.pitest:pitest-maven:mutationCoverage site -Pintegration-test

buildreports: $(BEFORE_BUILDREPORTS) maven
	zenta-xslt-runner -xsl:xslt/cpd2pmd.xslt -s:target/pmd.xml -o target/pmd_full.xml

clean: $(BEFORE_CLEAN)
	git clean -fdx
	rm -rf zenta-tools xml-doclet


zentaworkaround:
	mkdir -p ~/.zenta/.metadata/.plugins/org.eclipse.e4.workbench/
	cp $(TOOLCHAINDIR)/etc/workbench.xmi ~/.zenta/.metadata/.plugins/org.eclipse.e4.workbench/
	touch zentaworkaround

shippable/bugpriorities.xml: $(MODEL_BASENAME).consistencycheck inputs/$(MODEL_BASENAME).issues.xml $(MODEL_BASENAME).richescape shippable
	zenta-xslt-runner -xsl:xslt/issue-priorities.xslt -s:$(MODEL_BASENAME).consistencycheck -o:shippable/bugpriorities.xml issuesfile=inputs/$(MODEL_BASENAME).issues.xml modelfile=$(MODEL_BASENAME).richescape missingissuefile=shippable/missing.xml

keystore:
	$(TOOLCHAINDIR)/tools/generate_keystore

