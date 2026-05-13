#!/bin/bash

# Android KeyStore Generator for Oskolki
# Usage: ./generate-keystore.sh

KEYSTORE_DIR="keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/oskolki.keystore"
KEY_ALIAS="oskolki"
KEY_PASSWORD="oskolki123"
STORE_PASSWORD="oskolki123"
VALIDITY_DAYS=10000

echo "Generating keystore for Oskolki..."

# Create keystore directory if not exists
mkdir -p "$KEYSTORE_DIR"

# Generate keystore if not exists
if [ ! -f "$KEYSTORE_FILE" ]; then
    keytool -genkey -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keypass "$KEY_PASSWORD" \
        -storepass "$STORE_PASSWORD" \
        -keyalg "RSA" \
        -keysize 2048 \
        -validity "$VALIDITY_DAYS" \
        -dname "CN=Oskolki, OU=Development, O=Oskolki, L=Moscow, ST=Moscow, C=RU"
    
    echo "Keystore generated: $KEYSTORE_FILE"
else
    echo "Keystore already exists: $KEYSTORE_FILE"
fi

# Save properties
cat > keystore.properties <<EOF
storeFile=$KEYSTORE_FILE
storePassword=$STORE_PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASSWORD
EOF

echo "keystore.properties updated"
