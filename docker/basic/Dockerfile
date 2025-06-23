FROM eclipse-temurin:21.0.7_6-jre-jammy
LABEL authors="Foxikle & Webhead1104"

# Set the working directory
WORKDIR /app

#ENV OTEL_PORT=${OTEL_PORT}
#ENV OTEL_HOSTNAME=${OTEL_HOST}

# Copy cytosis Jar
ADD ../../build/libs/cytosis.jar .

# Conditionally copy config.json (if it exists at build time)
COPY ../../config.json* .

# Expose the port
EXPOSE 25567

# Run the java
CMD ["java", "-jar", "cytosis.jar"]