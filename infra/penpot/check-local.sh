#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

warn() { echo "[WARNING] $*"; }
info() { echo "[INFO] $*"; }
ok() { echo "[OK] $*"; }

MISSING=0

info "Checking local requirements for Penpot..."

if ! command -v docker >/dev/null 2>&1; then
  warn "Docker CLI non trovato. Installa Docker Desktop (include Docker Engine + Compose plugin)."
  warn "Download: https://www.docker.com/products/docker-desktop/"
  MISSING=1
else
  ok "Docker CLI trovato: $(docker --version 2>/dev/null || echo 'versione non rilevata')"
fi

if command -v docker >/dev/null 2>&1; then
  if ! docker compose version >/dev/null 2>&1; then
    warn "Docker Compose plugin non disponibile. Aggiorna Docker Desktop all'ultima versione."
    MISSING=1
  else
    ok "Docker Compose disponibile: $(docker compose version 2>/dev/null | head -n 1)"
  fi
fi

if command -v docker >/dev/null 2>&1; then
  DOCKER_CONTEXT="$(docker context show 2>/dev/null || true)"
  if [[ -n "$DOCKER_CONTEXT" ]]; then
    info "Docker context: $DOCKER_CONTEXT"
  fi
  if ! docker info >/dev/null 2>&1; then
    warn "Docker Engine non raggiungibile sul contesto ${DOCKER_CONTEXT:-corrente}. Avvia Docker Desktop o verifica il socket Docker, poi riprova."
    MISSING=1
  else
    ok "Docker Engine attivo"
  fi
fi

if [[ ! -f .env ]]; then
  warn "File .env mancante in $ROOT_DIR"
  warn "Esegui: cp .env.example .env"
  MISSING=1
else
  ok ".env trovato"

  PUB_URI="$(grep '^PENPOT_PUBLIC_URI=' .env | cut -d'=' -f2- || true)"
  if [[ -z "${PUB_URI}" ]]; then
    warn "PENPOT_PUBLIC_URI non configurata in .env"
    MISSING=1
  elif [[ "$PUB_URI" != "http://localhost:9001" ]]; then
    warn "PENPOT_PUBLIC_URI attuale: $PUB_URI"
    warn "Il profilo locale richiede: PENPOT_PUBLIC_URI=http://localhost:9001"
    MISSING=1
  else
    ok "PENPOT_PUBLIC_URI impostata per uso locale"
  fi

  if grep -q '^PENPOT_SECRET_KEY=CHANGE_ME' .env 2>/dev/null; then
    warn "PENPOT_SECRET_KEY è ancora placeholder. Genera una secret reale (openssl rand -hex 32)."
    MISSING=1
  fi

  if grep -q '^POSTGRES_PASSWORD=CHANGE_ME' .env 2>/dev/null; then
    warn "POSTGRES_PASSWORD è ancora placeholder. Imposta una password forte."
    MISSING=1
  fi

  if grep -Eq '^PENPOT_(SECRET_KEY|MCP_KEY)=(CHANGE_ME|REPLACE_WITH)' .env 2>/dev/null; then
    warn "Un valore Penpot è ancora un placeholder. Genera/configura i segreti prima dell'avvio."
    MISSING=1
  fi

  if grep -Eq '^(PENPOT_SMTP_(DEFAULT_FROM|DEFAULT_REPLY_TO|HOST|USERNAME|PASSWORD))=(smtp\.example\.com|your_smtp_user|your_smtp_password|no-reply@example\.com)' .env 2>/dev/null; then
    warn "Le credenziali SMTP sono ancora di esempio. Lasciale vuote nel profilo locale oppure configurale con valori reali."
    MISSING=1
  fi
fi

if command -v lsof >/dev/null 2>&1; then
  if lsof -i :9001 >/dev/null 2>&1; then
    warn "La porta 9001 risulta occupata. Cambia mapping in docker-compose.local.yml o libera la porta."
    MISSING=1
  else
    ok "Porta 9001 libera"
  fi
else
  warn "`lsof` non trovato: impossibile verificare automaticamente la porta 9001."
fi

echo
if [[ "$MISSING" -eq 1 ]]; then
  warn "Requisiti non completi: installa/configura quanto indicato sopra prima dell'avvio."
  exit 1
fi

ok "Controlli completati: ambiente pronto per l'avvio locale."
