# opening-hours-serverless

A simple Clojure application and a serverless ClojureScript function for parsing opening hours from JSON input. 

The local Clojure application parses data from a file, the remote function parses data from HTTP POST body.

# Example input
```json
{
  "monday": [
    {
      "type": "open",
      "value": 3600
    },
    {
      "type": "close",
      "value": 7200
    }
  ],
  "tuesday": []
}
```

# Example output
```shell
Monday: 1 AM - 2 AM
Tuesday: Closed 
```

# Build local Clojure application
```shell
lein uberjar
``` 
# Run tests
````shell
lein test
````
# Run locally
```shell
java -jar target/opening-hours.jar <json-file-path>
```
# Deploy remote ClojureScript function

```shell
$ serverless deploy
```

# Examples
Running Clojure version locally:
```shell
$ java -jar target/opening-hours.jar example.json
Monday: Closed
Tuesday: 10 AM - 6 PM
Wednesday: Closed
Thursday: 10 AM - 6 PM
Friday: 10 AM - 1 AM
Saturday: 10 AM - 1 AM
Sunday: 12 PM - 9 PM
```
Invoking the remote ClojureScript function:
```shell
$ curl -X POST https://bkjycycw6i.execute-api.eu-west-1.amazonaws.com/dev/opening-hours -H 'Content-Type: application/json' -d '{"monday":[],"tuesday":[{"type":"open","value":36000},{"type":"close","value":64800}],"wednesday":[],"thursday":[{"type":"open","value":36000},{"type":"close","value":64800}],"friday":[{"type":"open","value":36000}],"saturday":[{"type":"close","value":3600},{"type":"open","value":36000}],"sunday":[{"type":"close","value":3600},{"type":"open","value":43200},{"type":"close","value":75600}]}'
Monday: Closed
Tuesday: 10 AM - 6 PM
Wednesday: Closed
Thursday: 10 AM - 6 PM
Friday: 10 AM - 1 AM
Saturday: 10 AM - 1 AM
Sunday: 12 PM - 9 PM
```

```shell
$ curl -X POST https://bkjycycw6i.execute-api.eu-west-1.amazonaws.com/dev/opening-hours -H 'Content-Type: application/json' -d '{"friday":[{"type":"open","value":64800}],"saturday":[{"type":"close","value":3600},{"type":"open","value":32400},{"type":"close","value":39600},{"type":"open","value":57600},{"type":"close","value":82800}]}'
Friday: 6 PM - 1 AM
Saturday: 9 AM - 11 AM, 4 PM - 11 PM
```
