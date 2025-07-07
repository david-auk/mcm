FROM gradle:jdk24-corretto AS build

# Switch to root to install tmux using yum
USER root
RUN yum install -y tmux && \
    yum clean all && \
    rm -rf /var/cache/yum

# Switch back to the gradle user
USER gradle

# Set working directory
WORKDIR /app

# Copy project files with correct ownership
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle gradle gradle/
COPY --chown=gradle:gradle src src/

# Suspress OpenJDK 64-Bit Server VM warning: Unable to get SVE vector length on this system
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

# Build the project
RUN gradle --no-daemon build

# Run the application
CMD ["gradle", "bootRun", "--no-daemon", "--stacktrace"]