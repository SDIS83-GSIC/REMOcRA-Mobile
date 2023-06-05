# Doit correspondre au gradle/wrapper/gradle-wrapper.properties
FROM gradle:8.0.0-jdk17

# https://developer.android.com/studio#command-line-tools-only
ARG sdk_archive=commandlinetools-linux-9477386_latest.zip
ARG sdk_checksum=bd1aa17c7ef10066949c88dc6c9c8d536be27f992a1f3b5a584f9bd2ba5646a0
ARG android_home=/opt/android/sdk

RUN curl --silent --show-error --location --fail --retry 3 --output /tmp/${sdk_archive} https://dl.google.com/android/repository/${sdk_archive} && \
    echo "${sdk_checksum} */tmp/${sdk_archive}" | sha256sum -c - && \
    unzip -q /tmp/${sdk_archive} -d /tmp && \
    mkdir -p ${android_home}/cmdline-tools && \
    mv /tmp/cmdline-tools ${android_home}/cmdline-tools/latest && \
    rm /tmp/${sdk_archive}

ENV ANDROID_HOME ${android_home}
ENV PATH ${ANDROID_HOME}/emulator:${ANDROID_HOME}/tools:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}

RUN --mount=type=tmpfs,target=/root/.android \
    yes | sdkmanager --licenses && \
    yes | sdkmanager --update && \
    sdkmanager \
        "tools" \
        "platform-tools" \
        "emulator" \
        # Si non précisée dans le build.gradle.kts, la version minimale supportée par le plugin est utilisée:
        # https://developer.android.com/build/releases/gradle-plugin
        "build-tools;30.0.3" \
        # Correspond au compileSdk du build.gradle.kts
        "platforms;android-33"
