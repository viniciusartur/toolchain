<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
    xmlns:pom="http://maven.apache.org/POM/4.0.0"
    xmlns:zenta="http://magwas.rulez.org/zenta"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" version="1.0" encoding="utf-8" indent="yes" omit-xml-declaration="yes"/>

	<xsl:variable name="doc" select="/"/>

	<xsl:function name="zenta:exists" as="xs:boolean">
		<xsl:param name="path"/>
		<xsl:variable name="element" select="saxon:evaluate($path, $doc)"/>
		<xsl:choose>
			<xsl:when test="not($element)">
				<xsl:message select="concat(substring($path,5),' not found')"/>
				<xsl:value-of select="false()"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="true()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:function name="zenta:getPathForNode">
	<xsl:param name="node"/>
	<xsl:param name="path"/>
		<xsl:value-of select="
                if ( $node/name() = 'project' )
                then
                    concat('/pom:project/',substring($path, 0,string-length($path)))
                 else 
                    zenta:getPathForNode($node/.., concat('pom:',$node/name(),'/', $path))
            "/>
	</xsl:function>
	
  <xsl:template match="*">
  	<xsl:value-of select="concat('zenta:exists(''$p1/',zenta:getPathForNode(.,''),''') and',codepoints-to-string(10))"/>
  	<xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="/">
    <xsl:copy>
      <xsl:copy-of select="
		zenta:exists('$p1/pom:project/pom:modelVersion') and
		zenta:exists('$p1/pom:project/pom:groupId') and
		zenta:exists('$p1/pom:project/pom:artifactId') and
		zenta:exists('$p1/pom:project/pom:version') and
		zenta:exists('$p1/pom:project/pom:name') and
		zenta:exists('$p1/pom:project/pom:description') and
		zenta:exists('$p1/pom:project/pom:url') and
		zenta:exists('$p1/pom:project/pom:licenses') and
		zenta:exists('$p1/pom:project/pom:licenses/pom:license') and
		zenta:exists('$p1/pom:project/pom:licenses/pom:license/pom:name') and
		zenta:exists('$p1/pom:project/pom:licenses/pom:license/pom:url') and
		zenta:exists('$p1/pom:project/pom:developers') and
		zenta:exists('$p1/pom:project/pom:developers/pom:developer') and
		zenta:exists('$p1/pom:project/pom:developers/pom:developer/pom:name') and
		zenta:exists('$p1/pom:project/pom:developers/pom:developer/pom:email') and
		zenta:exists('$p1/pom:project/pom:developers/pom:developer/pom:organization') and
		zenta:exists('$p1/pom:project/pom:developers/pom:developer/pom:organizationUrl') and
		zenta:exists('$p1/pom:project/pom:scm') and
		zenta:exists('$p1/pom:project/pom:scm/pom:connection') and
		zenta:exists('$p1/pom:project/pom:scm/pom:developerConnection') and
		zenta:exists('$p1/pom:project/pom:scm/pom:url') and
		zenta:exists('$p1/pom:project/pom:properties') and
		zenta:exists('$p1/pom:project/pom:properties/pom:sonar.github.repository') and
		zenta:exists('$p1/pom:project/pom:properties/pom:project.build.sourceEncoding') and
		zenta:exists('$p1/pom:project/pom:properties/pom:maven.compiler.source') and
		zenta:exists('$p1/pom:project/pom:properties/pom:maven.compiler.target')
      "/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

