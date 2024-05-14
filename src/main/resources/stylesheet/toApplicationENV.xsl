<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ysl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:variable name="QUOTE">"</xsl:variable>
    <xsl:variable name="NEWLINE" select="'&#10;'"/>


    <xsl:template match="/">
        <xsl:apply-templates select="config/*" >
            <xsl:with-param name="prefix"/>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template match="comment">
            <xsl:if test="@mode='section'"><xsl:text>
</xsl:text></xsl:if>
        <xsl:call-template name="handleNewline">
            <xsl:with-param name="content" select="text()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="node()">
        <xsl:param name="prefix" />

        <xsl:variable name="currentName" select="translate(local-name(), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
        <xsl:variable name="currentPath">
          <xsl:if test="string-length($prefix) > 0">
            <xsl:value-of select="concat($prefix, '_')"/>
          </xsl:if>
          <xsl:value-of select="$currentName"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="self::node() [value]">
                <xsl:variable name="escapedValue">
                    <xsl:call-template name="escapeChars">
                      <xsl:with-param name="content" select="normalize-space(value)"/>
                    </xsl:call-template>
                </xsl:variable>

                <xsl:value-of select="concat($currentPath, ': ', $QUOTE, $escapedValue, $QUOTE )"/><xsl:text>
</xsl:text>
            </xsl:when>

            <xsl:otherwise>

                <xsl:apply-templates select="./*" >
                    <xsl:with-param name="prefix" select="$currentPath"/>
                </xsl:apply-templates>

            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


    <xsl:template name="handleNewline">
        <xsl:param name="content"/>

        <xsl:choose>
            <xsl:when test="contains($content, $NEWLINE)">
                <xsl:text># </xsl:text><xsl:value-of select="normalize-space(substring-before($content, $NEWLINE))"/><xsl:text>
</xsl:text>

                <xsl:call-template name="handleNewline">
                    <xsl:with-param name="content" select="substring-after($content, $NEWLINE)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text># </xsl:text><xsl:value-of select="normalize-space($content)"/><xsl:text>
</xsl:text>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


<xsl:template name="escapeChars">
    <xsl:param name="content"/>

    <xsl:choose>
        <xsl:when test="contains($content, '\')">
            <xsl:variable name="pre" select="substring-before($content, '\')"/>
            <xsl:variable name="post">
                <xsl:call-template name="escapeChars">
                    <xsl:with-param name="content" select="substring-after($content, '\')"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="concat($pre, '\\', $post)"/>

        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$content"/>
        </xsl:otherwise>
    </xsl:choose>

</xsl:template>
    <!--
        <xsl:template match="node()">

            <xsl:choose>
                <xsl:when test="string-length( normalize-space(text())) > 0">
                    <ysl:copy>
                        <value><xsl:value-of select="normalize-space(text())"/></value>
                    </ysl:copy>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:copy>
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:template>
    -->

</xsl:stylesheet>
