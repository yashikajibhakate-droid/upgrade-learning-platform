#!/bin/bash
curl -v -X POST http://localhost:8080/api/users/preferences \
-H "Content-Type: application/json" \
-d '{
    "email": "debug@example.com",
    "interests": ["Python Programming"]
}'
