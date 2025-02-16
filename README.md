
# Boundless

### For the POC we have a:
- Basic UI to upload and list invoices http://localhost:9000
- To set your personal chatpdf API key please set `pdf.api.key.value` under `/xtracto/src/main/resources/application.properties` which can be created at https://www.chatpdf.com/ (by default we have a test API key set which works)
- Note: After uploading, please wait for a second or so to display the list of invoices

### Assumptions:
- Assuming that API responses and data points are reasonable and can be held in memory
- When the application is running we can monitor the state of the application using Spring framework provided actuator APIs http://localhost:9001/actuator

### Next steps:
- Add account/user id auth, handling and scoping
- Consumer should be able to optionally provide instructions on entities to be extracted
- Make everything async and non-blocking - only PDF storage and processing is async now
- Be able to upload and process multiple documents in parallel
- Control how long we want the service to store the data extracted in DB
- Use a dedicated DB cluster instead of an in-memory DB like H2

## Tech stack
- Java 21
- Gradle
- Spring Boot
- H2 database
- Flogger (for logging)

## Running the application
- Before running please have `JDK 21` installed and env vars `PATH`, `JAVA_HOME` setup
- Run gradle
`./gradlew clean bootRun`
- UI to upload and list invoices http://localhost:9000
- Management/Actuator APIs http://localhost:9001/actuator
***
