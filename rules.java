#set MODEL_BASENAME to the basename of the zenta model
#set JAVA_TARGET to the filename of the file (jar or war in the target directory)

export TOOLCHAINDIR = /usr/local/toolchain

all: $(BEFORE_ALL) install

inputs/$(MODEL_BASENAME).issues.xml: shippable/$(MODEL_BASENAME)-implementedBehaviours.xml shippable/$(MODEL_BASENAME)-testcases.xml
	mkdir -p inputs
	$(TOOLCHAINDIR)/tools/getGithubIssues >inputs/$(MODEL_BASENAME).issues.xml


install: $(BEFORE_INSTALL) pomcheck compile sonar shippable
	cp -rf $(MODEL_BASENAME)/* target/* shippable

pomcheck:
	$(TOOLCHAINDIR)/tools/pomchecker

shippable:
	mkdir -p shippable

ifeq ($(IS_PULL_REQUEST),true)


sonar:

createdocs:
	$(TOOLCHAINDIR)/tools/noPullRequest

else #IS_PULL_REQUEST

sonar: $(BEFORE_SONAR) sonarconfig buildreports
	$(TOOLCHAINDIR)/tools/pullanalize

createdocs: $(MODEL_BASENAME).compiled codedocs
endif #IS_PULL_REQUEST

sonarconfig:
	cp $(TOOLCHAINDIR)/etc/m2/settings.xml ~/.m2

compile: $(BEFORE_COMPILE) zentaworkaround javabuild createdocs


codedocs: shippable/$(MODEL_BASENAME)-testcases.xml shippable/$(MODEL_BASENAME)-implementedBehaviours.xml shippable/$(MODEL_BASENAME)-implementedBehaviours.html shippable/bugpriorities.xml

shippable/$(MODEL_BASENAME)-testcases.xml: $(MODEL_BASENAME).richescape shippable
	zenta-xslt-runner -xsl:xslt/generate_test_cases.xslt -s $(MODEL_BASENAME).richescape outputbase=shippable/$(MODEL_BASENAME)-

shippable/$(MODEL_BASENAME)-implementedBehaviours.xml: buildreports shippable $(MODEL_BASENAME).rich
	zenta-xslt-runner -xsl:xslt/generate-behaviours.xslt -s target/test/javadoc.xml outputbase=shippable/$(MODEL_BASENAME)-

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
	JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn javadoc:javadoc javadoc:test-javadoc site

maven: target/$(JAVA_TARGET) javadoc

target/$(JAVA_TARGET): maven-prepare keystore maven-build

maven-prepare: $(BEFORE_MAVEN_PREPARE)
	mvn build-helper:parse-version versions:set versions:commit -DnewVersion=\$${parsedVersion.majorVersion}.\$${parsedVersion.minorVersion}.\$${parsedVersion.incrementalVersion}-$$(/usr/local/toolchain/tools/getbranch|sed 'sA/A_Ag').$$(git rev-parse --short HEAD)
	mvn clean 

maven-build: $(BEFORE_MAVEN_BUILD)
	mvn org.jacoco:jacoco-maven-plugin:prepare-agent install org.pitest:pitest-maven:mutationCoverage site -Pintegration-test

buildreports: $(BEFORE_BUILDREPORTS) maven
	zenta-xslt-runner -xsl:xslt/cpd2pmd.xslt -s:target/pmd.xml -o target/pmd_full.xml
	find ~/.m2/repository/org/slf4j/slf4j-api/ -regex .*slf4j-api-[0-9.]*.jar -exec ln -f -s {} /tmp/slf4j-api.jar \;
	find ~/.m2/repository/org/slf4j/slf4j-simple/ -regex .*slf4j-simple-[0-9.]*.jar -exec ln -f -s {} /tmp/slf4j-simple.jar \;
	java -cp /tmp/slf4j-api.jar:/tmp/slf4j-simple.jar:/usr/local/lib/mutation-analysis-plugin-1.3-SNAPSHOT.jar ch.devcon5.sonar.plugins.mutationanalysis.StandaloneAnalysis

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

