# DAS Jira
[![License](https://img.shields.io/:license-BSL%201.1-blue.svg)](/licenses/BSL.txt)

[Data Access Service](https://github.com/raw-labs/protocol-das) for [Jira](https://www.atlassian.com/software/jira).

## Options

| Name                    | Description                                                        | Default | Required |
|-------------------------|--------------------------------------------------------------------|---------|----------|
| `base_url`              | Jira base URL                                                      |         | Yes      |
| `personal_access_token` | API PAT for self hosted Jira instances                             |         | No       |
| `token`                 | API token for user's Atlassian account.                            |         | Yes      |
| `username`              | Email address of agent user who have permission to access the API. |         | Yes      |
