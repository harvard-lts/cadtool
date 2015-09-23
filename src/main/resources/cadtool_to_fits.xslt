<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output">
            <identification>
                <xsl:for-each select="/cad-tool-result/identity">
                    <identity>
                        <xsl:attribute name="format" select="@format"/>
                        <xsl:attribute name="mimetype" select="@mimetype"/>
                        <xsl:if test="@version">
                            <xsl:attribute name="version" select="@version"/>
                        </xsl:if>
                    </identity>
                </xsl:for-each>
            </identification>
        </fits>
    </xsl:template>
</xsl:stylesheet>