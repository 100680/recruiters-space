# ============================================
# Cloudflare Worker Deployment Script (Wrangler v3)
# Using API Token Authentication
# ============================================

Write-Host "Cloudflare Worker Deployment Started..."

# Step 1: Ask for API Token
$apiToken = Read-Host "pFSdcLcRy1OE6-acNM_rAO-hoIs93F14ukgkQUVZ"
$env:CLOUDFLARE_API_TOKEN = $apiToken
[System.Environment]::SetEnvironmentVariable("CLOUDFLARE_API_TOKEN", $apiToken, "User")
Write-Host "API token set."

# Step 2: Install Wrangler CLI if not installed
try {
    wrangler --version | Out-Null
    Write-Host "Wrangler is already installed."
} catch {
    Write-Host "Installing Wrangler CLI..."
    npm install -g wrangler
}

# Step 3: Ask for Account ID
$accountId = Read-Host "1aa38c9553c97d08a8742239e7d78308"

# Step 4: Create Worker project

$projectName = "product-categories-worker"
if (!(Test-Path $projectName)) {
    wrangler init $projectName --yes
}
Set-Location $projectName

# Step 5: Create Worker code (JSON API)
@'
export default {
  async fetch(request) {
    const categories = [
        [
		  { "categoryId": 1, "name": "Electronics" },
		  { "categoryId": 2, "name": "Beauty" },
		  { "categoryId": 3, "name": "Car" },
		  { "categoryId": 4, "name": "Furniture" },
		  { "categoryId": 5, "name": "Toys" },
		  { "categoryId": 6, "name": "Watches" }
		]
    ];
    return new Response(JSON.stringify(categories), {
      headers: { "Content-Type": "application/json" }
    });
  }
}
'@ | Out-File -Encoding UTF8 .\src\index.js

# Step 6: Configure wrangler.toml
@"
name = "$projectName"
main = "src/index.js"
compatibility_date = "$(Get-Date -Format yyyy-MM-dd)"
account_id = "$accountId"
"@ | Out-File -Encoding UTF8 .\wrangler.toml

# Step 7: Deploy Worker
Write-Host "Deploying Worker..."
wrangler deploy

Write-Host "Worker deployed successfully!"
