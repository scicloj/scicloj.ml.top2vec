# [Choice] Java version (use -bullseye variants on local arm64/Apple Silicon): 11, 17, 11-bullseye, 17-bullseye, 11-buster, 17-buster
ARG VARIANT=17
FROM mcr.microsoft.com/devcontainers/java:${VARIANT}

# [Optional] Clojure version
ARG CLOJURE_VERSION=1.11.1

# [Optional] Clojure tools version
ARG CLOJURE_CLI_VERSION=1.11.1.1208

# [Optional] Leiningen version
ARG LEININGEN_VERSION="stable"

# [Optional] POLYLITH version
ARG POLYLITH_VERSION="0.2.13-alpha"

# [Optional] Boot version
ENV BOOT_VERSION=2.8.3

# [Optional] Clojure version used by Boot
ENV BOOT_CLOJURE_VERSION=${CLOJURE_VERSION}

# [Option] Install Clojure CLI tool
ARG INSTALL_CLOJURE_CLI="true"

# [Option] Install Boot
ARG INSTALL_BOOT="false"

# [Option] Install Leiningen
ARG INSTALL_LEININGEN="false"

# [Option] Install Polylith
ARG INSTALL_POLYLITH="false"

RUN if [ "${INSTALL_CLOJURE_CLI}" = "true" ]; then \
    apt-get update \
    && apt-get -y install rlwrap \
    && curl -OL "https://download.clojure.org/install/linux-install-${CLOJURE_CLI_VERSION}.sh" \
    && chmod +x linux-install-${CLOJURE_CLI_VERSION}.sh \
    && /linux-install-${CLOJURE_CLI_VERSION}.sh \
    && rm /linux-install-${CLOJURE_CLI_VERSION}.sh \
    && su vscode -c "clj --version"; fi

RUN if [ "${INSTALL_BOOT}" = "true" ]; then \
    curl -OL "https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh" \
    && chmod +x boot.sh \
    && mv boot.sh /usr/local/sbin/boot \
    && su vscode -c "boot -u"; fi

RUN if [ "${INSTALL_LEININGEN}" = "true" ]; then \
    curl -OL "https://raw.githubusercontent.com/technomancy/leiningen/${LEININGEN_VERSION}/bin/lein" \
    && chmod +x lein \
    && mv lein /usr/local/sbin; fi

# Cache Clojure and dependencies
RUN if [ "${INSTALL_LEININGEN}" = "true" ]; then \
    su vscode -c " cd ~ \
    && echo '(defproject dummy \"\" :dependencies [[org.clojure/clojure \"'${CLOJURE_VERSION}'\"]])' > project.clj \
    && lein deps \
    && rm project.clj"; fi

RUN if [ "${INSTALL_POLYLITH}" = "true" ]; then \
    curl -OL "https://github.com/polyfy/polylith/releases/download/v${POLYLITH_VERSION}/poly-${POLYLITH_VERSION}.jar" \
    && mkdir -p /usr/local/polylith \
    && mv poly-$POLYLITH_VERSION.jar /usr/local/polylith \
    && echo '#!/bin/sh\nARGS=""\nwhile [ "$1" != "" ] ; do\n  ARGS="$ARGS $1"\n  shift\ndone\nexec "java" $JVM_OPTS "-jar" "/usr/local/polylith/poly-'$POLYLITH_VERSION'.jar" $ARGS\n' > /usr/local/sbin/poly \
    && chmod +x /usr/local/sbin/poly \
    && /usr/local/sbin/poly version; fi

# [Choice] Node.js version: none, lts/*, 16, 14, 12, 10
ARG NODE_VERSION="none"
RUN if [ "${NODE_VERSION}" != "none" ]; then su vscode -c "umask 0002 && . /usr/local/share/nvm/nvm.sh && nvm install ${NODE_VERSION} 2>&1"; fi

# [Optional] Uncomment this section to install additional OS packages.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
     && apt-get -y install  r-base r-cran-devtools

# Clean up package lists
RUN apt-get autoremove -y && apt-get clean -y && rm -rf /var/lib/apt/lists/*

RUN Rscript -e 'install.packages("tidyverse",repos = "https://cloud.r-project.org")'

USER vscode
