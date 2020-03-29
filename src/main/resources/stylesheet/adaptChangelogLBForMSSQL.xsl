<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:dbcl="http://www.liquibase.org/xml/ns/dbchangelog"
	xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext">

	<xsl:param name="MssqlLiquibaseTarget">http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd</xsl:param>

	<xsl:param name="InsertSequence">false</xsl:param>

	<xsl:template match="@xsi:schemaLocation">
		<xsl:attribute name="xsi:schemaLocation" select="$MssqlLiquibaseTarget" />
	</xsl:template>

	<xsl:template match="dbcl:databaseChangeLog">
		<xsl:copy>
			<xsl:apply-templates select="@*" />

        	<dbcl:property name="autoIncrement" value="true"/>
        	
        	<xsl:if test="$InsertSequence = 'true'">
	        	<dbcl:changeSet id="00000000000000" author="jhipster">
	        		<dbcl:createSequence sequenceName="sequence_generator" startValue="1050" incrementBy="50"/>
	    		</dbcl:changeSet>
        	</xsl:if>
        	
			<xsl:apply-templates select="node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="dbcl:loadData">
		<ext:loadData>
			<xsl:apply-templates select="@*" />
			<xsl:attribute name="identityInsertEnabled" select="'true'" />
			
			<xsl:apply-templates select="node()" />
		</ext:loadData>
	</xsl:template>


	<xsl:template match="dbcl:changeSet [@id='1580410283605-305']"/>
	
	<xsl:template match="dbcl:databaseChangeLog/dbcl:property [@name='autoIncrement']"/>
	
	<xsl:template match="dbcl:constraints/@unique">
		<xsl:attribute name="unique" select="'false'" />
	</xsl:template>
	
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>