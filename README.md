### **NCAA PRs API - Project Documentation**

## **Overview**

NCAA PRs API is a web application designed to **scrape, store, and serve athlete performance data** for college track and field teams. The application is deployed on **Google Kubernetes Engine (GKE)** and utilizes **Google Secret Manager** for securely managing sensitive credentials and **Google Artifact Registry** for storing and deploying containerized applications.
Note, it doesn't actually scrape tffrs itself, that is done with a seperate python flask app/api which this calls.

Part of ncaaprs project family:
Original UI+scraper (currrently live): https://github.com/braedensc/ncaaprs
Back-end seperated out from above project: https://github.com/braedensc/ncaaprs_backend
Actual site: https://ncaaprs.herokuapp.com

### **Core Components**

- **GKE (Google Kubernetes Engine)**:\
  The app runs as a set of Kubernetes deployments and services within **GKE**. The cluster manages application scaling, load balancing, and resource allocation.

  - GKE Project: [Google Cloud Console](https://console.cloud.google.com/) (

- **Google Secret Manager**:

  - Stores the MongoDB connection string securely, preventing it from being hardcoded into deployment files or environment variables.
  - The application retrieves the connection string at runtime using **Spring Cloud GCP Secret Manager integration**.

- **Google Artifact Registry**:

  - Stores Docker container images built for the application.
  - These images are referenced in **Kubernetes deployment configurations** to ensure GKE is running the latest version.

### **How Everything Fits Together**

1. **Code is developed and built locally**

   - The app is a Spring Boot service that scrapes NCAA performance data and exposes API endpoints.
   - **Maven is used for dependency management and packaging**:
     ```sh
     mvn clean package -DskipTests
     ```

2. **Application is containerized with Docker**

   - After building the JAR, a **Docker image is created and tagged**:
     ```sh
     docker buildx build --platform linux/amd64,linux/arm64 -t ncaaprsapi .
     ```
   - The image is then **pushed to Google Artifact Registry**:
     ```sh
     docker tag ncaaprsapi us-central1-docker.pkg.dev/ncaaprsapi/ncaaprsapi/ncaaprsapi
     docker push us-central1-docker.pkg.dev/ncaaprsapi/ncaaprsapi/ncaaprsapi
     ```

3. **Deployment to GKE**

   - The Kubernetes deployment configuration (**deployment.yaml**) references the latest container image from **Artifact Registry**.
   - Secret Manager is configured to inject sensitive values into environment variables used by the application.
   - Deployment is applied via `kubectl`:
     ```sh
     kubectl apply -f deployment.yaml
     kubectl apply -f service.yaml
     ```

4. **Database Connection via Google Secret Manager**

   - The MongoDB connection string is stored as a **Google Secret**:
     ```sh
     gcloud secrets create ncaaprsmongoconnectionstring --replication-policy="automatic"
     ```
   - The application retrieves it dynamically during startup to avoid exposing credentials in the repo.

5. **Production Readiness**

   - The **Google IAM (Identity and Access Management) service account** used by the GKE cluster is granted permission to access secrets.
   - Logging, monitoring, and scaling policies are managed through **GKE's built-in observability tools**.

---

## **Deployment Guide**

### **Build & Push the Application**

1. **Compile the JAR**:

   ```sh
   mvn clean package -DskipTests
   ```

2. **Build Docker Image**:

   ```sh
   docker buildx build --platform linux/amd64,linux/arm64 -t ncaaprsapi .
   ```

3. **Push to Artifact Registry**:

   ```sh
   docker tag ncaaprsapi us-central1-docker.pkg.dev/ncaaprsapi/ncaaprsapi/ncaaprsapi
   docker push us-central1-docker.pkg.dev/ncaaprsapi/ncaaprsapi/ncaaprsapi
   ```

### **Deploy to Kubernetes**

1. **Apply Kubernetes configuration**:

   ```sh
   kubectl apply -f deployment.yaml
   kubectl apply -f service.yaml
   ```

2. **Restart Deployment After Updates**

   ```sh
   kubectl rollout restart deployment ncaaprsapi
   ```

3. **Verify Deployment**:

   ```sh
   kubectl get pods
   kubectl get services
   ```

---

## **Local Development**

### **Run MongoDB Locally**

```sh
docker run -d --name mongodb -p 27017:27017 mongo:latest
```

### **Database Debugging**

Check if MongoDB is running:

```sh
docker ps
```

Connect to MongoDB and list databases:

```sh
docker exec -it mongodb mongosh
show dbs;
```

Retrieve a team by name:

```sh
db.teams.findOne({ teamName: "GEORGIA TECH" });
```

Find an athlete by name:

```sh
db.athletes.findOne({ name: "Omar Arnaout" });
```

---

## **API Endpoints**

### **Scrape & Update a Team**

```sh
curl -X GET "http://localhost:8080/api/v1/scrapeAndUpdateTeam?teamLink=https://www.tfrrs.org/teams/GA_college_m_Georgia_Tech.html"
```

### **Retrieve a Team's Data**

```sh
curl -X GET "http://localhost:8080/api/v1/team?teamLink=https://www.tfrrs.org/teams/GA_college_m_Georgia_Tech.html"
```

---

## **Future Considerations**

- Add **authentication and rate limiting** for API security.
- Improve **logging and monitoring** for scraper failures.
- Optimize **database indexing** for faster queries.
- Add events table and performance record tables for query optimization/future data analysis

---

## **Future TODO's**

### **Mid-Term Goals (CI/CD & Deployment Enhancements)**
- Set up CI/CD pipeline for automatic deployments
- Automate Docker builds and Kubernetes deployments.
- Ensure seamless deployment via GitHub Actions or Cloud Build.
- Deploy UI and Python Web Scraper inside this same GKE cluster
- Ensure all microservices are hosted in one managed cluster.
- Restrict API access to only allow calls from the deployed UI & scraper.
- Configure CORS settings properly.
### **Long-Term Goals (Future Enhancements)**
- Build GraphQL Endpoint for PR Data
- Enable efficient event-based and athlete-based queries.
- Add Logging & Monitoring
- Detect scraper failures and track API usage metrics.
- Use Google Cloud Logging & Prometheus.
- Optimize Database Queries
- Add API endpoints to fetch athletes by event for frontend sorting optimizations.
- Enable performance-based team rankings
- Fetch top athletes within each event.
- Compare progression of athletes over time using historical PRs.
### **Overall Priority Order**
- Integrate with frontend
- Deploy all services inside the cluster for a unified deployment.
- Implement GraphQL, logging, monitoring, and other optimizations.
- Let me know if you want anything revised or added!

