variable "vpc_id" {
  type = string
}

variable "allowed_cidr" {
  description = "Your public IP in CIDR notation — restricts SSH access to EKS worker nodes"
  type        = string
}
