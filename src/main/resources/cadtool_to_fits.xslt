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
            <fileinfo>
                <xsl:for-each select="/cad-tool-result/created">
                    <created>
                        <xsl:value-of select="."/>
                    </created>
                </xsl:for-each>
                <xsl:for-each select="/cad-tool-result/modified">
                    <lastmodified>
                        <xsl:value-of select="."/>
                    </lastmodified>
                </xsl:for-each>
                <xsl:for-each select="/cad-tool-result">
                    <filename>
                        <xsl:value-of select="@file"/>
                    </filename>
                </xsl:for-each>
                <xsl:for-each select="/cad-tool-result/creatingApplicationName">
                    <creatingApplicationName>
                        <xsl:value-of select="."/>
                    </creatingApplicationName>
                </xsl:for-each>
                <!-- copyrightNote?, -->
            </fileinfo>
            <!-- file status valid or well-formed? -->
            <!-- http://hul.harvard.edu/ois/xml/xsd/fits/fits_output.xsd -->
        </fits>
    </xsl:template>
</xsl:stylesheet>