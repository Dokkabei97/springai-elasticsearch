# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.6 application built with Kotlin that demonstrates Spring AI integration with Elasticsearch as a vector database. The project showcases product search and recommendation capabilities using AI embeddings and vector similarity search.

**Tech Stack:**
- Kotlin 1.9.25 with Spring Boot 3.5.6
- Java 21 toolchain
- Spring AI 1.0.2 base, with Spring AI 1.1.0-M1 for embedding/vector store
- Google Gemini API (gemini-embedding-001) for embeddings - 768 dimensions
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

**Search API (`search/` package)**
- `ProductSearchService`: Core search logic with semantic and hybrid search modes
  - **Semantic Search**: Pure vector similarity search (default, threshold 0.7)
  - **Hybrid Search**: Vector search + metadata filtering (auto-activated when filters present)
  - Sorting options: RELEVANCE, PRICE_ASC/DESC, NAME_ASC/DESC
  - Pagination support with configurable page size
- `ProductSearchController`: REST API endpoints:
  - `GET /api/search/products`: Main search with filters, pagination, sorting
  - `POST /api/search/products/advanced`: Advanced search with full request body
  - `GET /api/search/products/semantic`: Pure semantic search
  - `GET /api/search/products/category/{category}`: Category-specific search
  - `GET /api/search/products/brand/{brand}`: Brand-specific search
  - `GET /api/search/products/suggestions`: Search suggestions (basic implementation)
  - `GET /api/search/test/korean`: Korean language search testing endpoint

### Spring AI Integration

**Vector Store Configuration (`application.yml`)**
- Elasticsearch vector store with cosine similarity
- 768 dimensions (Gemini embedding size)
- Index name: "product"
- Auto-schema initialization enabled
- **Important**: Requires `spring.ai.google.genai.embedding.project-id` and `api-key` to be configured

**Key Dependencies:**
- `spring-ai-starter-vector-store-elasticsearch:1.1.0-M1`: Elasticsearch vector database integration
- `spring-ai-starter-model-google-genai-embedding:1.1.0-M1`: Google Gemini embeddings
- `spring-ai-advisors-vector-store`: Vector store advisors for RAG patterns

### Data Flow
1. Products are created using `Product.of()` factory method
2. Converted to Spring AI Documents via `toDocument()` extension function
3. Documents are embedded using Gemini API (768-dimensional vectors) and stored in Elasticsearch
4. Search queries are also embedded and matched against product vectors using cosine similarity
5. Results can be filtered by category, brand, maker, price range
6. Results can be sorted by relevance, price, or name

## Development Notes

### Elasticsearch Setup
- The application expects Elasticsearch running on localhost:9200
- Use the provided docker-compose.yml for local development
- Security is disabled for development (xpack.security.enabled=false)
- Kibana available at http://localhost:5601 for index inspection

### Gemini API Configuration
Before running the application, you must configure Google Gemini API credentials in `application.yml`:
```yaml
spring.ai.google.genai.embedding:
  project-id: your-project-id  # Replace with your Google Cloud project ID
  api-key: your-api-key        # Replace with your Gemini API key
```

### Sample Data Initialization
- Sample products are automatically initialized on startup via `@PostConstruct` in `SampleDataInit`
- Contains 54+ products across multiple categories (냉장고, 김치냉장고, 식품)
- All product descriptions and metadata are in Korean
- Each product is converted to a Spring AI Document with embedded metadata
- Metadata fields: productName, description, brand, maker, category, price, type

### Search Behavior
- **Semantic Search** (default): Uses vector similarity only, threshold 0.7
- **Hybrid Search** (auto-activated): Combines vector similarity with metadata filters
  - Triggers when any filter is present (categories, brands, makers, price range)
  - Uses lower threshold (0.5) to get broader results, then filters
  - Supports multiple filter combinations
- **Query Normalization**: Removes special characters, normalizes spacing, lowercases
- **Relevance Scoring**: Calculated based on result position (1.0 - index * 0.05)

### Testing
- Tests in `ProductSearchServiceTest` demonstrate various search scenarios
- All tests require Elasticsearch to be running (uses real vector store)
- Tests validate Korean language search, filters, hybrid search, and sorting
- Run specific test: `./gradlew test --tests "ProductSearchServiceTest.test Korean semantic search for refrigerators"`

### API Testing Examples
```bash
# Basic semantic search
curl "http://localhost:8080/api/search/products?q=냉장고&size=5"

# Search with price filter
curl "http://localhost:8080/api/search/products?q=냉장고&minPrice=500000&maxPrice=1000000"

# Search with category filter
curl "http://localhost:8080/api/search/products?q=삼성&categories=김치냉장고"

# Test Korean search functionality
curl "http://localhost:8080/api/search/test/korean"
```