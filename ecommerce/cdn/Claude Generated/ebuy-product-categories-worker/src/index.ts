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

    console.log(`Request: ${request.method} ${url.pathname}`);

    const corsHeaders = {
      "Access-Control-Allow-Origin": "*", // Or restrict to http://localhost:5173 for dev
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization, User-Agent, Accept",
      "Access-Control-Max-Age": "86400",
    };

    // Preflight request
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

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
        const response: APIResponse = {
          success: true,
          data: categories,
          timestamp: new Date().toISOString(),
        };

        return new Response(JSON.stringify(response), {
          status: 200,
          headers: {
            ...corsHeaders,
            "Content-Type": "application/json; charset=utf-8",
            "Cache-Control": "no-cache",
          },
        });
      }

      if (request.method === "POST" || request.method === "PUT") {
        const body = await request.json() as { categories: Category[] };

        const response: APIResponse = {
          success: true,
          message: "Categories updated successfully",
          data: body.categories,
          timestamp: new Date().toISOString(),
        };

        return new Response(JSON.stringify(response), {
          headers: {
            ...corsHeaders,
            "Content-Type": "application/json",
          },
        });
      }

      const errorResponse: APIResponse = {
        success: false,
        error: "Method not allowed",
        timestamp: new Date().toISOString(),
      };

      return new Response(JSON.stringify(errorResponse), {
        status: 405,
        headers: {
          ...corsHeaders,
          "Content-Type": "application/json",
        },
      });

    } catch (error) {
      const errorResponse: APIResponse = {
        success: false,
        error: "Internal server error",
        timestamp: new Date().toISOString(),
      };

      return new Response(JSON.stringify(errorResponse), {
        status: 500,
        headers: {
          ...corsHeaders,
          "Content-Type": "application/json",
        },
      });
    }
  },
} satisfies ExportedHandler<Env>;
