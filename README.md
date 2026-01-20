# TRASIMA - Traffic Simulation Mannheim

Multi-threaded traffic simulation system with geospatial coordinate modeling. Course project for **Distributed Systems (Verteilte Systeme)** at DHBW Mannheim.

## Project Overview

TRASIMA simulates virtual vehicles (V2) moving on Earth's surface using geographic coordinates. The simulation employs physics-based calculations (Euler approximation) and runs multiple vehicles concurrently using Java threads.

## Project Structure

This is a **Maven multi-module project** with the following structure:

```
trasima/
├── pom.xml                          # Parent POM with shared dependencies and plugins
│
└── trasima-aufgabe02/               # Assignment 02: Basic Vehicle Simulation
    ├── pom.xml                      # Module-specific configuration
    ├── src/
    │   ├── main/java/dhbw/trasima/
    │   │   ├── App.java             # Main entry point
    │   │   ├── Coordinates.java     # Geographic coordinate system
    │   │   ├── VirtualVehicle.java  # Thread-based vehicle simulation
    │   │   ├── IPublisher.java      # Generic publisher interface
    │   │   └── V2ConsolePublisher.java  # Console output implementation
    │   │
    │   └── test/java/dhbw/trasima/
    │       └── CoordinatesTest.java # Comprehensive unit tests
    │
    └── README.md                    # Module-specific documentation
```

### Subprojects

- **`trasima-aufgabe02`**: Initial implementation with basic vehicle simulation (120 seconds runtime)
- *(Future assignments will be added as separate modules)*

## Building the Project

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Maven 3.x

### Build Commands

```bash
# Build entire project (all modules)
mvn clean install

# Build specific module
cd trasima-aufgabe02
mvn clean install

# Run tests only
mvn test

# Skip tests during build
mvn clean install -DskipTests
```

## Running the Simulation

```bash
# Navigate to the module
cd trasima-aufgabe02

# Run with default settings (1 vehicle)
mvn exec:java -Dexec.mainClass="dhbw.trasima.App"

# Or compile and run JAR
mvn package
java -jar target/trasima-aufgabe02-1.0-SNAPSHOT.jar 3
```


## Course Context

This project is part of the **Verteilte Systeme (Distributed Systems)** course at DHBW Mannheim, taught by Prof. Dr. Harald Kornmayer.

## License

Copyright (c) 2026 Thomas Henseler

This is an educational project for DHBW Mannheim. Permission is hereby granted, free of charge, to students enrolled in the
Verteilte Systeme (Distributed Systems) course at DHBW Mannheim to use,
copy, modify, and distribute this software for educational purposes only.
