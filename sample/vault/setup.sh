#!/bin/bash

# Variablen
VAULT_ADDR="http://127.0.0.1:8200"
VAULT_TOKEN="dein-vault-token"

# Vault-Umgebung setzen
export VAULT_ADDR
export VAULT_TOKEN

# Schritt 1: Erstellen der Root CA (Stufe 1)
vault secrets enable pki
vault write -field=certificate pki/root/generate/internal \
    common_name="My Root CA" \
    ttl=87600h > root_ca.crt

# Schritt 2: Erstellen der Intermediate CA (Stufe 2)
vault secrets enable -path=pki_int pki
vault write -f pki_int/intermediate/generate/internal \
    common_name="My Intermediate CA" \
    ttl=43800h

# Schritt 3: Signieren des CSR der Intermediate CA mit der Root CA
CSR=$(vault write -format=json pki_int/intermediate/generate/internal \
    common_name="My Intermediate CA" \
    ttl=43800h | jq -r '.data.csr')

vault write -format=json pki/root/sign-intermediate \
    csr="$CSR" \
    format=pem_bundle \
    ttl=43800h > intermediate_cert.json

# Zertifikat der Intermediate CA importieren
INTERMEDIATE_CERT=$(jq -r '.data.certificate' intermediate_cert.json)
vault write pki_int/intermediate/set-signed certificate="$INTERMEDIATE_CERT"

# Schritt 4: Erstellen eines Zertifikatsprofils für TLS-End-Entities mit CRL-Endpunkt
vault write pki_int/config/urls \
    issuing_certificates="$VAULT_ADDR/v1/pki_int/ca" \
    crl_distribution_points="$VAULT_ADDR/v1/pki_int/crl"

# Erstellen eines Zertifikatsprofils
vault write pki_int/roles/tls-endpoint \
    allowed_domains="example.com" \
    allow_subdomains=true \
    max_ttl="24h" \
    require_cn=true \
    enforce_hostnames=true \
    allow_bips=true \
    server_flag=true \
    client_flag=false \
    generate_lease=true \
    key_usage="DigitalSignature,KeyEncipherment" \
    ext_key_usage="ServerAuth" \
    # Optional: CRL-Distribution-Points im Zertifikat
    # (Vault setzt dies automatisch anhand der URLs)

# Schritt 5: Anfordern eines TLS-End-Entity-Zertifikats
vault write pki_int/issue/tls-endpoint \
    common_name="server.example.com" \
    alt_names="www.example.com" \
    ttl="24h"

echo "Zertifikatsanfrage abgeschlossen."
