plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(24))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-aop")

	// Add Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

	// Jackson Core
	implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")

	// Jackson Databind (ObjectMapper lives here)
	implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

	// (Optional) Jackson Annotations — sometimes required for features like @JsonProperty
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")

	implementation("org.json:json:20231013")
	implementation("org.postgresql:postgresql:42.7.2")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // for JSON parsing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
	useJUnitPlatform()
}
