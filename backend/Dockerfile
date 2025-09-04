FROM gradle:jdk24-corretto AS build

# Set working directory
WORKDIR /app

# Switch to root to install tmux using yum
USER root
RUN yum install -y tmux && \
    yum clean all && \
    rm -rf /var/cache/yum

# Copy project files with correct ownership
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle/
COPY --chown=gradle:gradle src src/

RUN chown -R gradle:gradle build.gradle.kts settings.gradle.kts gradle src

# Switch back to the gradle user
USER gradle

# Parse JAVA_TOOL_OPTIONS
ARG JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS=$JAVA_TOOL_OPTIONS

# Create empty skel
RUN mkdir /home/gradle/server_instances
RUN touch /home/gradle/server_instances/touchfile

# Build the project
RUN gradle --no-daemon build --stacktrace

# Run the application
CMD ["gradle", "bootRun", "--no-daemon", "--stacktrace"]