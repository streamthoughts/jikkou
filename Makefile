# Makefile used to build docker images for kafka-specs

VERSION := $(shell mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)
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
	echo "VERSION="$(VERSION);
	echo "GIT_COMMIT="$(GIT_COMMIT);
	echo "GIT_BRANCH="$(GIT_BRANCH);
	echo "\n==========================================\n";

clean-build:
	echo "Cleaning build directory \n========================================== ";
	rm -rf ./docker-build;

build-dist: print-info
	./mvnw clean -ntp -B --file ./pom.xml package -Pnative

build-images: build-dist
	cp ./jikkou-cli/target/jikkou-cli-${VERSION}-runner ./docker/jikkou-cli-${VERSION}-runner
	docker build --compress \
	--build-arg jikkouVersion=${VERSION} \
	--build-arg jikkouCommit=${GIT_COMMIT} \
	--build-arg jikkouBranch=${GIT_BRANCH} \
	--rm \
        -f ./docker/Dockerfile \
	-t ${REPOSITORY}/${IMAGE}:${VERSION} ${DOCKER_PATH} || exit 1 ;
	
	docker tag ${REPOSITORY}/${IMAGE}:${VERSION} ${REPOSITORY}/${IMAGE}:${GIT_BRANCH} || exit 1 ;

docker-tag-latest: build-images
	docker tag ${REPOSITORY}/${IMAGE}:${VERSION} ${REPOSITORY}/${IMAGE}:latest || exit 1 ;

changelog:
	./mvnw jreleaser:changelog -Prelease -f ./jikkou-cli/pom.xml

install:
	./mvnw clean package -Pnative,deb -DskipTests 
	sudo dpkg -i ./dist/jikkou-${VERSION}-linux-x86_64.deb

clean: clean-containers clean-images clean-build
