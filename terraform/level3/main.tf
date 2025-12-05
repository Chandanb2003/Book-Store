terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# ===== VPC Module =====
module "vpc" {
  source  = "terraform-google-modules/network/google"
  version = "~> 7.0"

  project_id   = var.project_id
  network_name = "gke-vpc"

  subnets = [
    {
      subnet_name            = "gke-subnet"
      subnet_ip              = "10.10.0.0/20"
      subnet_region          = var.region
      subnet_private_access  = "true"
      subnet_flow_logs       = "true"
      description            = "GKE subnet"

      # CORRECTED: Object syntax for secondary ranges
      secondary_ip_range = {
        pods     = "10.10.16.0/20"
        services = "10.10.32.0/20"
      }
    }
  ]
}

# ===== GKE Cluster Service Account =====
resource "google_service_account" "gke_sa" {
  account_id   = "gke-service-account"
  display_name = "GKE SA"
}

# Grant Container Admin role to the SA
resource "google_project_iam_member" "container_admin" {
  project = var.project_id
  role    = "roles/container.admin"
  member  = "serviceAccount:${google_service_account.gke_sa.email}"
}

# ===== GKE Cluster Module =====
module "gke" {
  source     = "terraform-google-modules/kubernetes-engine/google"
  version    = "~> 29.0"

  project_id = var.project_id
  name       = "demo-gke"
  region     = var.region

  network    = module.vpc.network_name
  subnetwork = "gke-subnet"

  # VPC-native cluster secondary ranges
  ip_range_pods     = "pods"
  ip_range_services = "services"

  node_pools = [
    {
      name         = "default-pool"
      machine_type = "e2-medium"
      min_count    = 1
      max_count    = 3
    }
  ]

  service_account = google_service_account.gke_sa.email
}

# ===== Outputs =====
output "cluster_name" {
  value = module.gke.name
}

output "endpoint" {
  value     = module.gke.endpoint
  sensitive = true
}

output "sa_email" {
  value = google_service_account.gke_sa.email
}

