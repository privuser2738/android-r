Print("=== Multi-Argument Function Call Test ===")
Print("")

Print("Test 1: Print with multiple arguments")
Print("Hello", "World", "!")

Print("")
Print("Test 2: String functions with multiple args")
$text = "Hello World"
$sub = Substring($text, 0, 5)
Print("Substring result: " + $sub)

Print("")
Print("Test 3: Replace function (3 arguments)")
$original = "Hello World"
$replaced = Replace($original, "World", "Android")
Print("Replace result: " + $replaced)

Print("")
Print("Test 4: Contains function (2 arguments)")
$hasWorld = Contains("Hello World", "World")
Print("Contains result: " + ToString($hasWorld))

Print("")
Print("=== Test Complete ===")
