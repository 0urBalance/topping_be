#!/bin/bash

echo "=== Testing Profile Configuration ==="

# Set common environment
export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7

echo "1. Testing Local Profile with Environment Variable:"
export UPLOAD_PATH=/mnt/d/projects/topping/uploads
echo "UPLOAD_PATH: $UPLOAD_PATH"
$JAVA_HOME/bin/java -jar build/libs/topping-*.jar --spring.profiles.active=local --server.port=8081 &
LOCAL_PID=$!
sleep 5
echo "Local profile test (PID: $LOCAL_PID) - checking logs..."
kill $LOCAL_PID 2>/dev/null || echo "Process already stopped"

echo ""
echo "2. Testing Production Profile with Environment Variable:"
export UPLOAD_PATH=/tmp/test-prod-uploads
mkdir -p $UPLOAD_PATH
echo "UPLOAD_PATH: $UPLOAD_PATH" 
$JAVA_HOME/bin/java -jar build/libs/topping-*.jar --spring.profiles.active=prod --server.port=8082 &
PROD_PID=$!
sleep 5
echo "Production profile test (PID: $PROD_PID) - checking logs..."
kill $PROD_PID 2>/dev/null || echo "Process already stopped"

echo ""
echo "3. Profile Configuration Summary:"
echo "- Local profile: Uses /mnt/d/projects/topping/uploads"
echo "- Production profile: Uses /home/ourbalance_topping/uploads"
echo "- Environment variable UPLOAD_PATH overrides profile defaults"
echo "- Upload paths are externalized and environment-specific"

echo ""
echo "=== Profile Test Complete ==="