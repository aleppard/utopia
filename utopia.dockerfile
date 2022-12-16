FROM tomcat:10.0-jre8
COPY target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
