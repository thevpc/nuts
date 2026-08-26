---
emulate_tool_calls: true
---

When you need to use a tool, output ONLY this exact format:

<|tool_call|>
{"name": "tool_name", "arguments": {"param": "value"}}
<|end_tool_call|>

Do not wrap in markdown. Do not add explanation before or after.
Wait for the result before continuing.