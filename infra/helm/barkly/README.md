# Barkly Helm Chart

This chart packages the Barkly Kubernetes resources as a single Helm
release.

## Why Helm

Helm does not replace Kubernetes. It renders Kubernetes manifests from
templates and values and manages the resulting resources as one release.

``` text
values.yaml + templates
          |
          v
         Helm
          |
          v
Kubernetes manifests
          |
          v
      Kubernetes
```

`values.yaml` contains inputs consumed by templates.

``` yaml
api:
  image:
    repository: barkly-api
    tag: local
  replicas: 2
```

A template can use them as:

``` yaml
replicas: {{ .Values.api.replicas }}
image: "{{ .Values.api.image.repository }}:{{ .Values.api.image.tag }}"
```

## Managed Resources

The chart includes the API and web workloads, Services, API ConfigMap
and Secret, health probes, resource configuration, HPA, PDB, PostgreSQL
StatefulSet and persistent storage, and Ingress.

## Validate

``` bash
helm lint infra/helm/barkly
helm template barkly infra/helm/barkly -n barkly
```

`helm template` renders the Kubernetes manifests without installing
them.

## Install

``` bash
kubectl create namespace barkly
helm install barkly infra/helm/barkly -n barkly
```

Inspect the release:

``` bash
helm list -n barkly
helm status barkly -n barkly
```

## Upgrade

``` bash
helm upgrade barkly infra/helm/barkly -n barkly
```

A useful form for future automation is:

``` bash
helm upgrade --install barkly infra/helm/barkly -n barkly
```

## History and Rollback

``` bash
helm history barkly -n barkly
helm rollback barkly <revision> -n barkly
```

Helm tracks revisions for the release rather than treating every
resource as an unrelated deployment operation.

## Conditional Resources

Templates can conditionally create resources. For example:

``` yaml
api:
  autoscaling:
    enabled: true
```

can control whether the HPA template is rendered.

## Local Access

Barkly application Services remain `ClusterIP`. HTTP traffic enters
through Traefik and Ingress.

When the local Traefik LoadBalancer address is not reachable from
Windows:

``` bash
kubectl port-forward -n kube-system service/traefik 8080:80
```

With `127.0.0.1 barkly.local` configured in the Windows hosts file, use
`http://barkly.local:8080`.

PostgreSQL remains internal and is reached by the API as
`barkly-postgres:5432`.

## Raw Kubernetes Manifests

`infra/k8s` is intentionally retained for now. It documents the
underlying resources and allows direct comparison between raw manifests
and Helm templates.

The Helm chart is the preferred installation mechanism. The raw
manifests can be removed later if maintaining both starts causing
configuration drift.

## Current Scope

The chart currently focuses on local Kubernetes deployment.

Deferred topics:

-   environment-specific values
-   production deployment
-   automatic Helm deployment from GitHub Actions
-   immutable image tags
-   application versioning and release management
-   production secret management
-   highly available PostgreSQL
