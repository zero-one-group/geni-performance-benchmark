build:
	docker build -f Dockerfile -t r-dplyr .

r: build
	docker run --rm \
		-v /data:/data \
		-v $PWD:/root/dplyr \
		-w /root/dplyr \
		-it r-dplyr
