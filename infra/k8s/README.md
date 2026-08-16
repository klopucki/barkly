# Barkly Kubernetes Deployment

This directory contains the raw Kubernetes manifests for Barkly. They
are kept as an explicit representation of the Kubernetes architecture
and as a reference for the Helm chart in `infra/helm/barkly`.

## Architecture

``` text
                         Client
                           |
                           v
                    Traefik Ingress
                           |
              +------------+------------+
              |                         |
              v                         v
          path: /                   path: /api
              |                         |
              v                         v
      barkly-web Service        barkly-api Service
              |                         |
              v                         v
        Web Deployment          API Deployment
        Angular/Nginx            API Pods (2-5)
                                        |
                                        v
                              barkly-postgres Service
                                        |
                                        v
                              PostgreSQL StatefulSet
                              barkly-postgres-0
                                        |
                                        v
                              PersistentVolumeClaim
```

All resources belong to the `barkly` namespace.

## Responsibilities

### Deployments

`barkly-web` and `barkly-api` are stateless workloads. Deployments
maintain the desired number of Pods and replace failed Pods
automatically.

### Services

Services provide stable networking independent of Pod names and IP
addresses.

-   `barkly-web` routes traffic to frontend Pods.
-   `barkly-api` routes traffic to ready API Pods.
-   `barkly-postgres` exposes PostgreSQL inside the cluster.

These Services use `ClusterIP`; they do not need individual external IP
addresses.

### Ingress

Traefik is the Ingress Controller. The Barkly Ingress routes:

``` text
/       -> barkly-web
/api    -> barkly-api
```

### PostgreSQL

PostgreSQL is managed by a StatefulSet because it stores state. Its Pod
has the stable identity `barkly-postgres-0`.

The current setup contains one PostgreSQL instance and does not provide
database replication or high availability.

### Persistent Storage

`barkly-postgres-pvc` provides persistent storage:

``` text
PostgreSQL Pod
      |
      v
PersistentVolumeClaim
      |
      v
PersistentVolume
```

Recreating the PostgreSQL Pod does not remove its data while the PVC
remains intact. Deleting the PVC or namespace may remove local data
depending on the StorageClass reclaim policy.

### ConfigMap and Secret

`barkly-api-config` stores non-sensitive API configuration.
`barkly-api-secret` stores database credentials. The API imports both
through `envFrom`.

Environment variables are loaded when a container starts. Restart API
Pods after changing these resources:

``` bash
kubectl rollout restart deployment/barkly-api -n barkly
```

## Health Probes

The API uses Spring Boot Actuator.

-   **Startup probe** determines whether application startup has
    completed.
-   **Readiness probe** determines whether a Pod can receive traffic.
-   **Liveness probe** determines whether an unhealthy container should
    be restarted.

Endpoints:

``` text
/actuator/health/liveness
/actuator/health/readiness
```

## Resource Management

CPU and memory requests help the scheduler choose a Node. Limits
restrict maximum resource consumption.

If no Node can satisfy requests, a Pod remains `Pending`. Exceeding a
memory limit can result in `OOMKilled`; CPU limits may cause throttling.

``` bash
kubectl top pods -n barkly
kubectl top nodes
```

## Horizontal Pod Autoscaler

The API HPA currently scales between 2 and 5 replicas based on CPU
utilization.

``` text
normal load          increased load

API Pod              API Pod
API Pod     --->     API Pod
                     API Pod
                     API Pod
                     API Pod
```

``` bash
kubectl get hpa -n barkly
kubectl describe hpa barkly-api -n barkly
```

## Pod Disruption Budget

The API PDB uses `minAvailable: 1`. During voluntary disruptions
Kubernetes should preserve at least one available API Pod.

It does not protect against every unexpected application, Node or
infrastructure failure.

## Request Flow

``` text
Browser
   |
   v
Traefik
   |
   v
Ingress
   |
   v
barkly-api Service
   |
   v
Ready API Pod
   |
   v
barkly-postgres Service
   |
   v
barkly-postgres-0
   |
   v
PostgreSQL / persistent storage
```

Frontend requests follow
`Browser -> Traefik -> Ingress -> barkly-web Service -> Angular/Nginx Pod`.

## Failure and Recovery

If an API Pod fails, the Service stops routing traffic to it. The
Deployment creates a replacement because the actual state no longer
matches the desired replica count. The replacement receives traffic only
after startup and readiness probes succeed.

## Rolling Updates

Deployments gradually replace old Pods:

``` text
API v1   API v1
     |
     v
API v1   API v1   API v2 [starting]
     |
     v
API v1   API v2 [ready]
     |
     v
API v2   API v2
```

Monitor with:

``` bash
kubectl rollout status deployment/barkly-api -n barkly
```

## Useful Commands

``` bash
kubectl get all -n barkly
kubectl get ingress,pvc,hpa,pdb -n barkly
kubectl logs deployment/barkly-api -n barkly
kubectl logs statefulset/barkly-postgres -n barkly
kubectl describe pod <pod-name> -n barkly
```

## Mental Model

``` text
Deployment / StatefulSet
            |
            | creates and maintains
            v
           Pods

Service
            |
            | discovers Pods using labels
            v
           Pods

Ingress
            |
            | routes HTTP traffic
            v
         Services
```

Kubernetes is declarative: manifests describe the desired state and
controllers continuously work to align the actual state with it.
