FROM gradle:jdk24-corretto AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle/
COPY src src/

RUN gradle --no-daemon build --dryrun || true

CMD ["gradle", "bootRun", "--no-daemon"]
