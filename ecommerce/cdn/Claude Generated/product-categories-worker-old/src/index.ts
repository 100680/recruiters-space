/**
 * Product Categories API Worker
 * Provides CRUD operations for product categories
 */

interface Category {
  categoryId: number;
  name: string;
}

interface APIResponse {
  success: boolean;
  data?: Category[];
  message?: string;
  error?: string;
  timestamp: string;
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    
    // Log request for debugging
    console.log(`Request: ${request.method} ${url.pathname}`);
    console.log(`User-Agent: ${request.headers.get('User-Agent')}`);
    
    // Handle CORS for browser requests
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization, User-Agent, Accept",
      "Access-Control-Max-Age": "86400",
    };

    // Handle preflight requests
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    // Categories data - this should ideally come from KV storage or D1 database
    const categories: Category[] = [
      { categoryId: 1, name: "Electronics" },
      { categoryId: 2, name: "Beauty" },
      { categoryId: 3, name: "Car" },
      { categoryId: 4, name: "Furniture" },
      { categoryId: 5, name: "Toys" },
      { categoryId: 6, name: "Watches" }
    ];

    try {
      if (request.method === "GET") {
        // Return categories
        const response: APIResponse = {
          success: true,
          data: categories,
          timestamp: new Date().toISOString()
        };

        return new Response(JSON.stringify(response), {
          status: 200,
          headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Cache-Control": "no-cache"
          }
        });
      }
      
      if (request.method === "POST" || request.method === "PUT") {
        // Handle category updates (you'll need to implement KV storage or D1 database)
        const body = await request.json() as { categories: Category[] };
        
        // For now, just return success - implement actual storage later
        const response: APIResponse = {
          success: true,
          message: "Categories updated successfully",
          data: body.categories,
          timestamp: new Date().toISOString()
        };

        return new Response(JSON.stringify(response), {
          headers: {
            "Content-Type": "application/json"
          }
        });
      }

      // Method not allowed
      const errorResponse: APIResponse = {
        success: false,
        error: "Method not allowed",
        timestamp: new Date().toISOString()
      };

      return new Response(JSON.stringify(errorResponse), {
        status: 405,
        headers: {
          "Content-Type": "application/json"
        }
      });

    } catch (error) {
      const errorResponse: APIResponse = {
        success: false,
        error: "Internal server error",
        timestamp: new Date().toISOString()
      };

      return new Response(JSON.stringify(errorResponse), {
        status: 500,
        headers: {
          "Content-Type": "application/json"
        }
      });
    }
  }
} satisfies ExportedHandler<Env>;