# SineWaveATL1

This is a project for a ATL Programming Challenge

## Features

Allows you to generate a sine wave, a sawtooth, a triangle or a square

This is unfinished, so for now, please consider it an unstable program...

## Building and Running

### Prerequisites
- Java 11 or higher

### Building the Project
To build the project, you can use the Gradle wrapper:

On Windows:
```bash
gradlew.bat build
```

Or if you have Gradle installed globally:
```bash
gradle build
```

### Running the Application
After building, you can run the application using:
```bash
gradlew.bat run
```

Or from the built distribution:
```bash
java -jar build/libs/sine-wave-generator-1.0.0.jar
```

### Creating a Distribution
To create a distributable version:
```bash
gradlew.bat distZip
```
or
```bash
gradlew.bat distTar
```

The executable JAR will be located in `build/libs/`.
