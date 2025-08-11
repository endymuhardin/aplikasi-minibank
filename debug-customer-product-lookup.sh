#!/bin/bash

# This script will help debug the customer-product lookup issue
echo "=== Running a simple test to debug customer-product lookups ==="

# Run a simple integration test that checks customer and product APIs
mvn test -Dtest=AccountOpeningTest -Dkarate.options="--tags @debug" 2>&1 | grep -E "(Customer|Product|PERSONAL|CORPORATE|not available)" | head -20