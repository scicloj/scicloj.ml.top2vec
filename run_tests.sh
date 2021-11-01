#!/bin/bash

docker build -t scicloj.ml.top2vec --build-arg USER_ID=$(id -u) --build-arg GROUP_ID=$(id -g) .
docker run -it --rm -v $HOME/.m2:/home/user/.m2 -v "$(pwd):/app" -w /app scicloj.ml.top2vec clojure -T:build ci
