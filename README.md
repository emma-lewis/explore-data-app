# Explore Data App

A **Spring Boot** web application for **exploratory data analysis** (EDA) of CSV files. Users can upload a CSV dataset (up to 10 MB), view comprehensive summary statistics, and generate distribution visualizations.

---

## Overview

Provides a quick, code‑free way to perform basic EDA on CSV files:

* **Automated column profiling:** Detects data type, calculates min, max, mean, median, mode, standard deviation, variance, count of non‑null values, and NA counts.
* **Value distributions:** Identifies unique value counts and top 5 most frequent values for categorical columns.
* **Interactive plots:** Generates histogram and box plot for numeric columns using Plotly.js.
* **Configurable limits:** File upload size and server port can be adjusted via `application.properties`.

---

## Features

* **Upload CSV** (≤ 10 MB) through a web form
* **Summary report** for each column: type, numeric stats, NA counts, unique values, top frequencies
* **Column selector** and **interactive visualization** (histogram & box plot)
* **Error handling** for oversized files and parsing issues
* **Spring Boot DevTools** for hot‑reload during development

---

## Tech Stack

* **Java 21**
* **Spring Boot 3.3** (Web, Thymeleaf, DevTools)
* **Apache Commons CSV** 1.9.0
* **Apache Commons Math3** 3.6.1
* **Plotly.js** (loaded via CDN)
* **Maven 3.6+** with Wrapper

---

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/emma‑lewis/explore-data-app.git
   cd explore-data-app
   ```

2. **Build the project**

   ```bash
   mvn clean package
   ```

3. **Run the application**

   * Using Maven:

     ```bash
     mvn spring-boot:run
     ```
   * Or with the packaged JAR:

     ```bash
     java -jar target/explore-data-app-0.0.1-SNAPSHOT.jar
     ```

4. **Open in browser**
   Navigate to `http://localhost:8080` to upload your CSV and start exploring.

---

## Configuration

Modify `src/main/resources/application.properties` to adjust settings:

```properties
server.port=8080
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

* Change `server.port` to run on a different port.
* Adjust multipart limits for larger or smaller uploads.

---

## Project Structure

```
src/main/java/com/emmalewis/explore_data_app/
├── ExploreDataAppApplication.java      # Spring Boot entry point
├── controller/
│   └── DataController.java            # Handles upload & visualization endpoints
├── service/
│   └── DataAnalysisService.java       # CSV parsing, stats computation, script generation
└── resources/
    ├── templates/
    │   ├── index.html                 # File upload page
    │   ├── result.html                # Summary report page
    │   └── visualization.html         # Plot display page
    └── application.properties

pom.xml                                # Maven build file
mvnw, mvnw.cmd                         # Maven wrapper scripts
```

---

## License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
