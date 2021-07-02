#!/bin/bash

echo "generating key" 
openssl genrsa -des3 -out server.key 1024

echo "generating csr"
openssl req -new -key server.key -out server.csr

echo "generating self-signed certificate signed by key"
openssl x509 -req -days 3650 -in server.csr -signkey server.key -out server.crt

echo "generating key-store containing self-signed certificate signed by key"
openssl pkcs12 -export -in server.crt -inkey server.key -name "server" -out keystore.p12

echo "generating trust-store containing self-signed certificate signed by key"
openssl pkcs12 -export -in server.crt -inkey server.key -name "server" -out truststore.jks