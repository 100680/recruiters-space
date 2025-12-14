
// ==========================
// Insert Master Data
// ==========================

// Insert Categories
db.categories.insertMany([
  {
    category_id: NumberLong("1"),
    name: "Electronics",
    description: "Electronic devices and gadgets",
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    category_id: NumberLong("2"),
    name: "Beauty",
    description: "Beauty and personal care products",
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    category_id: NumberLong("3"),
    name: "Car",
    description: "Automotive products and accessories",
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    category_id: NumberLong("4"),
    name: "Furniture",
    description: "Home and office furniture",
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    category_id: NumberLong("5"),
    name: "Toys",
    description: "Toys and games for children and adults",
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    category_id: NumberLong("6"),
    name: "Watches",
    description: "Wristwatches and smartwatches",
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  }
]);

print("Categories inserted successfully!");

// Insert Discount Methods with enhanced fields
db.discount_methods.insertMany([
  {
    discount_method_id: NumberLong("1"),
    method_name: "PERCENTAGE",
    description: "Discount based on a percentage of the product price (e.g., 10% off)",
    is_percentage: true,
    max_discount_value: 50.0000,
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    discount_method_id: NumberLong("2"),
    method_name: "FLAT_AMOUNT",
    description: "Discount with a fixed amount reduction (e.g., $50 off)",
    is_percentage: false,
    max_discount_value: 500.0000,
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    discount_method_id: NumberLong("3"),
    method_name: "LOYALTY",
    description: "Discounts offered to loyalty program or membership customers",
    is_percentage: true,
    max_discount_value: 25.0000,
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  },
  {
    discount_method_id: NumberLong("4"),
    method_name: "COUPON",
    description: "Discounts applied using coupon or promo codes",
    is_percentage: false,
    max_discount_value: 100.0000,
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  }
]);

print("Discount methods inserted successfully!");

// ==========================
// Bulk Insert 500 Products
// ==========================
const productNames = [
  // Electronics
  "Samsung Galaxy S22","iPhone 14 Pro","Dell XPS 13 Laptop","Sony WH-1000XM5 Headphones","Apple iPad Pro",
  // Beauty
  "L'Oreal Face Cream","Maybelline Lipstick","Nivea Body Lotion","Dove Shampoo","MAC Eyeliner",
  // Car
  "Michelin Car Tyre","Bosch Car Battery","Castrol Engine Oil","3M Car Polish","Philips Car Headlight",
  // Furniture
  "Ikea Dining Table","Recliner Sofa","Wooden King Bed","Office Ergonomic Chair","Bookshelf Cabinet",
  // Toys
  "LEGO Star Wars Set","Hot Wheels Car Pack","Barbie Dollhouse","Rubik's Cube","Remote Control Helicopter",
  // Watches
  "Casio G-Shock","Rolex Submariner","Apple Watch Series 9","Fossil Smartwatch","Seiko Chronograph"
];

const totalNames = productNames.length;
let products = [];

for (let i = 1; i <= 500; i++) {
  let cat_id = ((i - 1) % 6) + 1;
  let product_name = productNames[(i - 1) % totalNames];
  let price = parseFloat((10 + Math.random() * 990).toFixed(4));
  let stock = Math.floor(1 + Math.random() * 500);
  let reorder_level = Math.max(5, Math.floor(stock * 0.2));
  let sku = "SKU-" + String(i).padStart(6, '0');
  
  products.push({
    product_id: NumberLong(i.toString()),
    name: product_name,
    description: "High quality " + product_name,
    price: price,
    stock: NumberInt(stock.toString()),
    reorder_level: NumberInt(reorder_level.toString()),
    category_id: NumberLong(cat_id.toString()),
    image_url: "https://example.com/images/product_" + i + ".jpg",
    sku: sku,
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    correlation_id: UUID(),
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  });

  // Progress indicator
  if (i % 100 === 0) {
    print("Prepared " + i + " products...");
  }
}

// Insert all 500 products
try {
  db.products.insertMany(products, { ordered: false });
  print("Inserted 500 products successfully!");
} catch (error) {
  print("Error inserting products: " + error);
  
  // Fallback: insert one by one to identify issues
  let successCount = 0;
  for (let i = 0; i < products.length; i++) {
    try {
      db.products.insertOne(products[i]);
      successCount++;
    } catch (singleError) {
      print("Failed to insert product at index " + i + ": " + singleError);
    }
  }
  print("Successfully inserted: " + successCount + " products via fallback method");
}

// ==========================
// Create Sample Discount Data
// ==========================
print("Creating sample discount data...");

let discounts = [];
let discountId = 1;

// Create active discounts for first 100 products
for (let i = 1; i <= 100; i++) {
  let discountMethodId = ((i - 1) % 4) + 1;
  let isPercentage = [true, false, true, false][discountMethodId - 1]; // PERCENTAGE, FLAT_AMOUNT, LOYALTY, COUPON
  let discountValue;
  
  if (isPercentage) {
    discountValue = parseFloat((5 + Math.random() * 20).toFixed(4)); // 5-25%
  } else {
    discountValue = parseFloat((10 + Math.random() * 90).toFixed(4)); // $10-$100
  }
  
  let startDate = new Date();
  startDate.setDate(startDate.getDate() - 30); // Started 30 days ago
  
  let endDate = new Date();
  endDate.setDate(endDate.getDate() + 30); // Ends 30 days from now
  
  discounts.push({
    product_discount_id: NumberLong(discountId.toString()),
    product_id: NumberLong(i.toString()),
    discount_method_id: NumberLong(discountMethodId.toString()),
    discount_value: discountValue,
    start_date: startDate,
    end_date: endDate,
    active: true,
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  });
  
  discountId++;
}

// Create expired discounts for products 401-450
for (let i = 401; i <= 450; i++) {
  let discountMethodId = ((i - 1) % 4) + 1;
  let isPercentage = [true, false, true, false][discountMethodId - 1];
  let discountValue;
  
  if (isPercentage) {
    discountValue = parseFloat((10 + Math.random() * 15).toFixed(4)); // 10-25%
  } else {
    discountValue = parseFloat((15 + Math.random() * 50).toFixed(4)); // $15-$65
  }
  
  let startDate = new Date();
  startDate.setDate(startDate.getDate() - 90); // Started 90 days ago
  
  let endDate = new Date();
  endDate.setDate(endDate.getDate() - 10); // Ended 10 days ago
  
  discounts.push({
    product_discount_id: NumberLong(discountId.toString()),
    product_id: NumberLong(i.toString()),
    discount_method_id: NumberLong(discountMethodId.toString()),
    discount_value: discountValue,
    start_date: startDate,
    end_date: endDate,
    active: false, // Properly marked as inactive
    created_at: new Date(),
    modified_at: new Date(),
    deleted_at: null,
    is_deleted: false,
    created_by: "system_admin",
    modified_by: null,
    row_version: NumberLong("1")
  });
  
  discountId++;
}

// Insert discount data
try {
  db.product_discounts.insertMany(discounts, { ordered: false });
  print("Inserted " + discounts.length + " discount records successfully!");
} catch (error) {
  print("Error inserting discounts: " + error);
}

// ==========================
// Display Summary Statistics
// ==========================
print("\n=== DATA INSERTION SUMMARY ===");

let productCount = db.products.countDocuments({ is_deleted: false });
let categoryCount = db.categories.countDocuments({ is_deleted: false });
let discountMethodCount = db.discount_methods.countDocuments({ is_deleted: false });
let activeDiscountCount = db.product_discounts.countDocuments({ active: true, is_deleted: false });
let expiredDiscountCount = db.product_discounts.countDocuments({ active: false, is_deleted: false });

print("Products inserted: " + productCount);
print("Categories: " + categoryCount);
print("Discount methods: " + discountMethodCount);
print("Active discounts: " + activeDiscountCount);
print("Expired discounts: " + expiredDiscountCount);

// Low stock products
let lowStockCount = db.products.aggregate([
  {
    $match: {
      is_deleted: false,
      $expr: { $lte: ["$stock", "$reorder_level"] }
    }
  },
  { $count: "low_stock" }
]).toArray();

print("Products with low stock: " + (lowStockCount.length > 0 ? lowStockCount[0].low_stock : 0));
print("================================");

// ==========================
// Test Sample Queries
// ==========================
print("\n=== SAMPLE TEST QUERIES ===");

print("\n1. Products with active discounts (first 5):");
db.products.aggregate([
  {
    $lookup: {
      from: "product_discounts",
      localField: "product_id",
      foreignField: "product_id",
      as: "discounts"
    }
  },
  {
    $match: {
      "discounts.active": true,
      "discounts.is_deleted": false,
      "discounts.start_date": { $lte: new Date() },
      $or: [
        { "discounts.end_date": null },
        { "discounts.end_date": { $gt: new Date() } }
      ]
    }
  },
  {
    $limit: 5
  },
  {
    $project: {
      name: 1,
      price: 1,
      "discounts.discount_value": 1,
      "discounts.start_date": 1,
      "discounts.end_date": 1
    }
  }
]).forEach(printjson);

print("\n2. Low stock products (first 5):");
db.products.find(
  {
    is_deleted: false,
    $expr: { $lte: ["$stock", "$reorder_level"] }
  },
  {
    name: 1,
    stock: 1,
    reorder_level: 1,
    category_id: 1
  }
).limit(5).forEach(printjson);

print("\n3. Products by category count:");
db.products.aggregate([
  {
    $match: { is_deleted: false }
  },
  {
    $group: {
      _id: "$category_id",
      count: { $sum: 1 },
      avg_price: { $avg: "$price" }
    }
  },
  {
    $lookup: {
      from: "categories",
      localField: "_id",
      foreignField: "category_id",
      as: "category"
    }
  },
  {
    $project: {
      category_name: { $arrayElemAt: ["$category.name", 0] },
      product_count: "$count",
      average_price: { $round: ["$avg_price", 2] }
    }
  },
  {
    $sort: { product_count: -1 }
  }
]).forEach(printjson);

print("\n=== SCRIPT COMPLETED SUCCESSFULLY! ===");
print("All collections created, data inserted, and sample queries executed.");
print("Total execution time: " + (new Date().getTime() - new Date().getTime()) + "ms");