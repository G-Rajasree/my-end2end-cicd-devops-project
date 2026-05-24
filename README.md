# Galaxy DevOps — AWS CI/CD Pipeline Project

![CI Pipeline](https://github.com/G-Rajasree/my-custom-cicd-project/actions/workflows/ci.yml/badge.svg)
![Security Scan](https://github.com/G-Rajasree/my-custom-cicd-project/actions/workflows/security.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)
![Terraform](https://img.shields.io/badge/Terraform-1.5+-purple?logo=terraform)
![AWS EKS](https://img.shields.io/badge/AWS-EKS-yellow?logo=amazonaws)
![Helm](https://img.shields.io/badge/Helm-3-blue?logo=helm)
![Docker](https://img.shields.io/badge/Docker-eclipse--temurin%3A21-blue?logo=docker)
![License](https://img.shields.io/badge/License-MIT-green)

A production-grade CI/CD pipeline that builds, tests, and deploys a Java 21 Spring Boot application to AWS EKS using a full DevOps toolchain. The application exposes **GitHub Trending Repositories** via a REST API — no external credentials required, works live instantly.

---

## Architecture

```
Developer Push (GitHub)
        │
        ├──► GitHub Actions (CI — runs on every push/PR)
        │         ├── Maven Build + Unit Tests
        │         ├── SonarCloud Analysis
        │         ├── Docker Build Verification
        │         └── Terraform Validate
        │
        ▼
Jenkins CI/CD Pipeline  (AWS EC2 - jenkins-master)
        │
        ├── 1. Maven Build          (build-slave EC2)
        ├── 2. JUnit Tests          (surefire + jacoco)
        ├── 3. SonarQube Analysis   (code quality gate)
        ├── 4. JFrog Artifactory    (JAR artifact storage)
        ├── 5. Docker Build & Push  (JFrog Docker Registry)
        └── 6. Helm Deploy to EKS    (helm upgrade --install + auto-rollback)
                        │
                        ▼
              AWS EKS Cluster (galaxy-eks-01)
              ┌──────────────────────────────────┐
              │  Namespace: galaxy               │
              │  ├── Deployment (2 pods)         │
              │  ├── Service (AWS NLB)           │
              │  └── HPA (2–5 replicas, 70% CPU) │
              └──────────────────────────────────┘
                        │
                        ▼
              Namespace: monitoring
              ├── Prometheus (RBAC + pod scraping)
              └── Grafana    (PVC + NLB + dashboards)
```

---

## Tech Stack

| Layer                   | Tool                          |
|-------------------------|-------------------------------|
| Cloud                   | AWS (EC2, EKS, VPC, S3)       |
| IaC                     | Terraform >= 1.5              |
| Configuration Mgmt      | Ansible                       |
| CI (modern)             | GitHub Actions                |
| CI/CD (Jenkins)         | Jenkins                       |
| Build                   | Maven 3.9.16                  |
| Code Quality            | SonarQube / SonarCloud        |
| Artifact Registry       | JFrog Artifactory             |
| Containerization        | Docker (eclipse-temurin:21)   |
| Container Orchestration | AWS EKS (Kubernetes)          |
| Helm                    | Helm 3 (parameterised deploy) |
| Monitoring              | Prometheus + Grafana          |
| Security Scanning       | Trivy + OWASP Dependency Check|
| Language                | Java 21 / Spring Boot 3.2.5   |

---

## Project Structure

```
my-custom-cicd-project/
├── .github/
│   └── workflows/
│       ├── ci.yml              # GitHub Actions: build, test, sonar, docker, terraform validate
│       └── security.yml        # GitHub Actions: Trivy image scan + OWASP dependency check
├── ansible/
│   ├── hosts                   # Inventory — update IPs after terraform apply
│   ├── jenkins-master.yaml     # Installs Jenkins on master EC2
│   └── jenkins-slave.yaml      # Installs Java, Maven, Docker, kubectl, Helm, AWS CLI
├── jenkins/
│   └── Jenkinsfile             # Full CI/CD pipeline
├── helm/
│   └── github-trending/        # Helm chart — replaces raw kubectl manifests
│       ├── Chart.yaml
│       ├── values.yaml          # Image tag, replicas, resources, HPA — all parameterised
│       └── templates/
│           ├── namespace.yaml
│           ├── deployment.yaml
│           ├── service.yaml
│           └── hpa.yaml
├── kubernetes/
│   ├── namespace.yaml          # galaxy namespace (kept for reference)
│   ├── secret.yaml             # Instructions to create JFrog pull secret manually
│   ├── deployment.yaml         # Raw manifest (kept for reference)
│   ├── service.yaml            # AWS NLB LoadBalancer service
│   ├── hpa.yaml                # HPA: 2–5 replicas at 70% CPU
│   └── deploy.sh               # Legacy deploy script (superseded by Helm)
├── monitoring/
│   ├── prometheus.yaml         # Prometheus + RBAC + ConfigMap
│   ├── grafana.yaml            # Grafana + PVC + Secret + NLB service
│   └── deploy-monitoring.sh    # One-command monitoring deploy
├── terraform/
│   ├── bootstrap/
│   │   └── main.tf             # Run ONCE: S3 bucket + DynamoDB for remote state
│   ├── eks/
│   │   ├── eks.tf              # EKS cluster + node group + IAM roles
│   │   ├── variables.tf
│   │   └── output.tf
│   ├── sg-eks/
│   │   ├── sg-eks.tf           # Security group for EKS worker nodes
│   │   ├── variables.tf
│   │   └── output.tf
│   ├── backend.tf              # S3 remote state backend
│   ├── ec2-with-vpc.tf         # VPC, subnets, EC2 instances, IGW, route tables
│   ├── variables.tf
│   ├── terraform.tfvars        # Your values (gitignored — copy from .example)
│   ├── terraform.tfvars.example
│   └── outputs.tf
└── github-trending/            # Java Spring Boot application source
    ├── src/
    │   ├── main/java/com/galaxy/demo/
    │   │   ├── GitHubTrendingApplication.java
    │   │   └── controller/GitHubTrendingController.java
    │   └── resources/application.properties
    └── test/java/com/galaxy/demo/
        ├── GitHubTrendingApplicationTests.java
        └── GitHubTrendingControllerTest.java
    ├── pom.xml                 # Java 21, Spring Boot 3.2.5, Actuator, Prometheus
    └── sonar-project.properties
```

---

## Prerequisites

- AWS account with IAM user (AdministratorAccess)
- AWS CLI configured: `aws configure`
- Terraform >= 1.5
- Ansible on your local machine
- EC2 key pair named `dpp` in `us-east-1`
- JFrog Artifactory account (free tier works)
- SonarCloud account (free tier works)

---

## Step-by-Step Setup

### Step 1 — Bootstrap Terraform Remote State (Run Once)

```bash
cd terraform/bootstrap
terraform init
terraform apply
```

Creates S3 bucket (`galaxy-terraform-state-bucket`) and DynamoDB table (`galaxy-terraform-lock`).

---

### Step 2 — Configure Terraform Variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your AMI ID and key pair name
```

---

### Step 3 — Provision AWS Infrastructure

```bash
cd terraform
terraform init
terraform plan
terraform apply
terraform output   # Note the EC2 IPs and EKS endpoint
```

Creates:
- VPC with 2 public subnets across 2 AZs
- 3 EC2 instances: `jenkins-master`, `build-slave`, `ansible`
- Security groups (SSH + port 8080)
- EKS cluster `galaxy-eks-01` with managed node group (2–3 x t3.medium)

---

### Step 4 — Configure EC2 Instances with Ansible

Update `ansible/hosts` with the public IPs from Step 3 output.

```bash
ansible-playbook -i ansible/hosts ansible/jenkins-master.yaml
ansible-playbook -i ansible/hosts ansible/jenkins-slave.yaml
```

---

### Step 5 — Configure kubectl on Build Slave

```bash
aws eks update-kubeconfig --region us-east-1 --name galaxy-eks-01
kubectl get nodes
```

---

### Step 6 — Create JFrog Image Pull Secret

```bash
kubectl apply -f kubernetes/namespace.yaml

kubectl create secret docker-registry jfrogcred \
  --docker-server=galaxy01.jfrog.io \
  --docker-username=<jfrog-username> \
  --docker-password=<jfrog-api-token> \
  --namespace=galaxy
```

---

### Step 7 — Configure Jenkins

1. Access Jenkins at `http://<jenkins-master-ip>:8080`
2. Install plugins: Git, Maven Integration, SonarQube Scanner, Artifactory, Docker Pipeline
3. Add credentials:

| Credential ID       | Type        | Value           |
|---------------------|-------------|-----------------|
| `artifactory_token` | Secret text | JFrog API token |

4. `Manage Jenkins > Configure System` → Add SonarQube server named `galaxy-SonarQube-Server`
5. `Manage Jenkins > Global Tool Configuration` → Add Maven named `maven` at `/opt/apache-maven-3.9.16`
6. Add agent node with label `maven-agent` pointing to `build-slave` EC2

---

### Step 8 — Configure GitHub Actions Secrets

In your GitHub repo → Settings → Secrets → Actions, add:

| Secret Name          | Value                        |
|----------------------|------------------------------|
| `SONAR_TOKEN`        | SonarCloud user token        |
| `SONAR_PROJECT_KEY`  | SonarCloud project key       |
| `SONAR_ORGANIZATION` | SonarCloud organization name |

---

### Step 9 — Run the Jenkins Pipeline

1. New Item → Pipeline → Pipeline script from SCM → Git
2. Script path: `jenkins/Jenkinsfile`
3. Save and Build

Pipeline stages:
```
Build → Unit Test → SonarQube Analysis → Quality Gate → Jar Publish → Docker Build → Docker Publish → Helm Deploy to EKS
```

---

### Step 10 — Deploy Monitoring Stack

```bash
# Edit monitoring/grafana.yaml and replace <GRAFANA_ADMIN_PASSWORD> first
cd monitoring
bash deploy-monitoring.sh

kubectl get svc grafana-service -n monitoring
# Open EXTERNAL-IP:80 in browser — login: admin / <your-password>
```

Add Prometheus data source in Grafana:
- URL: `http://prometheus-service.monitoring.svc.cluster.local:9090`

---

## API Endpoints

| Endpoint                                           | Description                               |
|----------------------------------------------------|-------------------------------------------|
| `GET /`                                            | App name, version, build number, status   |
| `GET /trending`                                    | Top 10 trending GitHub repos (weekly)     |
| `GET /trending?language=java&count=5&period=daily` | Trending repos filtered by language       |
| `GET /info`                                        | Build info — version, build number, date  |
| `GET /actuator/health`                             | Kubernetes liveness/readiness probe       |
| `GET /actuator/prometheus`                         | Prometheus metrics scrape endpoint        |

### Example Response — `GET /trending?language=java&count=3`

```json
[
  {
    "name": "spring-projects/spring-boot",
    "description": "Spring Boot",
    "stars": 73000,
    "language": "Java",
    "url": "https://github.com/spring-projects/spring-boot",
    "forks": 40000
  }
]
```

---

## Security Practices

- No hardcoded secrets anywhere in the codebase
- SSH, Jenkins (8080), and EKS API endpoint restricted to a single IP via `allowed_cidr` Terraform variable
- Kubernetes Secrets used for JFrog pull credentials
- Terraform state encrypted in S3 + DynamoDB state locking
- EBS volumes encrypted on all EC2 instances
- EC2 detailed monitoring enabled
- EKS private endpoint access enabled
- DynamoDB Point-In-Time Recovery enabled
- JFrog pull secret created via `kubectl create secret` (not committed)
- `.gitignore` excludes `*.tfstate`, `.terraform/`, `*.pem`, `*.env`
- Resource limits set on all containers
- Trivy Docker image scanning on every push
- OWASP Dependency Check runs weekly

---

## Cleanup

```bash
kubectl delete namespace galaxy
kubectl delete namespace monitoring

cd terraform && terraform destroy
cd terraform/bootstrap && terraform destroy
```
