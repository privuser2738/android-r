// AndroidScript Demo

Print("=== AndroidScript Demo ===")
Print("")

// Variables and math
$x = 15
$y = 25
$sum = $x + $y
$product = $x * $y

Print("Math operations:")
Print("  " + $x + " + " + $y + " = " + $sum)
Print("  " + $x + " * " + $y + " = " + $product)
Print("")

// Strings
$greeting = "Hello"
$name = "AndroidScript"
$message = $greeting + ", " + $name + "!"

Print("String manipulation:")
Print("  " + $message)
Print("  Uppercase: " + ToUpper($message))
Print("  Length: " + Length($message))
Print("")

// Conditionals
Print("Conditional test:")
if ($sum > 30) {
    Print("  Sum is greater than 30")
} else {
    Print("  Sum is 30 or less")
}
Print("")

// Loops
Print("Loop test (1 to 5):")
for ($i = 1; $i <= 5; $i = $i + 1) {
    Print("  Iteration " + $i)
}
Print("")

// Functions
function Double($n) {
    return $n * 2
}

function Greet($who) {
    return "Hello, " + $who + "!"
}

$doubled = Double(21)
$greeting2 = Greet("World")

Print("Function calls:")
Print("  Double(21) = " + $doubled)
Print("  " + $greeting2)
Print("")

// Automation placeholders
Print("Automation commands (placeholders):")
Tap(500, 1000)
Swipe(100, 100, 500, 500, 300)
Input("test text")
LaunchApp("com.example.app")
Print("")

Print("=== Demo Complete! ===")
