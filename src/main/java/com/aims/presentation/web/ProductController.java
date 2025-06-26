package com.aims.presentation.web;

import com.aims.application.commands.AddProductCommand;
import com.aims.application.commands.UpdateProductCommand;
import com.aims.application.commands.SearchProductQuery;
import com.aims.application.services.ProductApplicationService;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.entity.Book;
import com.aims.domain.product.entity.CD;
import com.aims.domain.product.entity.DVD;
import com.aims.presentation.dto.ApiResponse;
import com.aims.presentation.dto.ProductRequest;
import com.aims.presentation.dto.ProductResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Product operations
 * Unified controller following RESTful design principles
 * Delegates business logic to ProductApplicationService
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/products")
public class ProductController {

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

    private final ProductApplicationService productApplicationService;

    @Autowired
    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    /**
     * Create a new product
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductRequest request) {
        try {
            // Convert request to command
            AddProductCommand command = mapToAddProductCommand(request);
            
            // Delegate to application service
            Long productId = productApplicationService.addProduct(command);
            
            // Create response
            ProductResponse response = ProductResponse.success(productId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create product", INTERNAL_ERROR));
        }
    }

    /**
     * Update an existing product
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("id") Long id, 
            @RequestBody ProductRequest request) {
        try {
            // Set the ID from path parameter
            request.setId(id);
            
            // Convert request to command
            UpdateProductCommand command = mapToUpdateProductCommand(request);
            
            // Delegate to application service
            Product updatedProduct = productApplicationService.updateProduct(command);
            
            // Convert to response
            ProductResponse response = mapToProductResponse(updatedProduct);
            
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update product", INTERNAL_ERROR));
        }
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable("id") Long id) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid product ID", VALIDATION_ERROR));
            }
            
            // Delegate to application service
            Optional<Product> product = productApplicationService.viewProduct(id);
            
            if (product.isPresent()) {
                ProductResponse response = mapToProductResponse(product.get());
                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found", NOT_FOUND));
            }
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get product", INTERNAL_ERROR));
        }
    }

    /**
     * Get all products
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        try {
            // Delegate to application service
            List<Product> products = productApplicationService.getAllProducts();
            
            // Convert to responses
            List<ProductResponse> responses = products.stream()
                .map(this::mapToProductResponse)
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get products", INTERNAL_ERROR));
        }
    }

    /**
     * Search products
     * GET /api/products/search?title={title}&type={type}&...
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            // Create search query
            SearchProductQuery searchQuery = SearchProductQuery.builder()
                .title(title)
                .type(type)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .page(page)
                .size(size)
                .build();
            
            // Delegate to application service
            List<Product> products = productApplicationService.searchProducts(searchQuery);
            
            // Convert to responses
            List<ProductResponse> responses = products.stream()
                .map(this::mapToProductResponse)
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search products", INTERNAL_ERROR));
        }
    }

    /**
     * Delete product
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") Long id) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid product ID", VALIDATION_ERROR));
            }
            
            // For now, return method not implemented
            // This can be implemented when delete functionality is added to the application service
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Delete functionality not implemented", NOT_IMPLEMENTED));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete product", INTERNAL_ERROR));
        }
    }

    // Private mapping methods
    private AddProductCommand mapToAddProductCommand(ProductRequest request) {
        // Convert ProductRequest to domain Product entity
        Product product = mapToProductEntity(request);
        
        // Create command with appropriate settings
        AddProductCommand command = new AddProductCommand();
        command.setProduct(product);
        command.setMakeAvailableImmediately(true); // Default behavior
        
        return command;
    }

    private UpdateProductCommand mapToUpdateProductCommand(ProductRequest request) {
        // Convert ProductRequest to domain Product entity
        Product product = mapToProductEntity(request);
        
        // Create command
        UpdateProductCommand command = new UpdateProductCommand();
        command.setProductId(request.getId());
        command.setProduct(product);
        command.setNotifyInventoryChanges(true); // Default behavior
        
        return command;
    }

    private Product mapToProductEntity(ProductRequest request) {
        Product product;
        
        // Create specific product type based on request type
        if ("Book".equalsIgnoreCase(request.getType())) {
            Book book = new Book();
            // Set book-specific fields
            book.setGenre(request.getGenre());
            book.setAuthors(request.getAuthor());
            book.setPublishers(request.getPublisher());
            book.setPageCount(request.getNumberOfPages());
            // Note: publicationDate would need proper date parsing in production
            product = book;
        } else if ("CD".equalsIgnoreCase(request.getType())) {
            CD cd = new CD();
            // Set CD-specific fields
            cd.setArtist(request.getArtist());
            cd.setRecordLabel(request.getRecordLabel());
            cd.setGenre(request.getMusicGenre()); // Map musicGenre to genre
            cd.setReleaseDate(request.getReleaseDate());
            product = cd;
        } else if ("DVD".equalsIgnoreCase(request.getType())) {
            DVD dvd = new DVD();
            // Set DVD-specific fields
            dvd.setDirector(request.getDirector());
            dvd.setRuntimeMinutes(request.getRuntime());
            dvd.setStudio(request.getStudio());
            dvd.setSubtitleLanguages(request.getSubtitles());
            dvd.setGenre(request.getFilmGenre()); // Map filmGenre to genre
            product = dvd;
        } else {
            // Default to base Product
            product = new Product();
        }
        
        // Set common fields
        product.setTitle(request.getTitle());
        product.setType(request.getType());
        // Convert BigDecimal to Integer for price
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice().intValue());
        }
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        // Map description to introduction field
        product.setIntroduction(request.getDescription());
        
        return product;
    }

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getProductId());
        response.setTitle(product.getTitle());
        response.setType(product.getType());
        // Convert Integer to BigDecimal for price
        if (product.getPrice() != null) {
            response.setPrice(new java.math.BigDecimal(product.getPrice()));
        }
        response.setQuantity(product.getQuantity());
        response.setImageUrl(product.getImageUrl());
        // Check availability with quantity parameter
        response.setAvailable(product.isAvailable(1));
        // Map introduction to description
        response.setDescription(product.getIntroduction());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        // Map type-specific fields
        if (product instanceof Book book) {
            response.setAuthor(book.getAuthors());
            response.setGenre(book.getGenre());
            response.setPublisher(book.getPublishers());
            response.setNumberOfPages(book.getPageCount());
            if (book.getPublicationDate() != null) {
                response.setPublicationDate(book.getPublicationDate().toString());
            }
        } else if (product instanceof CD cd) {
            response.setArtist(cd.getArtist());
            response.setRecordLabel(cd.getRecordLabel());
            response.setMusicGenre(cd.getGenre()); // Map genre to musicGenre
            response.setReleaseDate(cd.getReleaseDate());
        } else if (product instanceof DVD dvd) {
            response.setDirector(dvd.getDirector());
            response.setRuntime(dvd.getRuntimeMinutes());
            response.setStudio(dvd.getStudio());
            response.setSubtitles(dvd.getSubtitleLanguages());
            response.setFilmGenre(dvd.getGenre()); // Map genre to filmGenre
        }
        
        return response;
    }
}
