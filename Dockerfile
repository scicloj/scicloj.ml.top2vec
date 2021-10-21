FROM rocker/r-ver:4.1.1
RUN apt-get update && apt-get -y install openjdk-11-jdk curl rlwrap libssl-dev build-essential zlib1g-dev  libncurses5-dev libgdbm-dev libnss3-dev  libreadline-dev libffi-dev  libbz2-dev libbluetooth-dev libbz2-dev libdb-dev libexpat1-dev libffi-dev libgdbm-dev liblzma-dev libmpdec-dev libncursesw5-dev libreadline-dev libsqlite3-dev libssl-dev zlib1g-dev  git libbz2-dev

RUN curl -O https://www.python.org/ftp/python/3.9.5/Python-3.9.5.tar.xz
RUN tar xf Python-3.9.5.tar.xz
RUN cd Python-3.9.5 && ./configure --enable-shared --with-ensurepip=install && make && make install && ldconfig
RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.981.sh && chmod +x linux-install-1.10.3.981.sh && ./linux-install-1.10.3.981.sh
RUN Rscript -e 'install.packages("http://rforge.net/Rserve/snapshot/Rserve_1.8-7.tar.gz")'
RUN clj -P
RUN pip3 install -U numpy wheel scikit-learn cython tqdm nltk

# leav this at th end
ARG USER_ID
ARG GROUP_ID

RUN addgroup --gid $GROUP_ID user
RUN adduser --disabled-password --gecos '' --uid $USER_ID --gid $GROUP_ID user

RUN git clone https://github.com/openefsa/Top2Vec.git -b progressLog
RUN cd Top2Vec &&  sed -i -r 's/\b(tensorflow|tensorflow_hub|tensorflow_text|torch|sentence_transformers)\b//g' requirements.txt && pip3 install -r requirements.txt && python3 setup.py install

RUN curl https://raw.githubusercontent.com/behrica/libpython-clj/load-file/cljbridge.py -o /usr/local/lib/python3.9/cljbridge.py
RUN curl https://gist.githubusercontent.com/behrica/91b3f958fad80247069ade3b96646dcf/raw/4f58a93118702d34394e49fb8e1f3c4b4ed6c95f/PWI_top2vec.py -o /usr/local/lib/python3.9/PWI_top2vec.py
USER user
WORKDIR /home/user

RUN export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 && pip3 install python-javabridge
RUN echo "import cljbridge\ncljbridge.init_clojure_repl()" > /home/user/start_repl.py


RUN curl -O https://julialang-s3.julialang.org/bin/linux/x64/1.5/julia-1.5.3-linux-x86_64.tar.gz \
  && tar -xvzf julia-1.5.3-linux-x86_64.tar.gz

ENV JULIA_HOME=/home/user/julia-1.5.3

ENV PYTHONUNBUFFERED=1


COPY deps.edn /home/user

CMD ["python3", "-c", "import cljbridge\ncljbridge.init_jvm(start_repl=True,port=12345,bind='0.0.0.0')"]
