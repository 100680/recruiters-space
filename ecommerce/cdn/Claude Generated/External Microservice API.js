// Sample External Microservice API Structure
// This represents what your external microservice should implement

// Express.js example (you can adapt this to any framework/language)
const express = require('express');
const app = express();
const port = 3000;

// Middleware
app.use(express.json());
app.use((req, res, next) => {
    // CORS headers
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-API-Key');
    
    // API Key authentication
    const apiKey = req.headers['x-api-key'] || req.headers['authorization']?.replace('Bearer ', '');
    
    // Skip auth for health check
    if (req.path === '/health') {
        return next();
    }
    
    if (!apiKey || apiKey !== process.env.API_KEY) {
        return res.status(401).json({
            success: false,
            error: 'Unauthorized - Invalid API Key'
        });
    }
    
    next();
});

// Sample in-memory storage (replace with your actual database)
let categories = [
    { categoryId: 1, name: "Electronics", description: "Electronic devices and gadgets", createdAt: new Date().toISOString() },
    { categoryId: 2, name: "Beauty", description: "Beauty and personal care products", createdAt: new Date().toISOString() },
    { categoryId: 3, name: "Car", description: "Automotive parts and accessories", createdAt: new Date().toISOString() },
    { categoryId: 4, name: "Furniture", description: "Home and office furniture", createdAt: new Date().toISOString() },
    { categoryId: 5, name: "Toys", description: "Toys and games for all ages", createdAt: new Date().toISOString() },
    { categoryId: 6, name: "Watches", description: "Watches and timepieces", createdAt: new Date().toISOString() }
];

let nextCategoryId = 7;

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({
        success: true,
        status: 'healthy',
        service: 'Categories Microservice',
        version: '1.0.0',
        timestamp: new Date().toISOString(),
        uptime: process.uptime()
    });
});

// GET /api/categories - Get all categories
app.get('/api/categories', (req, res) => {
    try {
        // Optional query parameters for filtering/pagination
        const { page = 1, limit = 100, search, sortBy = 'categoryId' } = req.query;
        
        let filteredCategories = [...categories];
        
        // Search functionality
        if (search) {
            filteredCategories = filteredCategories.filter(cat => 
                cat.name.toLowerCase().includes(search.toLowerCase()) ||
                cat.description.toLowerCase().includes(search.toLowerCase())
            );
        }
        
        // Sorting
        filteredCategories.sort((a, b) => {
            if (sortBy === 'name') {
                return a.name.localeCompare(b.name);
            }
            return a.categoryId - b.categoryId;
        });
        
        // Pagination
        const startIndex = (page - 1) * limit;
        const endIndex = startIndex + parseInt(limit);
        const paginatedCategories = filteredCategories.slice(startIndex, endIndex);
        
        res.json({
            success: true,
            data: paginatedCategories,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: filteredCategories.length,
                totalPages: Math.ceil(filteredCategories.length / limit)
            },
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// GET /api/categories/:id - Get specific category
app.get('/api/categories/:id', (req, res) => {
    try {
        const categoryId = parseInt(req.params.id);
        const category = categories.find(cat => cat.categoryId === categoryId);
        
        if (!category) {
            return res.status(404).json({
                success: false,
                error: 'Category not found',
                categoryId: categoryId
            });
        }
        
        res.json({
            success: true,
            data: category,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// POST /api/categories - Create new category
app.post('/api/categories', (req, res) => {
    try {
        const { name, description } = req.body;
        
        // Validation
        if (!name || name.trim().length === 0) {
            return res.status(400).json({
                success: false,
                error: 'Category name is required'
            });
        }
        
        // Check for duplicate names
        if (categories.find(cat => cat.name.toLowerCase() === name.toLowerCase())) {
            return res.status(409).json({
                success: false,
                error: 'Category with this name already exists'
            });
        }
        
        // Create new category
        const newCategory = {
            categoryId: nextCategoryId++,
            name: name.trim(),
            description: description?.trim() || '',
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };
        
        categories.push(newCategory);
        
        // Log the creation (in real app, this would be proper logging)
        console.log(`Category created: ${JSON.stringify(newCategory)}`);
        
        res.status(201).json({
            success: true,
            message: 'Category created successfully',
            data: newCategory,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// PUT /api/categories/:id - Update category
app.put('/api/categories/:id', (req, res) => {
    try {
        const categoryId = parseInt(req.params.id);
        const { name, description } = req.body;
        
        const categoryIndex = categories.findIndex(cat => cat.categoryId === categoryId);
        
        if (categoryIndex === -1) {
            return res.status(404).json({
                success: false,
                error: 'Category not found',
                categoryId: categoryId
            });
        }
        
        // Validation
        if (name && name.trim().length === 0) {
            return res.status(400).json({
                success: false,
                error: 'Category name cannot be empty'
            });
        }
        
        // Check for duplicate names (excluding current category)
        if (name && categories.find((cat, index) => 
            index !== categoryIndex && cat.name.toLowerCase() === name.toLowerCase())) {
            return res.status(409).json({
                success: false,
                error: 'Category with this name already exists'
            });
        }
        
        // Update category
        const updatedCategory = {
            ...categories[categoryIndex],
            ...(name && { name: name.trim() }),
            ...(description !== undefined && { description: description.trim() }),
            updatedAt: new Date().toISOString()
        };
        
        categories[categoryIndex] = updatedCategory;
        
        // Log the update
        console.log(`Category updated: ${JSON.stringify(updatedCategory)}`);
        
        res.json({
            success: true,
            message: 'Category updated successfully',
            data: updatedCategory,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// DELETE /api/categories/:id - Delete category
app.delete('/api/categories/:id', (req, res) => {
    try {
        const categoryId = parseInt(req.params.id);
        const categoryIndex = categories.findIndex(cat => cat.categoryId === categoryId);
        
        if (categoryIndex === -1) {
            return res.status(404).json({
                success: false,
                error: 'Category not found',
                categoryId: categoryId
            });
        }
        
        // Store deleted category info for logging
        const deletedCategory = categories[categoryIndex];
        
        // Remove category
        categories.splice(categoryIndex, 1);
        
        // Log the deletion
        console.log(`Category deleted: ${JSON.stringify(deletedCategory)}`);
        
        res.json({
            success: true,
            message: 'Category deleted successfully',
            deletedCategory: deletedCategory,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// POST /api/categories/bulk - Bulk operations
app.post('/api/categories/bulk', (req, res) => {
    try {
        const { operation, categories: bulkCategories } = req.body;
        
        if (!operation || !['create', 'update', 'delete'].includes(operation)) {
            return res.status(400).json({
                success: false,
                error: 'Invalid operation. Must be create, update, or delete'
            });
        }
        
        if (!Array.isArray(bulkCategories)) {
            return res.status(400).json({
                success: false,
                error: 'Categories must be an array'
            });
        }
        
        const results = [];
        const errors = [];
        
        for (const category of bulkCategories) {
            try {
                if (operation === 'create') {
                    if (!category.name) {
                        errors.push({ category, error: 'Name is required' });
                        continue;
                    }
                    
                    const newCategory = {
                        categoryId: nextCategoryId++,
                        name: category.name.trim(),
                        description: category.description?.trim() || '',
                        createdAt: new Date().toISOString(),
                        updatedAt: new Date().toISOString()
                    };
                    
                    categories.push(newCategory);
                    results.push(newCategory);
                }
                // Add bulk update and delete logic as needed
                
            } catch (err) {
                errors.push({ category, error: err.message });
            }
        }
        
        res.json({
            success: errors.length === 0,
            message: `Bulk ${operation} completed`,
            results: results,
            errors: errors,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// GET /api/categories/stats - Get category statistics
app.get('/api/categories/stats', (req, res) => {
    try {
        const stats = {
            totalCategories: categories.length,
            categoriesWithDescription: categories.filter(cat => cat.description && cat.description.length > 0).length,
            averageNameLength: categories.reduce((sum, cat) => sum + cat.name.length, 0) / categories.length,
            oldestCategory: categories.reduce((oldest, cat) => 
                new Date(cat.createdAt) < new Date(oldest.createdAt) ? cat : oldest
            ),
            newestCategory: categories.reduce((newest, cat) => 
                new Date(cat.createdAt) > new Date(newest.createdAt) ? cat : newest
            )
        };
        
        res.json({
            success: true,
            data: stats,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: error.message
        });
    }
});

// Error handling middleware
app.use((error, req, res, next) => {
    console.error('Unhandled error:', error);
    res.status(500).json({
        success: false,
        error: 'Internal server error',
        message: error.message
    });
});

// 404 handler
app.use('*', (req, res) => {
    res.status(404).json({
        success: false,
        error: 'Endpoint not found',
        path: req.originalUrl,
        availableEndpoints: [
            'GET /health',
            'GET /api/categories',
            'POST /api/categories',
            'GET /api/categories/:id',
            'PUT /api/categories/:id',
            'DELETE /api/categories/:id',
            'POST /api/categories/bulk',
            'GET /api/categories/stats'
        ]
    });
});

// Start server
app.listen(port, () => {
    console.log(`Categories Microservice running on port ${port}`);
    console.log(`Health check: http://localhost:${port}/health`);
    console.log(`API Base URL: http://localhost:${port}/api`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('Received SIGTERM, shutting down gracefully');
    process.exit(0);
});

process.on('SIGINT', () => {
    console.log('Received SIGINT, shutting down gracefully');
    process.exit(0);
});

module.exports = app;