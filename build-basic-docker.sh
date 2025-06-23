docker stop cytosis_lite || true
docker rm cytosis_lite || true
docker buildx build -t cytosis_lite --platform linux/amd64 --load --progress plain -f docker/basic/Dockerfile .
docker run --network host --name cytosis_lite -d cytosis_lite:latest