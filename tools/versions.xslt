<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
    xmlns:pom="http://maven.apache.org/POM/4.0.0"
    xmlns:zenta="http://magwas.rulez.org/zenta"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" version="1.0" encoding="utf-8" indent="yes" omit-xml-declaration="yes"/>

    <xsl:param name="metamodelUri" select="'https://raw.githubusercontent.com/kode-konveyor/metamodel/develop/metamodel.zenta'"/>

  <xsl:template match="/">
    <xsl:variable name="metamodel" select="document($metamodelUri)"/>
    <xsl:variable name="model" select="/"/>
    <xsl:variable name="modelVersion" select="$model//folder[@id='metamodel']/property[@key='version']/@value"/>
    <xsl:variable name="failBelow" select="$metamodel//folder[@id='metamodel']/property[@key='fail below']/@value"/>
    <xsl:variable name="minimumToolchainVersion" select="$metamodel//folder[@id='metamodel']/property[@key='minimum toolchain version']/@value"/>#
modelVersion=<xsl:value-of select="$modelVersion"/>
failBelow=<xsl:value-of select="$failBelow"/>
minimumToolchainVersion=<xsl:value-of select="$minimumToolchainVersion"/>
#
</xsl:template>

</xsl:stylesheet>

