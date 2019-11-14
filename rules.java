export TOOLCHAINDIR = /usr/local/toolchain


all: $(BEFORE_ALL) install shippable/behaviours.xml $(AFTER_ALL)

install: $(BEFORE_INSTALL) target/pomcheck.ok shippable/.dir compile checks target/install.ok $(AFTER_INSTALL)

compile: target/.dir $(BEFORE_COMPILE) target/zentaworkaround.ok target/product.war createdocs $(AFTER_COMPILE)

checks: $(BEFORE_CHECKS) checkdoc pmdcheck coveragecheck $(AFTER_CHECKS)

createdocs: $(BEFORE_CREATEDOCS) $(MODEL_BASENAME).compiled codedocs $(AFTER_CREATEDOCS)

clean: $(BEFORE_CLEAN)
	if git clean -ndx|grep "^Would remove src"; then echo  "\n\n----------  WARNING! ---------------\nnot deleting anything: please remove or add the above by hand\n\n";exit 1; fi
	git clean -fdx

checkdoc: $(MODEL_BASENAME).consistencycheck
	checkDocErrors

codedocs: shippable/implementedBehaviours.html

cpdcheck: target/cpd.xml
	if grep -A 1 "<duplication" target/cpd.xml; then exit 1; fi

pmdcheck: target/pmd.xml
	if grep -A 1 "<violation" target/pmd.xml; then exit 1; fi

coveragecheck: target/coverage-report
	if grep ERROR target/coverage-report; then exit 1; fi

shippable/behaviours.xml shippable/behaviours.txt: $(MODEL_BASENAME).rich $(MODEL_BASENAME).zenta shippable/implementedBehaviours.xml inputs/issues.xml
	zenta-xslt-runner -xsl:xslt/generate_behaviours.xslt -s $(MODEL_BASENAME).rich modelbasename=$(MODEL_BASENAME) reponame=$(REPO_NAME) github_org=$(GITHUB_ORGANIZATION)

inputs/issues.xml: 
	mkdir -p inputs
	$(TOOLCHAINDIR)/tools/getGithubIssues >inputs/issues.xml

target/install.ok: $(MODEL_BASENAME).compiled shippable/implementedBehaviours.html target/product.war target/coverage-report
	cp -rf $(MODEL_BASENAME)/* target/* shippable
	touch target/install.ok

target/.dir:
	mkdir -p target
	touch target/.dir

target/pomcheck.ok: target/.dir
	pomchecker
	touch target/pomcheck.ok

target/coverage-report target/coverage-report.xml: target/site/jacoco/jacoco.xml target/mutations.xml
	wget https://raw.githubusercontent.com/jacoco/jacoco/master/org.jacoco.report/src/org/jacoco/report/xml/report.dtd -O target/site/jacoco/report.dtd
	zenta-xslt-runner -xsl:xslt/check_coverage.xslt -s:target/mutations.xml -o:target/coverage-report

shippable/.dir:
	mkdir -p shippable
	touch shippable/.dir

target/nodok.ok:
	notCreatingDocumentationInPullRequest

#shippable/$(MODEL_BASENAME)-testcases.xml: $(MODEL_BASENAME).richescape shippable/.dir
#	zenta-xslt-runner -xsl:xslt/generate_test_cases.xslt -s $(MODEL_BASENAME).richescape outputbase=shippable/$(MODEL_BASENAME)-

shippable/implementedBehaviours.xml shippable/implementedBehaviours.docbook shippable/implementedTestCases.xml: target/test/javadoc.xml shippable/.dir $(MODEL_BASENAME).rich
	zenta-xslt-runner -xsl:xslt/generate_implemented_behaviours.xslt -s target/test/javadoc.xml modelbasename=$(MODEL_BASENAME)


$(MODEL_BASENAME).consistencycheck $(MODEL_BASENAME).consistency.stderr: $(MODEL_BASENAME).rich $(MODEL_BASENAME).check $(CONSISTENCY_INPUTS)
	zenta-xslt-runner -xsl:xslt/consistencycheck.xslt -s:$(basename $@).check -o:$@ 2>&1 |tee $(basename $@).consistency.stderr 

target/main.xml target/test/javadoc.xml target/site/cpd.html target/site/project-reports.html target/site/jacoco/jacoco.csv target/site/jacoco/jacoco-sessions.html target/site/jacoco/jacoco.xml target/site/jacoco/index.html target/site/apidocs/index.html target/site/testapidocs/index.html target/mutations.xml target/pmd.xml target/cpd.xml target/production/javadoc.xml target/product.war: $(BEFORE_JAVADOC)
	mkdir -p target/production target/test
	JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn -B javadoc:javadoc javadoc:test-javadoc org.jacoco:jacoco-maven-plugin:prepare-agent site install org.pitest:pitest-maven:mutationCoverage
	ln -s pit-reports/$$(basename target/pit-reports/*)/mutations.xml target/mutations.xml
	ln -s $$(basename target/*.war) target/product.war


target/maven-prepare.ok: target/.dir $(BEFORE_MAVEN_PREPARE)
	mvn -B build-helper:parse-version versions:set versions:commit -DnewVersion=\$${parsedVersion.majorVersion}.\$${parsedVersion.minorVersion}.\$${parsedVersion.incrementalVersion}-$$(/usr/local/toolchain/tools/getbranch|sed 'sA/A_Ag').$$(git rev-parse --short HEAD)
	touch target/maven-prepare.ok


target/zentaworkaround.ok: target/.dir
	mkdir -p ~/.zenta/.metadata/.plugins/org.eclipse.e4.workbench/
	cp $(TOOLCHAINDIR)/etc/workbench.xmi ~/.zenta/.metadata/.plugins/org.eclipse.e4.workbench/
	touch target/zentaworkaround.ok

#shippable/bugpriorities.xml: $(MODEL_BASENAME).consistencycheck inputs/issues.xml $(MODEL_BASENAME).richescape shippable/.dir
#	zenta-xslt-runner -xsl:xslt/issue-priorities.xslt -s:$(MODEL_BASENAME).consistencycheck -o:shippable/bugpriorities.xml issuesfile=inputs/issues.xml modelfile=$(MODEL_BASENAME).richescape missingissuefile=shippable/missing.xml

target/test-classes/resources/keystore.pk12:
	generate_keystore

include /usr/share/zenta-tools/model.rules
