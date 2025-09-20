# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.6 application built with Kotlin that demonstrates Spring AI integration with Elasticsearch as a vector database. The project showcases product search and recommendation capabilities using AI embeddings and vector similarity search.

**Tech Stack:**
- Kotlin 1.9.25 with Spring Boot 3.5.6
- Java 21 toolchain
- Spring AI 1.0.2 with Vertex AI embeddings
- Elasticsearch 9.1.2 as vector store
- Gradle with Kotlin DSL

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "SpringaiElasticsearchApplicationTests"

# Clean build
./gradlew clean build
```

### Infrastructure
```bash
# Start Elasticsearch and Kibana
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f elasticsearch
docker-compose logs -f kibana
```

### Elasticsearch Access
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601

## Architecture

### Core Components

**Product Domain Model (`Product.kt`)**
- Simple data class representing products with: productName, description, brand, maker, category, price
- Companion object factory method `Product.of()` for creation

**Vector Document Conversion (`ProductConvert.kt`)**
- Extension function `Product.toDocument()` converts products to Spring AI Document objects
- Creates structured text representation and metadata for vector embedding
- Text format includes all product attributes in Korean
- Metadata preserves individual fields for filtering and retrieval

**Sample Data (`SampleDataInit.kt`)**
- Contains 54+ sample products across categories: electronics, refrigerators, kimchi refrigerators, food items
- Uses VectorStore to add sample documents for testing and demonstration
- Products include Korean descriptions with technical specifications

### Spring AI Integration

**Vector Store Configuration (`application.yml`)**
- Elasticsearch vector store with cosine similarity
- 1536 dimensions (Vertex AI embedding size)
- Index name: "product"
- Auto-schema initialization enabled

**Key Dependencies:**
- `spring-ai-starter-vector-store-elasticsearch`: Elasticsearch vector database integration
- `spring-ai-starter-model-vertex-ai-embedding`: Google Vertex AI embeddings
- `spring-ai-advisors-vector-store`: Vector store advisors for RAG patterns

### Data Flow
1. Products are created using `Product.of()` factory method
2. Converted to Spring AI Documents via `toDocument()` extension
3. Documents are embedded using Vertex AI and stored in Elasticsearch
4. Vector similarity search enables semantic product discovery

## Development Notes

### Elasticsearch Setup
- The application expects Elasticsearch running on localhost:9200
- Use the provided docker-compose.yml for local development
- Security is disabled for development (xpack.security.enabled=false)

### Sample Data Initialization
- Sample products are initialized through `SampleDataInit.addSampledata()`
- Data includes diverse product categories with Korean descriptions
- Metadata structure supports filtering by brand, category, price range

### Vector Search Capabilities
- Products are vectorized based on their full text description
- Supports semantic search across product names, descriptions, brands
- Cosine similarity enables finding similar products by features/characteristics