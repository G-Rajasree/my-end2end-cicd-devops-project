#!/bin/bash
set -e

echo ">>> Applying namespace..."
kubectl apply -f namespace.yaml

echo ">>> Applying deployment..."
kubectl apply -f deployment.yaml

echo ">>> Applying service..."
kubectl apply -f service.yaml

echo ">>> Applying HPA..."
kubectl apply -f hpa.yaml

echo ">>> Waiting for rollout to complete..."
kubectl rollout status deployment/github-trending -n galaxy --timeout=120s

echo ""
echo ">>> Deployment complete. Service details:"
kubectl get svc github-trending-service -n galaxy
