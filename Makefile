# Makefile used to build docker images for kafka-specs

PROJECT_VERSION := $(shell ./gradlew printVersion -q)
GIT_COMMIT := $(shell git rev-parse --short HEAD)
GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD)

REPOSITORY = streamthoughts
IMAGE = jikkou
DOCKER_PATH=./docker

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

print-info:
	echo "\n==========================================\n";
	echo "CONNECT_VERSION="$(PROJECT_VERSION);
	echo "GIT_COMMIT="$(GIT_COMMIT);
	echo "GIT_BRANCH="$(GIT_BRANCH);
	echo "\n==========================================\n";

clean-build:
	echo "Cleaning build directory \n========================================== ";
	rm -rf ./docker-build;

build-dist: print-info
	./gradlew clean distZip

build-images: build-dist
	cp ./build/distributions/jikkou-${PROJECT_VERSION}.zip ./docker/jikkou-${PROJECT_VERSION}.zip 
	docker build --compress \
	--build-arg jikkouVersion=${PROJECT_VERSION} \
	--build-arg jikkouCommit=${GIT_COMMIT} \
	--build-arg jikkouBranch=${GIT_BRANCH} \
	--rm \
        -f ./docker/Dockerfile \
	-t ${REPOSITORY}/${IMAGE}:${PROJECT_VERSION} ${DOCKER_PATH} || exit 1 ;
	
	docker tag ${REPOSITORY}/${IMAGE}:${PROJECT_VERSION} ${REPOSITORY}/${IMAGE}:${GIT_BRANCH} || exit 1 ;

docker-tag-latest: build-images
	docker tag ${REPOSITORY}/${IMAGE}:${PROJECT_VERSION} ${REPOSITORY}/${IMAGE}:latest || exit 1 ;

clean: clean-containers clean-images clean-build
