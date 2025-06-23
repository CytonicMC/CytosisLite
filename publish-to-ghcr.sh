docker buildx build --push \
  -t ghcr.io/cytonicmc/cytosis_lite:latest \
  --platform linux/arm64/v8,linux/amd64 \
  --progress plain -f docker/basic/Dockerfile .