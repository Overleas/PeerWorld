FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    sqlite3 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY swcore /app/swcore
RUN chmod +x /app/swcore

RUN mkdir -p /app/data/blobs

ENV PORT=7860
ENV SHAREDWORLD_INTEGRATION_DB_FILE=/app/data/sharedworld.db
ENV SHAREDWORLD_INTEGRATION_BLOB_DIR=/app/data/blobs
ENV SW_ALLOW_DEV_AUTH=true
ENV SW_DEV_AUTH_SECRET=dev-secret
ENV SW_ALLOW_DEV_INSECURE_E4MC=true

EXPOSE 7860

CMD ["sh", "-c", "/app/swcore --dev --listen 0.0.0.0:${PORT:-7860} --db /app/data/sharedworld.db"]
