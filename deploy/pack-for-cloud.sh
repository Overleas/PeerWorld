#!/bin/bash
set -e

DEST="/home/overleas/Стільниця/sharedworld-cloud-deploy.tar.gz"
echo "Пакування хмарного сервера у $DEST..."

cd /home/overleas/.gemini/antigravity/scratch/sharedworld-offline/deploy/docker
tar -czf "$DEST" Dockerfile swcore data/

echo "Готово! Архів створено на Стільниці: sharedworld-cloud-deploy.tar.gz"
