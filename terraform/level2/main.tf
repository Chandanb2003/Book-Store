provider "google" {
  project = var.project_id
  region  = var.region
}

module "vpc" {
  source  = "terraform-google-modules/network/google"
  version = "~> 7.0"

  project_id   = var.project_id
  network_name = "demo-vpc"

  subnets = [
    {
      subnet_name   = "demo-subnet"
      subnet_ip     = "10.0.1.0/24"
      subnet_region = var.region
    }
  ]
}

resource "google_compute_firewall" "allow_ssh" {
  name    = "allow-ssh"
  network = module.vpc.network_name

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["0.0.0.0/0"]
}

resource "google_compute_instance" "vm" {
  name         = "demo-vm"
  machine_type = "e2-medium"
  zone         = var.zone

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }

  network_interface {
    subnetwork   = module.vpc.subnets["${var.region}/demo-subnet"].self_link
    access_config {}
  }
}

