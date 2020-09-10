build:
	docker build -f Dockerfile -t r-benchmark .

r: build
	docker run --rm \
		-v /data:/data \
		-v $PWD:/root/benchmark \
		-w /root/benchmark \
		-it r-benchmark
