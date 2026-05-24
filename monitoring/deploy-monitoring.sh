#!/bin/bash
set -e

echo ">>> Deploying Prometheus..."
kubectl apply -f prometheus.yaml

echo ">>> Waiting for Prometheus rollout..."
kubectl rollout status deployment/prometheus -n monitoring --timeout=120s

echo ">>> Deploying Grafana..."
kubectl apply -f grafana.yaml

echo ">>> Waiting for Grafana rollout..."
kubectl rollout status deployment/grafana -n monitoring --timeout=120s

echo ">>> Monitoring stack deployed."
echo ">>> Grafana URL:"
kubectl get svc grafana-service -n monitoring
