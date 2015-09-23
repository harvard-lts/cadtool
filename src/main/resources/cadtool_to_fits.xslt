<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <xsl:element name="fits">
            <xsl:copy-of select="/cad-tool-result/identity"/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>