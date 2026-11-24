# Single-server demo

This configuration is for the demo server. It uses one K3s node, local volumes,
one API replica, a separately provisioned S3-compatible bucket, and a certificate
managed by cert-manager. The local-development Helm values remain separate.
The cluster-local databases and monitoring services are not public. Local
volumes are not backups; losing the server can lose the demo data.

## Deployments

After CI publishes the API and web images for a push to `main`, the deployment
job sends this chart over SSH to the forced command installed from `deploy.sh`.
The server accepts a full commit SHA, checks that it is still the tip of `main`,
installs both images with that SHA, waits for Helm and checks HTTPS. Deployments
are serialized. GitHub Actions requires the `DEMO_DEPLOY_SSH_KEY` and
`DEMO_DEPLOY_KNOWN_HOSTS` secrets. The SSH key can deploy arbitrary chart content
and therefore must be treated as highly privileged despite its forced command.
Changing `deploy.sh` in Git does not automatically replace the copy installed
on the server; update and verify it separately.

The certificate is currently managed separately from the Barkly Helm release:
`barkly-ip-production` and `barkly-acme-production` in namespace `barkly`.
The deployment references `barkly-tls-production`, and must not replace it with
the staging certificate. The Let’s Encrypt IP certificate is short-lived and
its renewal depends on cert-manager and HTTP validation remaining operational.

## Monitoring

Prometheus keeps three days of metrics. Grafana is available only inside the
cluster. From the server run:

```bash
k3s kubectl -n monitoring port-forward --address 127.0.0.1 svc/barkly-grafana 3000:80
```

In a second terminal on your computer, forward that port over SSH:

```bash
ssh -i ~/.ssh/barkly_deploy_codex -L 3000:127.0.0.1:3000 root@80.211.34.65
```

Open http://127.0.0.1:3000 on your computer. The Grafana username is `admin`;
the password is stored in `/root/barkly-secrets/grafana-password` on the server.
Keep both terminal sessions open while using the dashboard. No alert delivery
is configured; the dashboard must be checked manually.
