# Local Barkly environment

This setup targets a single-node Rancher Desktop Kubernetes cluster. It runs Barkly,
PostgreSQL, Prometheus, Loki, Grafana Alloy, Grafana and Portainer. The charts and image tags here are for
local development only, not an internet-facing or production installation.

## Prerequisites

- Docker, `kubectl`, Helm, OpenSSL and a running Rancher Desktop cluster using the Docker runtime.
- Current `kubectl` context must be `rancher-desktop`. The scripts refuse other contexts
  unless explicitly overridden with `BARKLY_KUBE_CONTEXT`.
- Free local ports 8088, 3001, 9443 and 19091, plus enough disk and memory for the cluster.
- Network access to the Helm chart repositories and container registries.

## Install or update

From the repository root:

```bash
./infra/local-observability/up.sh
```

This builds uniquely tagged local API and web images, deploys Barkly with one API
replica, enables the internal Prometheus endpoint and provisions PVCs for PostgreSQL
and images. It installs pinned Prometheus, Loki, Alloy, Grafana and Portainer Helm charts. On
updates it restarts the API and web deployments after rebuilding the local tags.

The Kubernetes PostgreSQL PVC is separate from any existing Docker Compose database.
A new database is populated by Flyway migrations, including their baseline seed data;
no data is imported from Docker. Existing Docker volumes are not modified. The local
chart values contain development-only database credentials.

Prometheus collects Kubernetes and node metrics plus the Barkly API's Micrometer
metrics. It stores seven days of metrics, subject to the size of its PVC. Grafana
provisions the `Barkly — local overview` dashboard and Prometheus data source. Alloy
collects Barkly namespace pod logs and sends them to Loki, which stores them on a local
PVC with seven-day retention; Grafana has Loki configured as a data source. Open
**Explore → Loki** to filter logs by namespace, pod and container. There is no configured
external alert delivery.

## Access

Run the following command in a terminal and leave it running:

```bash
./infra/local-observability/ports.sh
```

- Barkly: http://localhost:8088
- Prometheus: http://localhost:19091
- Grafana: http://localhost:3001 — username `admin`; get the generated password locally:

  ```bash
  kubectl -n monitoring get secret barkly-grafana -o jsonpath='{.data.admin-password}' | base64 --decode; echo
  ```

- Portainer: https://localhost:9443 — the locally generated certificate is self-signed.
  Sign in as `admin` with the generated initial password, retrieved locally:

  ```bash
  kubectl -n portainer get secret portainer-admin-password -o jsonpath='{.data.password}' | base64 --decode; echo
  ```

  This secret contains the initial password only; if you change the password in Portainer,
  use the new password rather than the secret. Keep credentials private. Portainer has
  cluster administrator permissions.

All four services are bound to `127.0.0.1` by the forwarding script. Press Ctrl+C to
stop forwarding; the Kubernetes workloads and PVCs remain in place. Persistent volumes
use Rancher Desktop's local-path storage and are not backups. They remain dependent on
the local cluster and should not be treated as production-grade storage.
