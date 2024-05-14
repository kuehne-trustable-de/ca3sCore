<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ysl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:variable name="QUOTE">"</xsl:variable>
    <xsl:variable name="NEWLINE" select="'&#10;'"/>
    <xsl:variable name="DEFAULT_INDENT">  </xsl:variable>


    <xsl:template match="/">
<xsl:text>
# ca3s configuration properties

</xsl:text>

        <xsl:apply-templates select="config/*" >
            <xsl:with-param name="prefix"/>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template match="comment">
        <xsl:text>
</xsl:text>

        <xsl:if test="@mode='section'">
            <xsl:text>
## </xsl:text>
        </xsl:if>
        <xsl:if test="@mode='subsection'">
            <xsl:text>
### </xsl:text>
        </xsl:if>

        <xsl:call-template name="handleNewline">
            <xsl:with-param name="content" select="text()"/>
        </xsl:call-template>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <!-- dump a node, once there is a value avaliable -->
    <xsl:template match="node()">
        <xsl:param name="prefix" />

        <xsl:variable name="prepend">
            <xsl:if test="string-length($prefix) > 0">
                <xsl:value-of select="concat($prefix, '.')"/>
            </xsl:if>
        </xsl:variable>

        <xsl:apply-templates select="*" >
            <xsl:with-param name="prefix" select="concat($prepend, local-name())"/>
        </xsl:apply-templates>


        <xsl:choose>
            <xsl:when test="self::node() [value]">
                <xsl:text>
    </xsl:text>
                <xsl:value-of select="concat($prepend, local-name(), ' = ', value)"/><xsl:text>
</xsl:text>
            </xsl:when>

            <xsl:otherwise>


            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


    <xsl:template name="handleNewline">
        <xsl:param name="content"/>

        <xsl:choose>
            <xsl:when test="contains($content, $NEWLINE)">
                <xsl:value-of select="normalize-space(substring-before($content, $NEWLINE))"/><xsl:text> </xsl:text>

                <xsl:call-template name="handleNewline">
                    <xsl:with-param name="content" select="normalize-space(substring-after($content, $NEWLINE))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space($content)"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

</xsl:stylesheet>
