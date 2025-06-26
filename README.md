# AIMS - Advanced Inventory Management System

## Clean Architecture Structure

This project follows Clean Architecture principles to ensure maintainability, testability, and separation of concerns.

## Package Structure

```
src/main/java/com/aims/
├── presentation/              # Presentation Layer (Interface Adapters)
│   ├── web/                  # REST Controllers
│   ├── dto/                  # Request/Response DTOs
│   └── boundary/             # Interface definitions for external services
├── application/              # Application Layer (Use Cases)
│   ├── services/            # Application Services (orchestration)
│   └── commands/            # Command/Query objects
├── domain/                  # Domain Layer (Enterprise Business Rules)
│   ├── product/             # Product Domain
│   │   ├── entity/          # Product entities and value objects
│   │   ├── repository/      # Product repository interfaces
│   │   └── service/         # Product domain services
│   ├── cart/                # Cart Domain
│   │   ├── entity/          # Cart entities
│   │   ├── repository/      # Cart repository interfaces
│   │   └── service/         # Cart domain services
│   ├── order/               # Order Domain
│   │   ├── entity/          # Order entities
│   │   ├── repository/      # Order repository interfaces
│   │   └── service/         # Order domain services
│   ├── user/                # User Domain
│   │   ├── entity/          # User entities
│   │   ├── repository/      # User repository interfaces
│   │   └── service/         # User domain services
│   └── payment/             # Payment Domain
│       ├── entity/          # Payment entities
│       ├── repository/      # Payment repository interfaces
│       └── service/         # Payment domain services
├── infrastructure/          # Infrastructure Layer (Frameworks & Drivers)
│   ├── persistence/         # Database implementations
│   │   └── jpa/            # JPA repository implementations
│   ├── external/           # External service integrations
│   │   ├── vnpay/          # VNPay payment gateway
│   │   └── notification/   # Email/SMS services
│   └── config/             # Spring configuration classes
└── shared/                 # Shared utilities and common code
    ├── util/               # Utility classes
    └── exception/          # Common exceptions
```

## Architecture Layers

### 1. Domain Layer (Core Business Logic)
- **Entities**: Core business objects with identity
- **Value Objects**: Immutable objects without identity
- **Domain Services**: Business logic that doesn't belong to entities
- **Repository Interfaces**: Contracts for data access (no implementations)
- **Domain Events**: Business events that occur in the domain

**Dependencies**: None (pure business logic)

### 2. Application Layer (Use Cases)
- **Application Services**: Orchestrate domain services and repositories
- **Commands/Queries**: Input/output data structures
- **Use Case Implementations**: Specific business workflows

**Dependencies**: Domain Layer only

### 3. Presentation Layer (Interface Adapters)
- **Controllers**: Handle HTTP requests/responses
- **DTOs**: Data transfer objects for API contracts
- **Boundary Interfaces**: Contracts for external services
- **View Models**: Data structures for UI representation

**Dependencies**: Application Layer, Domain Layer

### 4. Infrastructure Layer (Technical Details)
- **Repository Implementations**: JPA/JDBC implementations
- **External Service Clients**: VNPay, email services
- **Configuration**: Spring Boot configuration
- **Database Migrations**: Schema management

**Dependencies**: All layers (implements interfaces from higher layers)

## Key Principles

### Dependency Rule
- Dependencies point inward (toward the domain)
- Inner layers never depend on outer layers
- Business logic is isolated from technical details

### Separation of Concerns
- Each layer has a single responsibility
- Business rules are separated from technical implementation
- Easy to test each layer in isolation

### Testability
- Domain layer can be tested without external dependencies
- Application layer can be tested with mocked repositories
- Infrastructure layer can be tested with integration tests

## Migration Status

### Phase 1: Package Structure ✅
- [x] Created Clean Architecture package structure
- [x] Added .gitkeep files for empty directories
- [x] Updated main application class
- [x] Created comprehensive documentation

### Upcoming Phases
- [ ] Phase 2: Entity Consolidation
- [ ] Phase 3: Repository Layer Refactoring
- [ ] Phase 4: Application Service Layer
- [ ] Phase 5: Controller Refactoring
- [ ] Phase 6: Payment Boundary Refactoring
- [ ] Phase 7: Service Layer Cleanup
- [ ] Phase 8: Configuration and Integration
- [ ] Phase 9: Final Migration and Cleanup

## Domain Contexts

### Product Domain
Manages product catalog, inventory, and product lifecycle.

### Cart Domain
Handles shopping cart operations and temporary product reservations.

### Order Domain
Manages order lifecycle from placement to fulfillment.

### User Domain
Handles user management and authentication.

### Payment Domain
Manages payment transactions and integration with payment gateways.

## Technology Stack

- **Framework**: Spring Boot 3.4.4
- **Java Version**: 21
- **Database**: JPA/Hibernate
- **Build Tool**: Maven
- **Architecture**: Clean Architecture
- **Testing**: JUnit 5, Mockito, TestContainers

## Getting Started

1. Ensure Java 21 is installed
2. Run `./mvnw clean install` to build the project
3. Run `./mvnw spring-boot:run` to start the application
4. Access the application at `http://localhost:8080`

## Development Guidelines

### Adding New Features
1. Start with domain entities and business rules
2. Create repository interfaces in domain layer
3. Implement application services for use cases
4. Add controllers for API endpoints
5. Implement infrastructure details last

### Testing Strategy
- Unit tests for domain services (no external dependencies)
- Integration tests for application services
- API tests for controllers
- Repository tests with @DataJpaTest

### Code Quality
- Follow SOLID principles
- Maintain high test coverage
- Use meaningful names for classes and methods
- Keep methods small and focused
- Document complex business logic

## Contributing

1. Follow the established package structure
2. Maintain dependency rules (inner layers don't depend on outer layers)
3. Write tests for all new functionality
4. Update documentation when adding new features
5. Use conventional commit messages
