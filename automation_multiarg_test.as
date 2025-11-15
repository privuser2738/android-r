Print("=== Automation Multi-Arg Test ===")
Print("Testing parser with multi-argument calls")
Print("")

# Test with string functions that work without devices
$result1 = Substring("Hello World", 0, 5)
Print("Substring result: " + $result1)

$result2 = Replace("Hello World", "World", "Android")
Print("Replace result: " + $result2)

$result3 = Contains("Hello World", "World")
Print("Contains result: " + ToString($result3))

Print("")
Print("All multi-argument calls parsed successfully!")
Print("=== Test Complete ===")
