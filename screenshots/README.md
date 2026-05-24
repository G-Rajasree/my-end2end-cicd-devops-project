# Screenshots Guide

Add the following screenshots to this folder after running the project.
Then embed them in README.md to prove the project works.

## Screenshots to capture

### 1. jenkins-pipeline-success.png
- Open Jenkins → your pipeline job
- Take screenshot showing all 8 stages green ✅
- Most important screenshot — shows the full CI/CD working

### 2. eks-pods-running.png
- Run: kubectl get pods -n galaxy
- Take screenshot showing 2 pods in Running state
- Shows Kubernetes deployment worked

### 3. app-trending-response.png
- Hit: http://<NLB-URL>/trending?language=java&count=5
- Take screenshot of the JSON response in browser
- Shows the app is live and returning real data

### 4. app-info-response.png
- Hit: http://<NLB-URL>/info
- Take screenshot showing build number matches Jenkins build
- Proves CI/CD deployed the correct version end to end

### 5. grafana-dashboard.png
- Open Grafana at http://<grafana-NLB-URL>
- Take screenshot of the dashboard with Prometheus data
- Shows monitoring stack is working

### 6. sonarqube-quality-gate.png
- Open SonarCloud → your project
- Take screenshot showing Quality Gate: Passed ✅
- Shows code quality enforcement

### 7. jfrog-artifacts.png
- Open JFrog Artifactory → libs-release-local
- Take screenshot showing the published JARs
- Shows artifact management working

### 8. github-actions-passing.png
- Open GitHub → Actions tab
- Take screenshot showing all workflows green ✅

## How to embed in README.md

After adding screenshots, add this section to README.md:

## Screenshots

### Jenkins Pipeline
![Jenkins Pipeline](screenshots/jenkins-pipeline-success.png)

### App Running on EKS
![EKS Pods](screenshots/eks-pods-running.png)

### Live API Response
![API Response](screenshots/app-trending-response.png)

### Grafana Dashboard
![Grafana](screenshots/grafana-dashboard.png)
