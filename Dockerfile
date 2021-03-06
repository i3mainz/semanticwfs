FROM tomcat:9

RUN apt-get update

RUN apt-get install unattended-upgrades apt-listchanges -y

COPY *.json /usr/local/tomcat/

COPY gmd2geodcat.xsl /usr/local/tomcat/

COPY dcat-ap-rdf2rdfa.xsl /usr/local/tomcat/

COPY users.xml /usr/local/tomcat/

COPY prefixes.txt /usr/local/tomcat/

COPY htmlcovtemplate.txt /usr/local/tomcat/

COPY htmltemplate.txt /usr/local/tomcat/

COPY htmltemplate2.txt /usr/local/tomcat/

COPY target/*.war /usr/local/tomcat/webapps/