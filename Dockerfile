FROM jboss/wildfly:10.1.0.Final
COPY target/gateway.war /opt/jboss/wildfly/standalone/deployments/