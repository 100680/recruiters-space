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
  "Samsung Galaxy S22","iPhone 14 Pro","Dell XPS 13 Laptop","Sony WH-1000XM5 Headphones","Apple iPad Pro",
  "L'Oreal Face Cream","Maybelline Lipstick","Nivea Body Lotion","Dove Shampoo","MAC Eyeliner",
  "Michelin Car Tyre","Bosch Car Battery","Castrol Engine Oil","3M Car Polish","Philips Car Headlight",
  "Ikea Dining Table","Recliner Sofa","Wooden King Bed","Office Ergonomic Chair","Bookshelf Cabinet",
  "LEGO Star Wars Set","Hot Wheels Car Pack","Barbie Dollhouse","Rubik's Cube","Remote Control Helicopter",
  "Casio G-Shock","Rolex Submariner","Apple Watch Series 9","Fossil Smartwatch","Seiko Chronograph"
];

const brands = {
  1: ["Samsung", "Apple", "Dell", "Sony", "HP", "LG"],
  2: ["L'Oreal", "Maybelline", "Nivea", "Dove", "MAC", "Revlon"],
  3: ["Michelin", "Bosch", "Castrol", "3M", "Philips", "Shell"],
  4: ["IKEA", "Ashley", "Wayfair", "West Elm", "CB2", "Generic"],
  5: ["LEGO", "Hot Wheels", "Barbie", "Fisher-Price", "Hasbro", "Mattel"],
  6: ["Casio", "Rolex", "Apple", "Fossil", "Seiko", "Citizen"]
};

async function generateBulkData() {
  console.log('Generating bulk data for 500 products...');
  
  const bulkBody = [];
  
  for (let i = 1; i <= 500; i++) {
    const categoryIndex = ((i - 1) % 6);
    const category = categories[categoryIndex];
    const productName = productNames[(i - 1) % productNames.length];
    const price = parseFloat((10 + Math.random() * 990).toFixed(2));
    const stock = Math.floor(1 + Math.random() * 500);
    const reorderLevel = Math.max(5, Math.floor(stock * 0.2));
    const brand = brands[category.id][Math.floor(Math.random() * brands[category.id].length)];
    
    // Calculate discount info (active for products 1-100)
    const hasActiveDiscount = i <= 100;
    const discountPercentage = hasActiveDiscount ? parseFloat((5 + Math.random() * 15).toFixed(1)) : 0;
    const discountedPrice = hasActiveDiscount ? parseFloat((price * (1 - discountPercentage / 100)).toFixed(2)) : price;
    
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
      description: `High quality ${productName}`,
      price: price,
      discounted_price: discountedPrice,
      discount_percentage: discountPercentage,
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
      image_url: `https://example.com/images/product_${i}.jpg`,
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
    // Uncomment the operation you want to perform:
    
    // 1. Delete the 20 products
    //await deleteProducts();
    
    // 2. Generate and index all 500 products
    await indexProducts();
    
  } catch (error) {
    console.error('Main execution error:', error);
  } finally {
    // Close the client connection
    client.close();
  }
}

// Run the script
main();