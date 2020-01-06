FROM tomcat:latest

RUN apt-get update

RUN apt-get install unattended-upgrades apt-listchanges -y

COPY *.json /usr/local/tomcat/

COPY target/*.war /usr/local/tomcat/webapps/