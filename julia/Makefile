build:
	docker build -f docker/Dockerfile -t julia-benchmark docker

julia: build
	docker run --rm \
		-v /data:/data \
		-v $PWD:/root/benchmark \
		-w /root/benchmark \
		-it julia-benchmark
