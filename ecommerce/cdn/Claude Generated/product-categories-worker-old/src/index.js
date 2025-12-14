export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    
    // Handle CORS for browser requests
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization",
    };

    // Handle preflight requests
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    // Categories data - this should ideally come from KV storage or D1 database
    const categories = [
      { "categoryId": 1, "name": "Electronics" },
      { "categoryId": 2, "name": "Beauty" },
      { "categoryId": 3, "name": "Car" },
      { "categoryId": 4, "name": "Furniture" },
      { "categoryId": 5, "name": "Toys" },
      { "categoryId": 6, "name": "Watches" }
    ];

    try {
      if (request.method === "GET") {
        // Add debug endpoint
        if (url.pathname === '/debug') {
          return new Response(JSON.stringify({
            success: true,
            message: "Worker is running correctly!",
            method: request.method,
            url: request.url,
            pathname: url.pathname,
            timestamp: new Date().toISOString(),
            userAgent: request.headers.get('User-Agent')
          }), {
            status: 200,
            headers: {
              "Content-Type": "application/json; charset=utf-8",
              "Cache-Control": "no-cache"
            }
          });
        }

        // Return categories for all other GET requests
        return new Response(JSON.stringify({
          success: true,
          data: categories,
          timestamp: new Date().toISOString()
        }), {
          status: 200,
          headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Cache-Control": "no-cache"
          }
        });
      }
      
      if (request.method === "POST" || request.method === "PUT") {
        // Handle category updates (you'll need to implement KV storage or D1 database)
        const body = await request.json();
        
        // For now, just return success - implement actual storage later
        return new Response(JSON.stringify({
          success: true,
          message: "Categories updated successfully",
          data: body,
          timestamp: new Date().toISOString()
        }), {
          status: 200,
          headers: {
            "Content-Type": "application/json; charset=utf-8"
          }
        });
      }

      // Method not allowed
      return new Response(JSON.stringify({
        success: false,
        error: "Method not allowed"
      }), {
        status: 405,
        headers: {
          "Content-Type": "application/json; charset=utf-8"
        }
      });

    } catch (error) {
      return new Response(JSON.stringify({
        success: false,
        error: "Internal server error"
      }), {
        status: 500,
        headers: {
          "Content-Type": "application/json; charset=utf-8"
        }
      });
    }
  }
}