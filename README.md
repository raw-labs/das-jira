# DAS Jira

## How to use

First you need to build the project:
```bash
$ sbt "project docker" "docker:publishLocal"
```

This will create a docker image with the name `das-jira`.

Then you can run the image with the following command:
```bash
$ docker run -p 50051:50051 <image_id>
```
... where `<image_id>` is the id of the image created in the previous step.
This will start the server on port 50051.

You can find the image id by looking at the sbt output or by running:
```bash
$ docker images
```

## Options


| Name                    | Description                                                        | Default | Required |
|-------------------------|--------------------------------------------------------------------|---------|----------|
| `base_url`              | Jira base url                                                      |         | Yes      |
| `personal_access_token` | API PAT for self hosted Jira instances                             |         | Yes      |
| `token`                 | API token for user's Atlassian account.                            |         | Yes      |
| `uesrname`              | Email address of agent user who have permission to access the API. |         | Yes      |