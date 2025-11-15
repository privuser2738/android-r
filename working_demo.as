Print("=== AndroidScript Working Demo ===")
Print("")

// Variables
$x = 10
$y = 20
Print("x = " + $x)
Print("y = " + $y)
Print("")

// Math
$sum = $x + $y
Print("x + y = " + $sum)
Print("")

// Strings
$text = "hello"
$upper = ToUpper($text)
Print("Original: " + $text)
Print("Uppercase: " + $upper)
Print("")

// Conditional
if ($sum > 25) {
    Print("Sum is greater than 25")
}
Print("")

// Loop
Print("Loop from 1 to 3:")
for ($i = 1; $i <= 3; $i = $i + 1) {
    Print("  " + $i)
}
Print("")

// Function
function Add($a, $b) {
    return $a + $b
}

$result = Add(5, 7)
Print("Add(5, 7) = " + $result)
Print("")

// Automation
Print("Automation commands:")
Tap(100, 200)
LaunchApp("com.test.app")
Print("")

Print("=== Demo Complete ===")
