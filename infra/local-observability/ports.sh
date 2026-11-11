#!/usr/bin/env bash
set -euo pipefail

expected_context="${BARKLY_KUBE_CONTEXT:-rancher-desktop}"
actual_context="$(kubectl config current-context)"
if [[ "$actual_context" != "$expected_context" ]]; then
  printf 'Refusing to connect: current context is %s, expected %s.\n' "$actual_context" "$expected_context" >&2
  exit 1
fi

pids=()
forward() {
  local description="$1" namespace="$2" service="$3" mapping="$4"
  local local_port="${mapping%%:*}"
  if ss -H -ltn "sport = :${local_port}" | rg -q .; then
    printf '%s: port %s is already in use; keeping the existing listener.\n' "$description" "$local_port"
    return
  fi
  kubectl -n "$namespace" port-forward --address 127.0.0.1 "service/${service}" "$mapping" &
  pids+=("$!")
}
cleanup() {
  for pid in "${pids[@]}"; do kill "$pid" 2>/dev/null || true; done
}
trap cleanup EXIT INT TERM

forward Barkly kube-system traefik 8088:80
forward Grafana monitoring barkly-grafana 3001:80
forward Portainer portainer portainer 9443:9443
forward Prometheus monitoring barkly-prometheus-server 19091:80
printf 'Barkly: http://localhost:8088\nGrafana: http://localhost:3001\nPortainer: https://localhost:9443\nPrometheus: http://localhost:19091\nPress Ctrl+C to stop forwarding.\n'
if ((${#pids[@]})); then
  wait -n "${pids[@]}"
fi
