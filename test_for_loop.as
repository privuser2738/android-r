Print("=== For Loop Test ===")

Print("Test 1: Basic for loop")
for ($i = 1; $i <= 5; $i = $i + 1) {
    Print("Count: " + $i)
}

Print("")
Print("Test 2: For loop with step of 2")
for ($j = 0; $j <= 10; $j = $j + 2) {
    Print("Even: " + $j)
}

Print("")
Print("Test 3: Nested for loops")
for ($x = 1; $x <= 3; $x = $x + 1) {
    for ($y = 1; $y <= 2; $y = $y + 1) {
        Print("  (" + $x + ", " + $y + ")")
    }
}

Print("")
Print("=== All For Loops Passed! ===")
