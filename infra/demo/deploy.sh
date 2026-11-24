#!/usr/bin/env bash
set -euo pipefail

# Installed as a forced SSH command; the caller supplies only a Git commit ID.
if [[ ! ${SSH_ORIGINAL_COMMAND:-} =~ ^deploy\ ([0-9a-f]{40})$ ]]; then
  echo 'Expected deploy followed by a full commit SHA.' >&2
  exit 1
fi
commit=${BASH_REMATCH[1]}
exec 9>/run/lock/barkly-deploy.lock
flock -x 9
latest=$(git ls-remote https://github.com/klopucki/barkly.git refs/heads/main)
if [[ ${latest%%[[:space:]]*} != "$commit" ]]; then
  echo 'Skipping deploy: this commit is no longer the tip of main.'
  exit 0
fi
staging=$(mktemp -d /tmp/barkly-deploy.XXXXXXXX)
trap 'rm -rf -- "$staging"' EXIT
tar -xzf - -C "$staging" --no-same-owner --no-same-permissions
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
helm lint "$staging/barkly" -f "$staging/barkly/values-demo.yaml" \
  --set certificate.enabled=false
helm upgrade --install barkly "$staging/barkly" -n barkly \
  -f "$staging/barkly/values-demo.yaml" \
  --set-string "api.image.tag=$commit" --set-string "web.image.tag=$commit" \
  --set-string ingress.tlsSecret=barkly-tls-production \
  --set certificate.enabled=false --wait --timeout 5m
curl --fail --silent --show-error --max-time 15 https://80.211.34.65/ -o /dev/null
curl --fail --silent --show-error --max-time 15 https://80.211.34.65/api/auth/csrf -o /dev/null
