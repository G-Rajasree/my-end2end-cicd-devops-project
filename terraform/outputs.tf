output "jenkins_master_ip" {
  value       = { for k, v in aws_instance.demo-server : k => v.public_ip }
  description = "Public IPs of all EC2 instances — update ansible/hosts with these"
}

output "vpc_id" {
  value = aws_vpc.dpp-vpc.id
}

output "eks_endpoint" {
  value = module.eks.endpoint
}

output "eks_cluster_name" {
  value       = module.eks.cluster_name
  description = "Run: aws eks update-kubeconfig --region us-east-1 --name <this value>"
}
