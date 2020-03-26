<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">


	<xsl:template match="project/dependencies">
	
		<xsl:copy>
			<xsl:apply-templates select="@*" />
		</xsl:copy>
		
		<dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
            <version>3.5.5</version>
        </dependency>
        <dependency>
            <groupId>com.github.sabomichal</groupId>
            <artifactId>liquibase-mssql</artifactId>
        </dependency>
        
		<xsl:copy>
			<xsl:apply-templates select="@*" />
		</xsl:copy>
	
	</xsl:template>

	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>