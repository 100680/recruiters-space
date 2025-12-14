
INSERT INTO product.categories (name, description)
VALUES 
  ('Electronics', 'Electronic devices and gadgets'),
  ('Beauty', 'Beauty and personal care products'),
  ('Car', 'Automotive products and accessories'),
  ('Furniture', 'Home and office furniture'),
  ('Toys', 'Toys and games for children and adults'),
  ('Watches', 'Wristwatches and smartwatches');
 
 
INSERT INTO product.discount_methods (method_name, description)
VALUES 
  ('PERCENTAGE', 'Discount based on a percentage of the product price (e.g., 10% off)'),
  ('FLAT_AMOUNT', 'Discount with a fixed amount reduction (e.g., $50 off)'),
  ('LOYALTY', 'Discounts offered to loyalty program or membership customers'),
  ('COUPON', 'Discounts applied using coupon or promo codes');
  
DO $$
DECLARE
    product_names TEXT[] := ARRAY[
        'Samsung Galaxy S22', 'iPhone 14 Pro', 'Dell XPS 13 Laptop', 'Sony WH-1000XM5 Headphones', 'Apple iPad Pro',
        'L''Oreal Face Cream', 'Maybelline Lipstick', 'Nivea Body Lotion', 'Dove Shampoo', 'MAC Eyeliner',
        'Michelin Car Tyre', 'Bosch Car Battery', 'Castrol Engine Oil', '3M Car Polish', 'Philips Car Headlight',
        'Ikea Dining Table', 'Recliner Sofa', 'Wooden King Bed', 'Office Ergonomic Chair', 'Bookshelf Cabinet',
        'LEGO Star Wars Set', 'Hot Wheels Car Pack', 'Barbie Dollhouse', 'Rubik''s Cube', 'Remote Control Helicopter',
        'Casio G-Shock', 'Rolex Submariner', 'Apple Watch Series 9', 'Fossil Smartwatch', 'Seiko Chronograph'
    ];
	
	product_images TEXT[] := ARRAY[
        'samsungmobile', 'iphone', 'delllaptop', 'headphone', 'ipad',
        'facecream', 'lipstick', 'babylotion', 'shampoo', 'eyeliner',
        'cartyre', 'carbattery', 'engineoil', 'carpolish', 'carheadlight',
        'diningtable', 'reclinersofa', 'woodenkingbed', 'officechair', 'cabinet',
        'starwarsset', 'carpack', 'dollhouse', 'cube', 'helicopter',
        'gshock', 'rolex', 'applewatch', 'fossil', 'seiko'
    ];
    
    i INTEGER;
    cat_id BIGINT;
    product_name TEXT;
	product_image TEXT;
    product_price NUMERIC(10,2);
    product_stock INTEGER;
    reorder_level INTEGER;
	block_size INTEGER = 5;
	catgories_cnt INTEGER;
    category_ids BIGINT[];
BEGIN
	TRUNCATE TABLE product.product_discounts, product.product_price_history, product.products RESTART IDENTITY;
	
    SELECT ARRAY_AGG(category_id ORDER BY category_id) INTO category_ids
    FROM product.categories WHERE NOT is_deleted;

	catgories_cnt := array_length(category_ids, 1);

    FOR i IN 1..500 LOOP
        cat_id := category_ids[((i - 1) / block_size) % catgories_cnt + 1];
        product_name := product_names[(i - 1) % array_length(product_names, 1) + 1];
        product_image = product_images[(i - 1) % array_length(product_images, 1) + 1];
		product_price := ROUND((10 + random() * 990)::NUMERIC, 2);
        product_stock := FLOOR(5 + random() * 500)::INTEGER; -- ensure at least 5
        reorder_level := GREATEST(1, FLOOR(product_stock * 0.3)); -- e.g. 30% of stock

        INSERT INTO product.products (
            name, description, price, stock, reorder_level, category_id, image_url, created_by
        ) VALUES (
            product_name,
            'High quality ' || product_name,
            product_price,
            product_stock,
            reorder_level,
            cat_id,
            product_image || '.jpg',
            'system_admin'
        );
    END LOOP;

    RAISE NOTICE 'Inserted 500 products successfully!';
END;
$$ LANGUAGE plpgsql;



