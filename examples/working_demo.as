// AndroidScript Demo - Basic Features
// Note: Function declarations not yet implemented in parser

Print("=== AndroidScript Demo ===")
Print("")

// Variables and assignments
$message = "Hello from AndroidScript"
Print($message)
Print("")

// Math operations
$x = 15
$y = 25
$sum = $x + $y
$product = $x * $y

Print("Math:")
Print("  15 + 25 = " + $sum)
Print("  15 * 25 = " + $product)
Print("")

// String operations
$text = "hello world"
$upper = ToUpper($text)
$lower = ToLower("HELLO WORLD")

Print("Strings:")
Print("  Original: " + $text)
Print("  Uppercase: " + $upper)
Print("  Lowercase: " + $lower)
Print("")

// Conditionals
Print("Conditional:")
if ($sum > 30) {
    Print("  Sum is greater than 30")
} else {
    Print("  Sum is less than or equal to 30")
}
Print("")

// Loops
Print("For loop (1 to 5):")
for ($i = 1; $i <= 5; $i = $i + 1) {
    Print("  Iteration: " + $i)
}
Print("")

// Automation commands (placeholders)
Print("UI Automation (placeholders):")
Tap(500, 1000)
Swipe(100, 100, 500, 500, 300)
Input("Hello Android")
LaunchApp("com.example.app")
Print("")

Print("=== Demo Complete! ===")
Print("The interpreter is working!")
