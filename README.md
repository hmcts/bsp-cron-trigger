# bsp-cron-trigger

A lightweight Spring Boot application that runs scheduled Bulk Print and Bulk Scan health-check jobs and reports results to Slack.

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Prerequisites](#prerequisites)
5. [Configuration](#configuration)
6. [Building the Application](#building-the-application)
7. [Running the Application](#running-the-application)
8. [Testing](#testing)
9. [Contributing](#contributing)
10. [License](#license)

## Overview
`bsp-cron-trigger` is designed to perform two daily checks:

- **Bulk Print Checks**
  Fetches stale letters from the Send-Letter service, marks them as `Created` or `Aborted` based on age/status, and sends a summary to Slack.

- **Bulk Scan Checks**
  1. Cleans up stale blobs in the Blob-Router service.
  2. Deletes and reprocesses incomplete envelopes via the Bulk-Scan Processor.
  3. Retries failed payments (new and update) through the Bulk-Scan Orchestrator.

Results of each run are chunked and delivered to Slack using the configured channel.

## Features
- Modular triggers using the `Trigger` interface for easy extension.
- Feign clients for inter-service communication:
  - `BlobRouterServiceClient`
  - `BulkScanProcessorClient`
  - `BulkScanOrchestratorClient`
  - `SendLetterServiceClient`
- Configurable via Spring Boot `application.yml` or environment variables.
- Slack integration with automatic chunking for long messages.
- Graceful error handling with action summaries.

## Architecture

All triggers implement the `Trigger` interface and are selected at startup based on the `app.trigger-type` property.

## Prerequisites
- Java 11+ (JDK)
- Docker & Docker Compose v2
- Git

## Configuration

| Property                          | Description                                          | Environment Variable                  |
|-----------------------------------|------------------------------------------------------|---------------------------------------|
| `url.blob-router-service`         | Base URL for Blob-Router service                     | `BLOB_ROUTER_SERVICE_URL`             |
| `url.bulk-scan-processor`         | Base URL for Bulk-Scan Processor                     | `BULK_SCAN_PROCESSOR_URL`             |
| `url.bulk-scan-orchestrator`      | Base URL for Bulk-Scan Orchestrator                  | `BULK_SCAN_ORCHESTRATOR_URL`          |
| `url.send-letter-service`         | Base URL for Send-Letter service                     | `SEND_LETTER_SERVICE_URL`             |
| `authorisation.bearer-token`      | Bearer token for authorization headers               | `AUTHORISATION_BEARER_TOKEN`          |
| `slack.token-daily-checks`        | Slack bot token                                      | `SLACK_TOKEN_DAILY_CHECKS`            |
| `slack.channel-id-daily-checks`   | Slack channel ID                                     | `SLACK_CHANNEL_ID_DAILY_CHECKS`       |
| `app.trigger-type`                | Which job to run: `BULK_PRINT_CHECKS` or `BULK_SCAN_CHECKS` | `APP_TRIGGER_TYPE`              |
| `app.enabled`                     | Enable or disable the runner (`true`/`false`)        | `APP_ENABLED`                         |

Example `application.yml`:

```yaml
url:
  blob-router-service: ${BLOB_ROUTER_SERVICE_URL}
  bulk-scan-processor: ${BULK_SCAN_PROCESSOR_URL}
  bulk-scan-orchestrator: ${BULK_SCAN_ORCHESTRATOR_URL}
  send-letter-service: ${SEND_LETTER_SERVICE_URL}

authorisation:
  bearer-token: ${AUTHORISATION_BEARER_TOKEN}

slack:
  token-daily-checks: ${SLACK_TOKEN_DAILY_CHECKS}
  channel-id-daily-checks: ${SLACK_CHANNEL_ID_DAILY_CHECKS}

app:
  trigger-type: ${APP_TRIGGER_TYPE:BULK_PRINT_CHECKS}
  enabled: ${APP_ENABLED:true}
```

## Building/Running the Application

Use the Gradle wrapper to build the jar:
`./gradlew clean build`

Make sure the environment variables are set (refer to testing section).

## Testing
Run unit and integration tests:
`./gradlew test`

To test against AAT (configure service URLs beforehand):
```shell
export SEND_LETTER_SERVICE_URL="http://...-send-letter-service-aat..."
export BLOB_ROUTER_SERVICE_URL="http://...-blob-router-aat..."
export BULK_SCAN_PROCESSOR_URL="http://...-bulk-scan-processor-aat..."
export BULK_SCAN_ORCHESTRATOR_URL="http://...-bulk-scan-orchestrator-aat..."
./gradlew build
```

## Contributing

Fork the repository

Create a feature branch (git checkout -b feature/XYZ)

Commit your changes

Open a pull request

## License

This project is licensed under the MIT License. See the LICENSE file f



