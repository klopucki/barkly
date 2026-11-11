# Image object storage

Barkly stores optimized JPEGs in an S3 bucket and serves them through the existing
`/api/training-images/{key}` URL. The bucket must already exist. This store is shared
by training, school and dog images. Configure `BARKLY_S3_BUCKET`, `BARKLY_S3_REGION`,
`BARKLY_S3_ENDPOINT` (empty for AWS), and `BARKLY_S3_PATH_STYLE`. The AWS SDK default
credential chain supports environment variables and workload roles; never commit real
credentials. Grant the application only GetObject, PutObject and DeleteObject on its
bucket. For AWS use a separate bucket, role, region and credentials per environment.

## Assessment

[PGSTY Silo](https://github.com/pgsty/silo) is an independently maintained MinIO fork,
with S3 compatibility and security releases, licensed AGPL-3.0-or-later. Evaluate the
license obligations and upstream advisories for the intended deployment. The pinned
single-node Kubernetes manifest is for local development, not high availability:
the PVC and host are failure domains. Object storage does not replace an
independent backup. Use TLS for traffic leaving the trusted cluster, network policies,
monitoring, capacity alerts and a supported backup destination before use. Production
can instead use managed S3 with its own credentials and storage.

## Local Kubernetes setup

Create a `silo-root-credentials` secret with `username` and `password` keys in the
`barkly` namespace, then apply `silo.yaml` there. Using the bundled `mcli`, create a
private `barkly-images` bucket and an application user with GetObject, PutObject and
DeleteObject permissions on that bucket. Create a `barkly-s3-credentials` secret with
`AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` keys for that user. The local Helm
values use the `barkly-silo` service and this secret.

Deployment environments, their credentials, backups and storage configuration will be
prepared together when an environment is provisioned. Do not use the local credentials
or single-node manifest as a production deployment.

## Migration and recovery

Existing local image files are not migrated automatically. When a database key points
to a missing object, the API displays a non-cached placeholder; new uploads use S3.
The previous Helm PVC `barkly-training-images` is not deleted automatically. Retain it
if the old files may still be needed, or follow your normal data removal process.

Back up objects to a separate storage account or site using scheduled, encrypted
S3-aware copies with versioning/retention appropriate to the environment. Coordinate
backup points with database backups because the database contains the image keys.
Regularly restore both into an isolated environment and verify object count, checksums
and representative image URLs. A PVC snapshot alone is not an independent backup.

For Docker Compose, `docker compose -f infra/docker-compose.yml up --build` starts a
persistent local Silo and creates the bucket. Its credentials are development-only.
For direct API runs, set the endpoint to `http://localhost:9000`, path style to `true`,
and set `AWS_ACCESS_KEY_ID=barkly-local` and
`AWS_SECRET_ACCESS_KEY=barkly-local-password` after starting the Compose storage.

To verify the adapter against the local bucket after Compose has started Silo:

```bash
BARKLY_TEST_S3_ENDPOINT=http://localhost:9000 ./gradlew -p api test --rerun-tasks
```

The integration test uses the development credentials above and creates and removes
one test image. It is skipped when `BARKLY_TEST_S3_ENDPOINT` is not set.
