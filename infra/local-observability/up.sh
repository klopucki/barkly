#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
expected_context="${BARKLY_KUBE_CONTEXT:-rancher-desktop}"
actual_context="$(kubectl config current-context)"
if [[ "$actual_context" != "$expected_context" ]]; then
  printf 'Refusing to deploy: current context is %s, expected %s.\n' "$actual_context" "$expected_context" >&2
  exit 1
fi
cd "$repo_root"
helm repo add grafana-community https://grafana-community.github.io/helm-charts --force-update
helm repo add grafana https://grafana.github.io/helm-charts --force-update
helm repo update grafana-community grafana
docker build -t barkly-api:monitoring-dev -f infra/docker/api.Dockerfile .
docker build -t barkly-web:monitoring-dev -f infra/docker/web.Dockerfile .
helm upgrade --install barkly infra/helm/barkly --namespace barkly --create-namespace -f infra/helm/barkly/values-local.yaml --wait --timeout 10m
# Recreate application pods after rebuilding the same local image tags.
kubectl -n barkly rollout restart deployment/barkly-api deployment/barkly-web
kubectl -n barkly rollout status deployment/barkly-api --timeout=5m
kubectl -n barkly rollout status deployment/barkly-web --timeout=5m
helm upgrade --install barkly-prometheus prometheus --repo https://prometheus-community.github.io/helm-charts --version 29.43.0 --namespace monitoring --create-namespace -f infra/local-observability/prometheus-values.yaml --wait --timeout 10m
helm upgrade --install barkly-loki grafana-community/loki --version 21.0.0 --namespace monitoring --create-namespace -f infra/local-observability/loki-values.yaml --wait --timeout 10m
helm upgrade --install barkly-alloy grafana/alloy --version 1.15.0 --namespace monitoring -f infra/local-observability/alloy-values.yaml --wait --timeout 10m
helm upgrade --install barkly-grafana grafana --repo https://grafana-community.github.io/helm-charts --version 14.4.0 --namespace monitoring -f infra/local-observability/grafana-values.yaml --wait --timeout 10m
# Keep the initial Portainer credential outside the repository.
if ! kubectl get namespace portainer >/dev/null 2>&1; then
  kubectl create namespace portainer
fi
if ! kubectl -n portainer get secret portainer-admin-password >/dev/null 2>&1; then
  if helm -n portainer status portainer >/dev/null 2>&1; then
    printf 'Portainer is already installed without the expected password secret; resolve its existing credentials before continuing.\n' >&2
    exit 1
  fi
  openssl rand -base64 24 | tr -d '\n' | kubectl -n portainer create secret generic portainer-admin-password --from-file=password=/dev/stdin
fi
helm upgrade --install portainer portainer --repo https://portainer.github.io/k8s/ --version 245.3.0 --namespace portainer --create-namespace -f infra/local-observability/portainer-values.yaml --wait --timeout 10m
