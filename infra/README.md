# Barkly Infrastructure

This directory contains the infrastructure configuration used to run
Barkly locally and in Kubernetes.

## Overview

``` text
Application source
       |
       v
Docker images
       |
       v
Kubernetes workloads
       |
       v
Helm release
```

### Docker

Docker provides container images for the Barkly API and web application.
CI already builds and publishes these images.

### Kubernetes

The `k8s` directory contains the raw Kubernetes manifests used to
introduce and document the Kubernetes architecture: Deployments,
Services, StatefulSet, Ingress, health probes, resource limits,
autoscaling and persistent storage.

They are retained as a useful reference for the Kubernetes configuration
behind the Helm chart.

### Helm

The `helm/barkly` directory contains the Helm chart used to package the
Kubernetes resources as a single Barkly release.

Helm provides centralized configuration through `values.yaml`, reusable
templates, conditional resources, release upgrades, history and rollback
support.

The Helm chart is the preferred way to install the complete Barkly stack
into Kubernetes.

## Local Kubernetes

Barkly is currently tested using a local Kubernetes cluster provided by
Rancher Desktop. Application resources run in the `barkly` namespace.

``` bash
helm lint infra/helm/barkly
helm template barkly infra/helm/barkly -n barkly
helm install barkly infra/helm/barkly -n barkly
helm status barkly -n barkly
kubectl get all -n barkly
```

If Traefik is not directly reachable from the Windows host:

``` bash
kubectl port-forward -n kube-system service/traefik 8080:80
```

Configure `127.0.0.1 barkly.local` in the Windows hosts file and use
`http://barkly.local:8080`.

## Scope

The current infrastructure is intended for local development and
learning. Environment-specific configuration, automated Helm deployment,
immutable image tags, release/version management and production secret
management are intentionally deferred.
