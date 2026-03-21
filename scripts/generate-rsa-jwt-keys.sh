#!/usr/bin/env bash
# Генерирует пару RSA PEM для JWT (Spring Security / Nimbus), как в rsa.* в application.yml.
# Требуется OpenSSL 1.1.1+ или 3.x.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEST="${1:-$ROOT_DIR/src/main/resources/certs}"

mkdir -p "$DEST"
PRIVATE="$DEST/private.pem"
PUBLIC="$DEST/public.pem"

if [[ -f "$PRIVATE" || -f "$PUBLIC" ]]; then
  echo "Уже есть $PRIVATE или $PUBLIC — удалите файлы вручную или укажите другой каталог:" >&2
  echo "  $0 /path/to/certs" >&2
  exit 1
fi

openssl genpkey -algorithm RSA -out "$PRIVATE" -pkeyopt rsa_keygen_bits:2048
openssl pkey -in "$PRIVATE" -pubout -out "$PUBLIC"
chmod 600 "$PRIVATE"
chmod 644 "$PUBLIC"

echo "Записано:"
echo "  $PRIVATE"
echo "  $PUBLIC"
