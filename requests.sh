#!/usr/bin/env bash

for i in {1..100}
do
    curl -X POST "localhost:9000/api/convert" --data '{"fromCurrency": "GBP", "toCurrency": "EUR", "amount": 987}' &
done