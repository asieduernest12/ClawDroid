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
    go \
    ca-certificates \
    tzdata \
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

# Set up NDK cross-compiler variables for Go CGO
ENV NDK_TOOLCHAIN="/opt/sdk/ndk/25.2.9519653/toolchains/llvm/prebuilt/linux-x86_64/bin"
ENV PATH="${NDK_TOOLCHAIN}:${PATH}"
ENV CC_arm64="aarch64-linux-android21-clang"
ENV CC_amd64="x86_64-linux-android21-clang"
ENV CC_386="i686-linux-android21-clang"
ENV CC_arm="armv7a-linux-androideabi21-clang"

# Copy PicoClaw build scripts
COPY docker/resolve-ref.sh /usr/local/bin/resolve-ref.sh
COPY docker/build-picoclaw.sh /usr/local/bin/build-picoclaw.sh
RUN chmod +x /usr/local/bin/resolve-ref.sh /usr/local/bin/build-picoclaw.sh

ENV GITHUB_API="https://api.github.com/repos/sipeed/picoclaw"

# Create app directory
WORKDIR /app

# Command
CMD ["/bin/bash"]
