terraform {
  backend "s3" {
    bucket         = "galaxy-terraform-state-bucket"
    key            = "cicd/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "galaxy-terraform-lock"
    encrypt        = true
  }
}
