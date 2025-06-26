package com.aims.presentation.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Controller for database connectivity and system health checks
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private static final String STATUS_KEY = "status";
    private static final String SUCCESS_VALUE = "success";
    private static final String ERROR_KEY = "error";
    private static final String DATABASE_KEY = "database";
    private static final String CONNECTED_KEY = "connected";
    
    private final DataSource dataSource;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AIMS Backend");
        response.put("timestamp", System.currentTimeMillis());
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            response.put("database", Map.of(
                "url", metaData.getURL(),
                "driver", metaData.getDriverName(),
                "version", metaData.getDriverVersion(),
                "connected", true
            ));
        } catch (Exception e) {
            log.error("Database connection failed", e);
            response.put("database", Map.of(
                "connected", false,
                "error", e.getMessage()
            ));
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get list of database tables
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTables() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> tables = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Get all tables
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    Map<String, Object> table = new HashMap<>();
                    table.put("name", rs.getString("TABLE_NAME"));
                    table.put("type", rs.getString("TABLE_TYPE"));
                    table.put("schema", rs.getString("TABLE_SCHEM"));
                    
                    // Get row count for each table
                    String tableName = rs.getString("TABLE_NAME");
                    try (Statement stmt = connection.createStatement();
                         ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        if (countRs.next()) {
                            table.put("rowCount", countRs.getInt(1));
                        }
                    } catch (Exception e) {
                        table.put("rowCount", "N/A");
                        table.put("error", e.getMessage());
                    }
                    
                    tables.add(table);
                }
            }
            
            response.put("status", "success");
            response.put("tables", tables);
            response.put("totalTables", tables.size());
            
        } catch (Exception e) {
            log.error("Failed to retrieve tables", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get products for testing
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_id, title, price, product_type FROM products LIMIT 10")) {
            
            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("id", rs.getLong("product_id"));
                product.put("title", rs.getString("title"));
                product.put("price", rs.getInt("price"));
                product.put("type", rs.getString("product_type"));
                products.add(product);
            }
            
            response.put("status", "success");
            response.put("products", products);
            response.put("count", products.size());
            
        } catch (Exception e) {
            log.error("Failed to retrieve products", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("products", new ArrayList<>());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Database connection test
     */
    @GetMapping("/db-connection")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            response.put("status", "connected");
            response.put("database", Map.of(
                "productName", metaData.getDatabaseProductName(),
                "productVersion", metaData.getDatabaseProductVersion(),
                "driverName", metaData.getDriverName(),
                "driverVersion", metaData.getDriverVersion(),
                "url", metaData.getURL(),
                "userName", metaData.getUserName(),
                "autoCommit", connection.getAutoCommit(),
                "readOnly", connection.isReadOnly(),
                "catalog", connection.getCatalog()
            ));
            
            // Test a simple query
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1 as test")) {
                if (rs.next()) {
                    response.put("queryTest", "success");
                    response.put("queryResult", rs.getInt("test"));
                }
            }
            
        } catch (Exception e) {
            log.error("Database connection test failed", e);
            response.put("status", "failed");
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
