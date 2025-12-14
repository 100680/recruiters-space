const { Client } = require('@opensearch-project/opensearch');
const fs = require('fs-extra');
const { v4: uuidv4 } = require('uuid');

// OpenSearch client configuration
const client = new Client({
  node: 'https://localhost:9200', // Replace with your OpenSearch endpoint
  auth: {
    username: 'admin',
    password: 'MyStr0ngP@ssw0rd!'
  },
  ssl: {
    rejectUnauthorized: false
  }
});

const categories = [
  {id: 1, name: "Electronics", path: "electronics"},
  {id: 2, name: "Beauty", path: "beauty"},
  {id: 3, name: "Car", path: "car"},
  {id: 4, name: "Furniture", path: "furniture"},
  {id: 5, name: "Toys", path: "toys"},
  {id: 6, name: "Watches", path: "watches"}
];

const productNames = [
  "Samsung Galaxy S22", "iPhone 14 Pro", "Dell XPS 13 Laptop", "Sony WH-1000XM5 Headphones", "Apple iPad Pro",
  "L'Oreal Face Cream", "Maybelline Lipstick", "Nivea Body Lotion", "Dove Shampoo", "MAC Eyeliner",
  "Michelin Car Tyre", "Bosch Car Battery", "Castrol Engine Oil", "3M Car Polish", "Philips Car Headlight",
  "Ikea Dining Table", "Recliner Sofa", "Wooden King Bed", "Office Ergonomic Chair", "Bookshelf Cabinet",
  "LEGO Star Wars Set", "Hot Wheels Car Pack", "Barbie Dollhouse", "Rubik's Cube", "Remote Control Helicopter",
  "Casio G-Shock", "Rolex Submariner", "Apple Watch Series 9", "Fossil Smartwatch", "Seiko Chronograph"
];

const productImages = [
  'samsungmobile', 'iphone', 'delllaptop', 'headphone', 'ipad',
  'facecream', 'lipstick', 'babylotion', 'shampoo', 'eyeliner',
  'cartyre', 'carbattery', 'engineoil', 'carpolish', 'carheadlight',
  'diningtable', 'reclinersofa', 'woodenkingbed', 'officechair', 'cabinet',
  'starwarsset', 'carpack', 'dollhouse', 'cube', 'helicopter',
  'gshock', 'rolex', 'applewatch', 'fossil', 'seiko'
];

const brands = {
  1: ["Samsung", "Apple", "Dell", "Sony", "HP", "LG"],
  2: ["L'Oreal", "Maybelline", "Nivea", "Dove", "MAC", "Revlon"],
  3: ["Michelin", "Bosch", "Castrol", "3M", "Philips", "Shell"],
  4: ["IKEA", "Ashley", "Wayfair", "West Elm", "CB2", "Generic"],
  5: ["LEGO", "Hot Wheels", "Barbie", "Fisher-Price", "Hasbro", "Mattel"],
  6: ["Casio", "Rolex", "Apple", "Fossil", "Seiko", "Citizen"]
};

// Discount methods matching PostgreSQL script
const discountMethods = [
  'PERCENTAGE',
  'FLAT_AMOUNT', 
  'LOYALTY',
  'COUPON'
];

async function generateBulkData() {
  console.log('Generating bulk data for 500 products...');
  
  const bulkBody = [];
  const blockSize = 5; // Matching PostgreSQL logic
  const categoriesCount = categories.length;
  
  for (let i = 1; i <= 500; i++) {
    // Match PostgreSQL category assignment logic: ((i - 1) / block_size) % categories_count + 1
    const categoryIndex = Math.floor((i - 1) / blockSize) % categoriesCount;
    const category = categories[categoryIndex];
    
    // Match PostgreSQL product name assignment: (i - 1) % array_length + 1
    const productName = productNames[(i - 1) % productNames.length];
    
    // Match PostgreSQL image assignment: (i - 1) % array_length + 1  
    const productImage = productImages[(i - 1) % productImages.length];
    
    // Match PostgreSQL price logic: ROUND((10 + random() * 990)::NUMERIC, 2)
    const price = parseFloat((10 + Math.random() * 990).toFixed(2));
    
    // Match PostgreSQL stock logic: FLOOR(5 + random() * 500) - ensure at least 5
    const stock = Math.floor(5 + Math.random() * 500);
    
    // Match PostgreSQL reorder level logic: GREATEST(1, FLOOR(stock * 0.3)) - 30% of stock
    const reorderLevel = Math.max(1, Math.floor(stock * 0.3));
    
    const brand = brands[category.id][Math.floor(Math.random() * brands[category.id].length)];
    
    // Calculate discount info (you can adjust this logic as needed)
    const hasActiveDiscount = Math.random() < 0.2; // 20% chance of having discount
    const discountPercentage = hasActiveDiscount ? parseFloat((5 + Math.random() * 15).toFixed(1)) : 0;
    const discountedPrice = hasActiveDiscount ? parseFloat((price * (1 - discountPercentage / 100)).toFixed(2)) : price;
    const discountMethod = hasActiveDiscount ? discountMethods[Math.floor(Math.random() * discountMethods.length)] : null;
    
    // Stock status logic
    const isInStock = stock > 0;
    let stockStatus = "out_of_stock";
    if (stock > reorderLevel) stockStatus = "in_stock";
    else if (stock > 0) stockStatus = "low_stock";
    
    // Generate tags and search keywords
    const tags = [category.name.toLowerCase(), productName.toLowerCase().split(' ')[0], brand.toLowerCase()];
    const searchKeywords = `${productName} ${brand} ${category.name}`.toLowerCase();
    
    // Add to bulk body
    bulkBody.push({
      index: {
        _index: 'products',
        _id: i.toString()
      }
    });
    
    bulkBody.push({
      product_id: i,
      name: productName,
      description: `High quality ${productName}`, // Matching PostgreSQL description
      price: price,
      discounted_price: discountedPrice,
      discount_percentage: discountPercentage,
      discount_method: discountMethod,
      has_active_discount: hasActiveDiscount,
      stock: stock,
      reorder_level: reorderLevel,
      is_in_stock: isInStock,
      stock_status: stockStatus,
      category: {
        category_id: category.id,
        name: category.name,
        path: category.path
      },
      sku: `SKU-${String(i).padStart(6, '0')}`,
      image_url: `${productImage}.jpg`, // Matching PostgreSQL image_url format
      tags: tags,
      brand: brand,
      rating: {
        average: parseFloat((3.5 + Math.random() * 1.5).toFixed(1)),
        count: Math.floor(20 + Math.random() * 300)
      },
      popularity_score: parseFloat((5 + Math.random() * 5).toFixed(1)),
      search_keywords: searchKeywords,
      created_at: new Date().toISOString(),
      modified_at: new Date().toISOString(),
      created_by: 'system_admin', // Matching PostgreSQL created_by
      is_deleted: false,
      is_active: true,
      sort_order: i,
      search_boost: parseFloat((0.8 + Math.random() * 0.8).toFixed(1))
    });
    
    if (i % 100 === 0) {
      console.log(`Generated ${i} products...`);
    }
  }
  
  return bulkBody;
}

async function createIndex() {
  try {
    console.log('Creating products index...');
    
    const indexExists = await client.indices.exists({ index: 'products' });
    
    if (indexExists.body) {
      console.log('Index already exists. Deleting...');
      await client.indices.delete({ index: 'products' });
    }
    
    const response = await client.indices.create({
      index: 'products',
      body: {
        settings: {
          number_of_shards: 1,
          number_of_replicas: 0,
          analysis: {
            analyzer: {
              product_analyzer: {
                type: 'custom',
                tokenizer: 'standard',
                filter: ['lowercase', 'stop']
              }
            }
          }
        },
        mappings: {
          properties: {
            product_id: { type: 'long' },
            name: { 
              type: 'text', 
              analyzer: 'product_analyzer',
              fields: {
                keyword: { type: 'keyword' }
              }
            },
            description: { type: 'text', analyzer: 'product_analyzer' },
            price: { type: 'double' },
            discounted_price: { type: 'double' },
            discount_percentage: { type: 'float' },
            discount_method: { type: 'keyword' },
            has_active_discount: { type: 'boolean' },
            stock: { type: 'integer' },
            reorder_level: { type: 'integer' },
            is_in_stock: { type: 'boolean' },
            stock_status: { type: 'keyword' },
            category: {
              properties: {
                category_id: { type: 'long' },
                name: { type: 'keyword' },
                path: { type: 'keyword' }
              }
            },
            sku: { type: 'keyword' },
            image_url: { type: 'keyword' },
            tags: { type: 'keyword' },
            brand: { type: 'keyword' },
            rating: {
              properties: {
                average: { type: 'float' },
                count: { type: 'integer' }
              }
            },
            popularity_score: { type: 'float' },
            search_keywords: { type: 'text', analyzer: 'product_analyzer' },
            created_at: { type: 'date' },
            modified_at: { type: 'date' },
            created_by: { type: 'keyword' },
            is_deleted: { type: 'boolean' },
            is_active: { type: 'boolean' },
            sort_order: { type: 'integer' },
            search_boost: { type: 'float' }
          }
        }
      }
    });
    
    console.log('Index created successfully');
    
  } catch (error) {
    console.error('Error creating index:', error);
    throw error;
  }
}

async function indexProducts() {
  try {
    console.log('Starting bulk indexing...');
    const bulkBody = await generateBulkData();
    
    // Split into batches of 100 for better performance
    const batchSize = 100;
    for (let i = 0; i < bulkBody.length; i += batchSize * 2) { // *2 because each product has 2 entries (index + doc)
      const batch = bulkBody.slice(i, i + batchSize * 2);
      
      const response = await client.bulk({
        refresh: true,
        body: batch
      });
      
      if (response.body.errors) {
        console.error('Bulk indexing errors:', response.body.items.filter(item => item.index.error));
      } else {
        console.log(`Batch ${Math.floor(i / (batchSize * 2)) + 1} indexed successfully`);
      }
    }
    
    console.log('All products indexed successfully!');
    
    // Verify count
    const countResponse = await client.count({
      index: 'products'
    });
    console.log(`Total products in index: ${countResponse.body.count}`);
    
    // Show category distribution to verify it matches PostgreSQL logic
    const categoryAggResponse = await client.search({
      index: 'products',
      body: {
        size: 0,
        aggs: {
          categories: {
            terms: {
              field: 'category.name',
              size: 10
            }
          }
        }
      }
    });
    
    console.log('Category distribution:');
    categoryAggResponse.body.aggregations.categories.buckets.forEach(bucket => {
      console.log(`${bucket.key}: ${bucket.doc_count} products`);
    });
    
  } catch (error) {
    console.error('Error during bulk indexing:', error);
  }
}

async function deleteProducts() {
  try {
    console.log('Deleting products 1-20...');
    
    // Option A: Delete by query (product_id range)
    const deleteResponse = await client.deleteByQuery({
      index: 'products',
      refresh: true,
      body: {
        query: {
          range: {
            product_id: {
              gte: 1,
              lte: 20
            }
          }
        }
      }
    });
    
    console.log(`Deleted ${deleteResponse.body.deleted} products`);
    
    // Verify deletion
    const searchResponse = await client.search({
      index: 'products',
      body: {
        query: {
          range: {
            product_id: {
              gte: 1,
              lte: 20
            }
          }
        }
      }
    });
    
    console.log(`Remaining products in range 1-20: ${searchResponse.body.hits.total.value}`);
    
  } catch (error) {
    console.error('Error during deletion:', error);
  }
}

// Main execution
async function main() {
  try {
    // Uncomment the operations you want to perform:
    
    // 1. Create the index with proper mappings
    await createIndex();
    
    // 2. Generate and index all 500 products
    await indexProducts();
    
    // 3. Delete the first 20 products (uncomment if needed)
    // await deleteProducts();
    
  } catch (error) {
    console.error('Main execution error:', error);
  } finally {
    // Close the client connection
    client.close();
  }
}

// Run the script
main();