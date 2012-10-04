#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -cp $DIR/jcommander-1.29.jar:$DIR/target/mcp_deobfuscate-1.0.jar:$DIR/asm-all-3.3.1.jar org.ldg.mcpd.MCPDeobfuscate $@
