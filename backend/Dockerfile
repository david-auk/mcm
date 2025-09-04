FROM gradle:jdk24-corretto AS build

# Work in /app
WORKDIR /app

# Root for package installs and fixing ownerships
USER root
RUN yum install -y tmux && \
    yum clean all && rm -rf /var/cache/yum

# Copy project files (Podman may ignore --chown; we'll chown explicitly)
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle/
COPY src src/

# Ensure everything under /app is writable by 'gradle'
RUN chown -R gradle:gradle /app

# Switch back to the gradle user
USER gradle

# Keep Gradle caches in the user home (reduces writes in /app)
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Optional: pass JAVA_TOOL_OPTIONS through (ok if empty)
ARG JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS=$JAVA_TOOL_OPTIONS

# Create skel dirs as the gradle user
RUN mkdir -p /home/gradle/server_instances && \
    touch /home/gradle/server_instances/touchfile

# Build
RUN gradle --no-daemon build --stacktrace

# make a writable project cache dir
RUN mkdir -p /home/gradle/.gradle/project-cache

# run with explicit project cache dir
CMD ["gradle","bootRun","--no-daemon","--stacktrace","--project-cache-dir","/home/gradle/.gradle/project-cache"]