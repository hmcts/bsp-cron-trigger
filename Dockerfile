 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.2
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/bsp-cron-trigger.jar /opt/app/

EXPOSE 5678
CMD [ "bsp-cron-trigger.jar" ]
