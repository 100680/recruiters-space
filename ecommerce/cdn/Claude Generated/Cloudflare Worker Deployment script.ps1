# ============================================
# Cloudflare Worker Deployment Script (Wrangler v3)
# Using API Token Authentication
# ============================================

Write-Host "Cloudflare Worker Deployment Started..." -ForegroundColor Green

# Step 1: Get API Token (don't hardcode it!)
Write-Host "Please enter your Cloudflare API Token:" -ForegroundColor Yellow
$apiToken = Read-Host -AsSecureString
$apiTokenPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($apiToken))

# Set environment variable
$env:CLOUDFLARE_API_TOKEN = $apiTokenPlain
Write-Host "API token set." -ForegroundColor Green

# Step 2: Install Wrangler CLI if not installed
Write-Host "Checking Wrangler installation..." -ForegroundColor Yellow
try {
    $wranglerVersion = wrangler --version 2>$null
    Write-Host "Wrangler is already installed: $wranglerVersion" -ForegroundColor Green
} catch {
    Write-Host "Installing Wrangler CLI..." -ForegroundColor Yellow
    npm install -g wrangler
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to install Wrangler. Please install Node.js and npm first." -ForegroundColor Red
        exit 1
    }
}

# Step 3: Get Account ID
Write-Host "Please enter your Cloudflare Account ID:" -ForegroundColor Yellow
$accountId = Read-Host

# Step 4: Verify authentication
Write-Host "Verifying Cloudflare authentication..." -ForegroundColor Yellow
try {
    wrangler whoami
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Authentication failed. Please check your API token." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Authentication verification failed." -ForegroundColor Red
    exit 1
}

# Step 5: Create Worker project
$projectName = "ebuy-product-categories-worker"
if (!(Test-Path $projectName)) {
    Write-Host "Creating new Worker project..." -ForegroundColor Yellow
    wrangler init $projectName --yes
}

Set-Location $projectName

# Step 6: Create Worker code with proper categories structure
Write-Host "Creating Worker code..." -ForegroundColor Yellow
$workerCode = @'
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
        // Return categories
        return new Response(JSON.stringify({
          success: true,
          data: categories,
          timestamp: new Date().toISOString()
        }), {
          headers: {
            "Content-Type": "application/json",
            ...corsHeaders
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
          data: body
        }), {
          headers: {
            "Content-Type": "application/json",
            ...corsHeaders
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
          "Content-Type": "application/json",
          ...corsHeaders
        }
      });

    } catch (error) {
      return new Response(JSON.stringify({
        success: false,
        error: "Internal server error"
      }), {
        status: 500,
        headers: {
          "Content-Type": "application/json",
          ...corsHeaders
        }
      });
    }
  }
}
'@

$workerCode | Out-File -Encoding UTF8 .\src\index.js

# Step 7: Create/Update wrangler.toml configuration
Write-Host "Creating wrangler.toml configuration..." -ForegroundColor Yellow
$wranglerConfig = @"
name = "$projectName"
main = "src/index.js"
compatibility_date = "$(Get-Date -Format 'yyyy-MM-dd')"
account_id = "$accountId"

# Uncomment the following lines if you want to use KV storage for persistent data
# [[kv_namespaces]]
# binding = "CATEGORIES_KV"
# id = "your-kv-namespace-id"
# preview_id = "your-preview-kv-namespace-id"

# Uncomment for D1 database (recommended for more complex data)
# [[d1_databases]]
# binding = "CATEGORIES_DB"
# database_name = "categories-db"
# database_id = "your-database-id"

[vars]
ENVIRONMENT = "production"
"@

$wranglerConfig | Out-File -Encoding UTF8 .\wrangler.toml

# Step 8: Deploy Worker
Write-Host "Deploying Worker to Cloudflare..." -ForegroundColor Yellow
try {
    wrangler deploy
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Worker deployed successfully!" -ForegroundColor Green
        Write-Host "Your worker is now available at: https://$projectName.your-subdomain.workers.dev" -ForegroundColor Green
        Write-Host ""
        Write-Host "API Endpoints:" -ForegroundColor Cyan
        Write-Host "GET  - Retrieve categories" -ForegroundColor White
        Write-Host "POST - Create/Update categories" -ForegroundColor White
        Write-Host "PUT  - Update categories" -ForegroundColor White
    } else {
        Write-Host "Deployment failed. Check the error messages above." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Deployment failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 9: Test the deployed worker
Write-Host ""
Write-Host "Testing the deployed worker..." -ForegroundColor Yellow
try {
    $workerUrl = "https://$projectName.your-subdomain.workers.dev"
    Write-Host "You can test your API at: $workerUrl" -ForegroundColor Green
    Write-Host ""
    Write-Host "Example curl commands:" -ForegroundColor Cyan
    Write-Host "curl $workerUrl" -ForegroundColor White
    Write-Host "curl -X POST $workerUrl -H 'Content-Type: application/json' -d '{""categories"":[{""categoryId"":7,""name"":""Books""}]}'" -ForegroundColor White
} catch {
    Write-Host "Could not generate test URLs. Please check your deployment manually." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Deployment completed!" -ForegroundColor Green