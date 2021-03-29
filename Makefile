# Makefile used to build docker images for kafka-specs

PROJECT_VERSION := $(shell ./gradlew printVersion -q)
GIT_COMMIT := $(shell git rev-parse --short HEAD)
GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD)

REPOSITORY = streamthoughts
IMAGE = kafka-specs

.SILENT:

all: build-images clean-build

clean-containers:
	echo "Cleaning containers \n========================================== ";

clean-images:
	echo "Cleaning images \n========================================== ";
	for image in `docker images -qf "label=io.streamthoughts.docker.name"`; do \
	    echo "Removing image $${image} \n==========================================\n " ; \
        docker rmi -f $${image} || exit 1 ; \
    done

clean-build:
	echo "Cleaning build directory \n========================================== ";
	rm -rf ./docker-build;

build-images:
	echo "Building image \n========================================== ";
	echo "PROJECT_VERSION="$(PROJECT_VERSION)
	echo "GIT_COMMIT="$(GIT_COMMIT)
	echo "GIT_BRANCH="$(GIT_BRANCH)
	echo "==========================================\n "
	./gradlew clean distZip && \
	docker build \
	--build-arg kafkaSpecsVersion=${PROJECT_VERSION} \
	--build-arg kafkaSpecsCommit=${GIT_COMMIT} \
	--build-arg kafkaSpecsBranch=${GIT_BRANCH} \
    -f Dockerfile \
	-t ${REPOSITORY}/${IMAGE}:latest . || exit 1 ;
	docker tag ${REPOSITORY}/${IMAGE}:latest ${REPOSITORY}/${IMAGE}:${PROJECT_VERSION} || exit 1 ;

clean: clean-containers clean-images clean-build
