param(
  [string]$OutputPath = "data/sample-transactions-10000.csv",
  [int]$Rows = 10000
)

$branches = @("SUC001", "SUC002", "SUC003", "SUC404")
$products = @("PROD001", "PROD002", "PROD003", "PROD404")
$types = @("INGRESO", "EGRESO", "INVALIDO")
$start = Get-Date "2026-01-01"

$dir = Split-Path -Parent $OutputPath
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$writer = New-Object System.IO.StreamWriter($OutputPath, $false, $utf8NoBom)
try {
    $writer.WriteLine("fecha,codigoSucursal,codigoProducto,monto,tipo")
    for ($i = 0; $i -lt $Rows; $i++) {
        $date = $start.AddDays($i % 31).ToString("yyyy-MM-dd")
        $branch = $branches[$i % $branches.Length]
        $product = $products[$i % $products.Length]
        $amount = if ($i % 15 -eq 0) { "-10.00" } else { [string]::Format([System.Globalization.CultureInfo]::InvariantCulture, "{0:0.00}", (10 + ($i % 5000))) }
        $type = $types[$i % $types.Length]
        $writer.WriteLine("$date,$branch,$product,$amount,$type")
    }
}
finally {
    $writer.Dispose()
}
