<?xml version="1.0" encoding="utf-8" ?>

<!--  

  Copyright 2015-2018 EUROPEAN UNION
  Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
  the European Commission - subsequent versions of the EUPL (the "Licence");
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
 
  http://ec.europa.eu/idabc/eupl
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
 
  Authors: European Commission, Joint Research Centre (JRC)
           Andrea Perego <andrea.perego@ec.europa.eu>
 
  This work was supported by the EU Interoperability Solutions for
  European Public Administrations Programme (http://ec.europa.eu/isa)
  through Action 1.17: Re-usable INSPIRE Reference Platform 
  (http://ec.europa.eu/isa/actions/01-trusted-information-exchange/1-17action_en.htm).

-->

<!--

  PURPOSE AND USAGE

  This XSLT is a proof of concept for the HTML+RDFa representation of metadata 
  based on DCAT-AP, and related extensions (as GeoDCAT-AP). The DCAT-AP 
  specification is available on Joinup, the collaboration platform of the 
  EU ISA Programme:
  
    https://joinup.ec.europa.eu/node/63567/
    
  As such, this XSLT must be considered as unstable, and can be updated any 
  time based on the revisions to the DCAT-AP specification and related 
  extensions.
  
  The official distributions of this XSLT are published in the dedicated GitHub
  repository:
  
    https://github.com/SEMICeu/dcat-ap-rdf2html

  Comments and inquiries should be sent via the corresponding issue tracker: 

    https://github.com/SEMICeu/dcat-ap-rdf2html/issues
  
-->

<xsl:transform
    xmlns:dcat    = "http://www.w3.org/ns/dcat#"
    xmlns:dcterms = "http://purl.org/dc/terms/"
    xmlns:dctype  = "http://purl.org/dc/dcmitype/"
    xmlns:foaf    = "http://xmlns.com/foaf/0.1/"
    xmlns:rdf     = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs    = "http://www.w3.org/2000/01/rdf-schema#"
    xmlns:vcard   = "http://www.w3.org/2006/vcard/ns#"
    xmlns:xsl     = "http://www.w3.org/1999/XSL/Transform"
    version="1.0">

  <xsl:output method="html"
              doctype-system="about:legacy-compact"
              media-type="text/html"
              omit-xml-declaration="yes"
              encoding="UTF-8"
              exclude-result-prefixes="#all"
              indent="yes" />

<!-- Global parameters -->  

<!-- The URL of the repository hosting the XSLT source code -->

  <xsl:param name="home">https://github.com/SEMICeu/dcat-ap-rdf2html</xsl:param>

<!-- The title of the resulting HTML page. 
     This information can  be passed as a parameter by the XSLT 
     processor used. -->

  <xsl:param name="title">
    <xsl:choose>
      <xsl:when test="/rdf:RDF/rdf:Description[@rdf:about='']">
        <xsl:choose>
          <xsl:when test="/rdf:RDF/rdf:Description/dcterms:title">
            <xsl:value-of select="/rdf:RDF/rdf:Description/dcterms:title"/>
          </xsl:when>
          <xsl:when test="/rdf:RDF/rdf:Description/rdfs:label">
            <xsl:value-of select="/rdf:RDF/rdf:Description/rdfs:label"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>DCAT-AP in HTML+RDFa</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>DCAT-AP in HTML+RDFa</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>

<!-- The footer of the resulting HTML page. 
     This information can  be passed as a parameter by the XSLT 
     processor used. -->

  <xsl:param name="footer">
    <p><xsl:value-of select="$title"/><xsl:text> @ Stash: </xsl:text><a href="{$home}"><xsl:value-of select="$home"/></a></p>
  </xsl:param>

<!-- Use this parameter to specify the set of LINK, STYLE, SCRIPT
     elements to be added to the HEAD of the resulting HTML document.
     This information can  be passed as a parameter by the XSLT 
     processor used. -->

  <xsl:param name="head"/>

<!-- Namespace URIs -->

  <xsl:param name="rdf">http://www.w3.org/1999/02/22-rdf-syntax-ns#</xsl:param>

<!-- Class to be used to type untyped blank nodes -->  
  
  <xsl:param name="bnodeClass">
    <rdfs:Resource/>
  </xsl:param>
  <xsl:param name="bnodeClassName" select="name(document('')/*/xsl:param[@name='bnodeClass']/*)"/>
  <xsl:param name="bnodeClassURI" select="concat(namespace-uri(document('')/*/xsl:param[@name='bnodeClass']/*),local-name(document('')/*/xsl:param[@name='bnodeClass']/*))"/>

<!-- Main template -->  
  
  <xsl:template match="/">
  
    <xsl:param name="DatasetNr" select="count(rdf:RDF/dcat:Dataset|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Dataset'])"/>
    <xsl:param name="ServiceNr" select="count(rdf:RDF/dcat:Catalog|rdf:RDF/dctype:Service|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Catalog' or rdf:type/@rdf:resource='http://purl.org/dc/dcmitype/Service'])"/>

<html>
  <head>
    <title><xsl:value-of select="$title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <xsl:value-of select="$head" disable-output-escaping="yes"/>
  </head>
  <body>
    <header><h1><xsl:value-of select="$title"/></h1></header>
    <nav>
    </nav>
    <section>
      <h1>Summary</h1>
      <dl>
        <dt>Datasets</dt>
        <dd><xsl:value-of select="$DatasetNr"/></dd>
        <dt>Services</dt>
        <dd><xsl:value-of select="$ServiceNr"/></dd>
      </dl>
    </section>
    <xsl:apply-templates select="rdf:RDF/dcat:Dataset|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Dataset']"/>
    <xsl:apply-templates select="rdf:RDF/dcat:Catalog|rdf:RDF/dctype:Service|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Catalog' or rdf:type/@rdf:resource='http://purl.org/dc/dcmitype/Service']"/>
<!--    
    <section>
      <h1>Datasets (<xsl:value-of select="count(rdf:RDF/dcat:Dataset|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Dataset'])"/>)</h1>
      <xsl:apply-templates select="rdf:RDF/dcat:Dataset|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Dataset']"/>
    </section>
    <section>
      <h1>Services (<xsl:value-of select="count(rdf:RDF/dcat:Catalog|rdf:RDF/dctype:Service|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Catalog' or rdf:type/@rdf:resource='http://purl.org/dc/dcmitype/Service'])"/>)</h1>
      <xsl:apply-templates select="rdf:RDF/dcat:Catalog|rdf:RDF/dctype:Service|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Catalog' or rdf:type/@rdf:resource='http://purl.org/dc/dcmitype/Service']"/>
    </section>
-->    
    <aside>
    </aside>
    <footer>
      <xsl:copy-of select="$footer"/>
    </footer>
  </body>
</html>
    
  </xsl:template>

  <xsl:template name="Dataset" match="rdf:RDF/dcat:Dataset|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Dataset']">
    <section class="record">
      <h1>Dataset: <span xml:lang="{dcterms:title/@xml:lang}" lang="{dcterms:title/@xml:lang}"><xsl:value-of select="dcterms:title"/></span></h1>
      <xsl:call-template name="Agent"/>
      <p xml:lang="{dcterms:description/@xml:lang}" lang="{dcterms:description/@xml:lang}"><xsl:value-of select="dcterms:description"/></p>
      <xsl:call-template name="metadata"/>
    </section>
  </xsl:template>

  <xsl:template name="Service" match="rdf:RDF/dcat:Catalog|rdf:RDF/dctype:Service|rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://www.w3.org/ns/dcat#Catalog' or rdf:type/@rdf:resource='http://purl.org/dc/dcmitype/Service']">
    <section class="record">
      <h1>Service: <span xml:lang="{dcterms:title/@xml:lang}" lang="{dcterms:title/@xml:lang}"><xsl:value-of select="dcterms:title"/></span></h1>
      <xsl:call-template name="Agent"/>
      <p xml:lang="{dcterms:description/@xml:lang}" lang="{dcterms:description/@xml:lang}"><xsl:value-of select="dcterms:description"/></p>
      <xsl:call-template name="metadata"/>
    </section>
  </xsl:template>
  
  <xsl:template name="Agent">
    <address>
      <dl>
        <xsl:for-each select="dcterms:publisher">
          <xsl:variable name="org" select="*/foaf:name"/>
          <xsl:variable name="email" select="*/foaf:mbox/@rdf:resource"/>
          <xsl:variable name="url" select="*/foaf:workplaceHomepage/@rdf:resource"/>
          <xsl:variable name="name" select="$org"/>
          <dt>Publisher</dt>
          <dd>
          <xsl:choose>
            <xsl:when test="$url != ''">
              <a href="{$url}"><xsl:value-of select="$name"/></a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$name"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="$email != ''">
            <xsl:text> (</xsl:text><a href="{$email}"><xsl:value-of select="substring-after($email, 'mailto:')"/></a><xsl:text>)</xsl:text>
          </xsl:if>
          </dd>
        </xsl:for-each>
        <xsl:for-each select="dcat:contactPoint">
          <xsl:variable name="individual" select="*/vcard:fn"/>
          <xsl:variable name="org" select="*/vcard:organization-name"/>
          <xsl:variable name="email" select="*/vcard:hasEmail/@rdf:resource"/>
          <xsl:variable name="url" select="*/vcard:hasURL/@rdf:resource"/>
          <xsl:variable name="name">
            <xsl:choose>
              <xsl:when test="$individual != '' and $org != ''">
                <xsl:value-of select="$individual"/>
                <xsl:text>, </xsl:text>
                <xsl:value-of select="$org"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$individual"/>
                <xsl:value-of select="$org"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <dt>Contact point</dt>
          <dd>
          <xsl:choose>
            <xsl:when test="$url != ''">
              <a href="{$url}"><xsl:value-of select="$name"/></a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$name"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="$email != ''">
            <xsl:text> (</xsl:text><a href="{$email}"><xsl:value-of select="substring-after($email, 'mailto:')"/></a><xsl:text>)</xsl:text>
          </xsl:if>
          </dd>
        </xsl:for-each>
      </dl>
    </address>
  </xsl:template>
  
  <xsl:template name="metadata">
    <section class="metadata">
      <h2>Metadata</h2>
        <details>
          <summary>Details</summary>
          <xsl:call-template name="subject"/>
        </details>
    </section>
  </xsl:template>

  <xsl:template name="subject">
    <xsl:param name="ename">
      <xsl:call-template name="setEname"/>  
    </xsl:param>
    <xsl:param name="predicate">
      <xsl:call-template name="label"/>
      <dd>
      <xsl:for-each select="*">
        <xsl:call-template name="predicate"/>
      </xsl:for-each>
      </dd>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="@rdf:about">
        <dl about="{@rdf:about}" typeof="{$ename}">
          <xsl:copy-of select="$predicate"/>
        </dl>
      </xsl:when>
      <xsl:when test="@rdf:ID">
        <dl about="#{@rdf:ID}" typeof="{$ename}">
          <xsl:copy-of select="$predicate"/>
        </dl>
      </xsl:when>
      <xsl:when test="@rdf:nodeID">
        <dl typeof="{$ename}">
          <xsl:copy-of select="$predicate"/>
        </dl>
      </xsl:when>
      <xsl:otherwise>
        <dl typeof="{$ename}">
          <xsl:copy-of select="$predicate"/>
        </dl>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="predicate">
    <xsl:param name="ename">
      <xsl:call-template name="setEname"/>  
    </xsl:param>
    <xsl:choose>
<!-- Object properties -->    
      <xsl:when test="(child::* and not(@rdf:parseType='Literal')) or @rdf:resource">
        <dl rel="{$ename}">
          <xsl:call-template name="label"/>
          <xsl:choose>
            <xsl:when test="@rdf:resource">
              <dd><a resource="{@rdf:resource}" href="{@rdf:resource}"><xsl:value-of select="@rdf:resource"/></a></dd>
            </xsl:when>
            <xsl:when test="@rdf:parseType = 'Resource'">
              <dd>
                <dl typeof="{$bnodeClassURI}">
                  <dt><xsl:value-of select="$bnodeClassName"/></dt>
                  <dd>
                  <xsl:for-each select="*">
                    <xsl:call-template name="predicate"/>
                  </xsl:for-each>
                  </dd>
                </dl>
              </dd>
            </xsl:when>
            <xsl:otherwise>
              <dd>
                <xsl:for-each select="*">
                  <xsl:call-template name="subject"/>
                </xsl:for-each>
              </dd>
            </xsl:otherwise>
          </xsl:choose>
        </dl>
      </xsl:when>
<!-- Datatype properties -->      
      <xsl:otherwise>
        <dl>
          <xsl:call-template name="label"/>
          <xsl:choose>
            <xsl:when test="@xml:lang">
              <dd property="{$ename}" content="{.}" xml:lang="{@xml:lang}" lang="{@xml:lang}"><xsl:value-of select="."/></dd>
            </xsl:when>
            <xsl:when test="@rdf:datatype">
              <dd property="{$ename}" content="{.}" datatype="{@rdf:datatype}"><code><xsl:value-of select="."/></code></dd>
            </xsl:when>
            <xsl:when test="@rdf:parseType = 'Literal'">
              <dd property="{$ename}" content="{.}" datatype="{$rdf}XMLLiteral"><code><xsl:value-of select="."/></code></dd>
            </xsl:when>
            <xsl:otherwise>
              <dd property="{$ename}" content="{.}"><xsl:value-of select="."/></dd>
            </xsl:otherwise>
          </xsl:choose>
        </dl>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="label">
    <xsl:param name="qname">
      <xsl:call-template name="setQname"/>  
    </xsl:param>
    <xsl:choose>
<!-- Object properties & individuals -->
      <xsl:when test="child::* or @rdf:about or @rdf:resource">
        <xsl:choose>
          <xsl:when test="@rdf:parseType">
            <dt><xsl:value-of select="$qname"/></dt>
          </xsl:when>
          <xsl:when test="@rdf:nodeID">
            <dt><xsl:value-of select="$qname"/></dt>
          </xsl:when>
          <xsl:when test="@rdf:about">
            <dt><a href="{@rdf:about}"><xsl:value-of select="@rdf:about"/></a><xsl:text> (</xsl:text><xsl:value-of select="$qname"/><xsl:text>)</xsl:text></dt>
          </xsl:when>
          <xsl:when test="@rdf:ID">
            <dt><a href="#{@rdf:ID}"><xsl:value-of select="concat('#',@rdf:about)"/></a><xsl:text> (</xsl:text><xsl:value-of select="$qname"/><xsl:text>)</xsl:text></dt>
          </xsl:when>
          <xsl:otherwise>
            <dt><xsl:value-of select="$qname"/></dt>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
<!-- Datatype properties, plain and typed literals -->      
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@rdf:datatype">
            <dt><xsl:value-of select="$qname"/><xsl:text> (</xsl:text><a href="{@rdf:datatype}"><xsl:value-of select="@rdf:datatype"/></a><xsl:text>)</xsl:text></dt>
          </xsl:when>
          <xsl:when test="@xml:lang">
            <dt><xsl:value-of select="$qname"/><xsl:text> (</xsl:text><xsl:value-of select="@xml:lang"/><xsl:text>)</xsl:text></dt>
          </xsl:when>
          <xsl:otherwise>
            <dt><xsl:value-of select="$qname"/></dt>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="setQname">
    <xsl:choose>
      <xsl:when test="name(.) = 'rdf:Description'">
        <xsl:value-of select="$bnodeClassName"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="name(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="setEname">
    <xsl:choose>
      <xsl:when test="name(.) = 'rdf:Description'">
        <xsl:value-of select="$bnodeClassURI"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat(namespace-uri(.),local-name(.))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:transform>