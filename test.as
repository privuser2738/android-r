// Simple test script for AndroidScript

Print("=== AndroidScript Test ===")
Print("")

// Variables
$name = "AndroidScript"
$version = 1.0
Print("Welcome to " + $name + " v" + $version)
Print("")

// Math
$a = 10
$b = 20
$sum = $a + $b
Print("Math: " + $a + " + " + $b + " = " + $sum)
Print("")

// Strings
$text = "hello world"
$upper = ToUpper($text)
Print("String: '" + $text + "' => '" + $upper + "'")
Print("")

// Arrays
$numbers = [1, 2, 3, 4, 5]
Print("Array: " + ToString($numbers))
Print("Array length: " + Count($numbers))
Print("")

// Loops
Print("Loop test:")
for ($i = 1; $i <= 5; $i = $i + 1) {
    Print("  Iteration " + $i)
}
Print("")

// Functions
function Factorial($n) {
    if ($n <= 1) {
        return 1
    }
    return $n * Factorial($n - 1)
}

$result = Factorial(5)
Print("Function: Factorial(5) = " + $result)
Print("")

// Conditionals
if ($sum > 25) {
    Print("Conditional: sum is greater than 25")
} else {
    Print("Conditional: sum is 25 or less")
}
Print("")

// Automation placeholder
Print("Testing automation functions (placeholders):")
Tap(100, 200)
Swipe(100, 200, 300, 400, 500)
Input("test input")
LaunchApp("com.example.app")
Print("")

Print("=== All tests passed! ===")
