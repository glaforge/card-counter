set dotenv-load

default: run

run:
    ./gradlew -t run

docker-build:
    docker build . --platform linux/amd64 --tag europe-west1-docker.pkg.dev/genai-java-demos/containers/cardcounter

docker-run:
    docker run --rm -it -p 8080:8080 europe-west1-docker.pkg.dev/genai-java-demos/containers/cardcounter

docker-push:
    docker push europe-west1-docker.pkg.dev/genai-java-demos/containers/cardcounter

build:
    gcloud builds submit -t $CLOUD_REGION-docker.pkg.dev/$PROJECT_ID/containers/$CONTAINER_NAME:v1

deploy: build
    gcloud run deploy $CONTAINER_NAME --region=$CLOUD_REGION --cpu=$CPU --memory=$MEMORY --image=$CLOUD_REGION-docker.pkg.dev/$PROJECT_ID/containers/$CONTAINER_NAME:v1