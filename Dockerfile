# This Dockerfile creates a static build image for CI

FROM openjdk:8-jdk

ARG ANDROID_COMPILE_SDK
RUN test -n "$ANDROID_COMPILE_SDK"

ENV ANDROID_SDK_ROOT /android-sdk-linux
ENV PATH="${PATH}:/android-sdk-linux/platform-tools/:/android-sdk-linux/cmdline-tools/latest/bin/"

# install OS packages
RUN apt-get --quiet update --yes
RUN apt-get --quiet install --yes wget apt-utils unzip lib32stdc++6 lib32z1 build-essential ruby ruby-dev
# We use this for xxd hex->binary
RUN apt-get --quiet install --yes vim-common
# install Android SDK
RUN wget --quiet --output-document=android-commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-6858069_latest.zip
RUN unzip -q android-commandlinetools.zip
RUN rm android-commandlinetools.zip
RUN mkdir -p /android-sdk-linux/cmdline-tools/latest
RUN mv cmdline-tools/* /android-sdk-linux/cmdline-tools/latest/
RUN rm -r cmdline-tools
RUN yes | sdkmanager --install "platforms;android-${ANDROID_COMPILE_SDK}"
# install FastLane
COPY Gemfile.lock .
COPY Gemfile .
RUN gem install bundler
RUN bundle install
