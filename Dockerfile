FROM alvrme/alpine-android:android-34-jdk17

# Install additional build tools and utilities
RUN sdkmanager "platforms;android-21" \
    "platforms;android-34" \
    "platforms;android-35" \
    "build-tools;35.0.0" \
    "ndk;25.2.9519653" \
    "cmake;3.22.1"

# Install common dev tools
RUN apk add --no-cache \
    git \
    curl \
    bash \
    openssh \
    jq \
    nmap \
    && rm -rf /var/cache/apk/*

# Install Gradle
ARG GRADLE_VERSION=8.9
RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && rm /tmp/gradle.zip \
    && ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle \
    && gradle --version

# Set up Android environment
ENV ANDROID_SDK_ROOT=/opt/sdk \
    ANDROID_HOME=/opt/sdk \
    GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true" \
    TERM=xterm-256color

# Create app directory
WORKDIR /app

# Command
CMD ["/bin/bash"]
