<?xml version="1.0" encoding="utf-8"?>
<!-- 
     Extracts the polarities out of a tree adjoining grammar in XMG
     format.  See file `polarities.dtd,xml' for the format of a
     polarity file.

     Copyright (C) 2008 INRIA
     Author: Sylvain Schmitz <Sylvain.Schmitz@loria.fr>

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan">
  
<xsl:output method="xml" indent="yes" version="1.0"
            encoding="utf-8"
            doctype-system="polarities.dtd,xml"
            xalan:indent-amount="2"/>

<xsl:strip-space elements="*"/>

<xsl:template match="grammar">
  <polarities>
    <xsl:apply-templates/>
  </polarities>
</xsl:template>

<xsl:template match="entry">
  <entry name="{@name}">
    <xsl:copy-of select="family"/>
    <!--<left>
      <xsl:apply-templates select="tree" mode="left"/>
    </left>-->
    <global>
      <xsl:apply-templates select="tree" mode="global"/>
    </global>
  </entry>
</xsl:template>

<xsl:template match="tree" mode="left">
    <xsl:call-template name="foot"/>

    <!-- Only consider substitution nodes that precede the anchor.
         Beware!  Will not work properly on TT-MCTAGs! -->
    <xsl:for-each 
        select="descendant::node[@type='subst' or @type='lex']">
      <xsl:if test="ancestor-or-self::node/following-sibling::node/descendant-or-self::node[@type='anchor']">
        <xsl:call-template name="node"/>
      </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template match="tree" mode="global">
    <xsl:call-template name="foot"/>
    
    <xsl:for-each 
        select="descendant::node[@type='subst' or @type='lex' or @type='anchor' or @type='coanchor']">
      <xsl:call-template name="node"/>
    </xsl:for-each>
</xsl:template>

<xsl:template name="foot">
  <!-- If the tree is an initial tree, then its root category
       provides a positive polarity. -->
  <xsl:if test="not(descendant::node[@type='foot'])">
    <plus name="{node/narg/fs/f[@name='cat']/sym/@value}"/>
  </xsl:if>  
</xsl:template>

<xsl:template name="node">
  <xsl:choose>
    <xsl:when test="@type='subst'">
      <minus name="{narg/fs/f[@name='cat']/sym/@value}"/>
    </xsl:when>
    <xsl:when test="@type='lex'">
      <lex name="{narg/fs/f[@name='cat']/sym/@value}"/>
    </xsl:when>
    <xsl:when test="@type='anchor'">
      <anchor/>
    </xsl:when>
    <xsl:when test="@type='coanchor'">
      <coanchor name="{narg/fs/f[@name='cat']/sym/@value}"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>