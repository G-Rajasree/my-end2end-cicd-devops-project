provider "aws" {
  region = var.aws_region
}

resource "aws_instance" "demo-server" {
  for_each               = toset(["jenkins-master", "build-slave", "ansible"])
  ami                    = var.ami_id
  instance_type          = each.key == "ansible" ? "t3.micro" : var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.demo-sg.id]
  subnet_id              = each.key == "build-slave" ? aws_subnet.dpp-public-subnet-02.id : aws_subnet.dpp-public-subnet-01.id
  monitoring             = true

  root_block_device {
    volume_size = each.key == "build-slave" ? 30 : 20
    encrypted   = true
  }

  tags = {
    Name = each.key
  }
}

resource "aws_security_group" "demo-sg" {
  name        = "demo-sg"
  description = "SSH and Jenkins access"
  vpc_id      = aws_vpc.dpp-vpc.id

  ingress {
    description = "SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_cidr]
  }

  ingress {
    description = "Jenkins port access"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [var.allowed_cidr]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = {
    Name = "demo-sg"
  }
}

resource "aws_vpc" "dpp-vpc" {
  cidr_block = var.vpc_cidr
  tags = {
    Name = "dpp-vpc"
  }
}

resource "aws_subnet" "dpp-public-subnet-01" {
  vpc_id                  = aws_vpc.dpp-vpc.id
  cidr_block              = var.public_subnet_01_cidr
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true
  tags = {
    Name = "dpp-public-subnet-01"
  }
}

resource "aws_subnet" "dpp-public-subnet-02" {
  vpc_id                  = aws_vpc.dpp-vpc.id
  cidr_block              = var.public_subnet_02_cidr
  availability_zone       = "${var.aws_region}b"
  map_public_ip_on_launch = true
  tags = {
    Name = "dpp-public-subnet-02"
  }
}

resource "aws_internet_gateway" "dpp-igw" {
  vpc_id = aws_vpc.dpp-vpc.id
  tags = {
    Name = "dpp-igw"
  }
}

resource "aws_route_table" "dpp-public-rt" {
  vpc_id = aws_vpc.dpp-vpc.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.dpp-igw.id
  }
}

resource "aws_route_table_association" "dpp-rta-public-subnet-01" {
  subnet_id      = aws_subnet.dpp-public-subnet-01.id
  route_table_id = aws_route_table.dpp-public-rt.id
}

resource "aws_route_table_association" "dpp-rta-public-subnet-02" {
  subnet_id      = aws_subnet.dpp-public-subnet-02.id
  route_table_id = aws_route_table.dpp-public-rt.id
}

module "sgs" {
  source       = "./sg-eks"
  vpc_id       = aws_vpc.dpp-vpc.id
  allowed_cidr = var.allowed_cidr
}

module "eks" {
  source       = "./eks"
  vpc_id       = aws_vpc.dpp-vpc.id
  subnet_ids   = [aws_subnet.dpp-public-subnet-01.id, aws_subnet.dpp-public-subnet-02.id]
  sg_ids       = module.sgs.security_group_public
  key_name     = var.key_name
  allowed_cidr = var.allowed_cidr
}
