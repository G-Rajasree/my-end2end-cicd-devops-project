variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "ami_id" {
  type = string
}

variable "instance_type" {
  description = "EC2 instance type for Jenkins master and build slave — minimum t3.medium for Jenkins"
  type        = string
  default     = "t3.medium"
}

variable "key_name" {
  type = string
}

variable "vpc_cidr" {
  type    = string
  default = "10.1.0.0/16"
}

variable "public_subnet_01_cidr" {
  type    = string
  default = "10.1.1.0/24"
}

variable "public_subnet_02_cidr" {
  type    = string
  default = "10.1.2.0/24"
}

variable "allowed_cidr" {
  description = "Your public IP in CIDR notation (e.g. 203.0.113.10/32) — restricts SSH and Jenkins access"
  type        = string
}
