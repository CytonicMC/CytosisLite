FROM eclipse-temurin:21.0.7_6-jre-jammy
LABEL authors="Foxikle & Webhead1104"

# Set the working directory
WORKDIR /app

# Copy cytosis Jar
ADD ../../build/libs/cytosis.jar .

# Expose the port
EXPOSE 25565

# Run the java
CMD ["java", "-jar", "cytosis.jar"]