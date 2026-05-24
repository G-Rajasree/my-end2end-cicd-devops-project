variable "sg_ids" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "vpc_id" {
  type = string
}

variable "key_name" {
  type    = string
  default = "dpp"
}

variable "allowed_cidr" {
  description = "Your public IP in CIDR notation — restricts EKS public endpoint access"
  type        = string
}
