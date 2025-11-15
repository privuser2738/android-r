Print("=== AndroidScript Test ===")

$x = 10
$y = 20
$sum = $x + $y
Print("Sum: " + $sum)

$text = "hello"
$upper = ToUpper($text)
Print("Uppercase: " + $upper)

if ($sum > 15) {
    Print("Sum is greater than 15")
}

for ($i = 1; $i <= 3; $i = $i + 1) {
    Print("Loop: " + $i)
}

Tap(100, 200)
LaunchApp("com.app")

Print("=== Complete ===")
