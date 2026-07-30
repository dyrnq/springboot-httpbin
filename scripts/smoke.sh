#!/usr/bin/env bash
#
# Lightweight, self-contained HTTP smoke test for springboot-httpbin.
#
# Usage:  scripts/smoke.sh <servlet_port> <reactive_port>
#
# No external network access. Uses only curl, jq, and brotli.
# Exits non-zero on the first failure with a concise diagnostic.
#
set -uo pipefail

SERVLET_PORT="${1:-8080}"
REACTIVE_PORT="${2:-8081}"

PASS=0
FAIL=0
FAIL_NAMES=()

# Colors (only if stdout is a TTY)
if [ -t 1 ]; then
  RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'; RESET=$'\033[0m'
else
  RED=""; GREEN=""; YELLOW=""; RESET=""
fi

log_pass() { echo "${GREEN}✓${RESET} $1"; PASS=$((PASS + 1)); }
log_fail() { echo "${RED}✗${RESET} $1"; FAIL=$((FAIL + 1)); FAIL_NAMES+=("$1"); }
section()  { echo; echo "${YELLOW}== $1 ==${RESET}"; }

# Run a curl request and split status / headers / body into temp files.
# Usage: do_curl <port> <out_headers> <out_body> <path> [curl args ...]
# The path is appended to "http://127.0.0.1:${port}" as a single URL
# so callers don't accidentally pass two URLs to curl.
do_curl() {
  local port="$1" headers_file="$2" body_file="$3" path="$4"; shift 4
  # -w on stdout is captured by command substitution; suppress the
  # "ignored null byte in input" warning that surfaces when the body
  # is binary (gzip/brotli). We only read the status code from stdout.
  curl -sS -o "$body_file" -D "$headers_file" -w '%{http_code}' \
       --max-time 10 \
       "http://127.0.0.1:${port}${path}" "$@" 2>/dev/null
}

# expect_status <port> <expected_status> <name> [curl args ...] <path>
# The last argument is treated as the URL path and passed to do_curl as
# the 4th argument so curl sees a single URL.
expect_status() {
  local port="$1" expected="$2" name="$3"; shift 3
  local path="${@: -1}"
  # Strip the path out of "$@" so we don't pass it twice.
  local args=()
  if [ "$#" -gt 1 ]; then
    args=("${@:1:$#-1}")
  fi
  local status body
  status=$(do_curl "$port" /tmp/.smoke.headers /tmp/.smoke.body "$path" "${args[@]}")
  body=$(head -c 200 /tmp/.smoke.body)
  if [ "$status" = "$expected" ]; then
    log_pass "$name (port=${port}, ${status})"
  else
    log_fail "$name (port=${port}, expected ${expected} got ${status})"
    echo "  body: ${body}"
  fi
}

# expect_header <name> <expected_substring> <label>
expect_header() {
  local name="$1" expected="$2" label="$3"
  if grep -qi "^${name}:" /tmp/.smoke.headers && \
     grep -i "^${name}:" /tmp/.smoke.headers | grep -qi "${expected}"; then
    log_pass "${label} (${name} contains '${expected}')"
  else
    log_fail "${label} (${name} missing or does not contain '${expected}')"
    grep -i "^${name}:" /tmp/.smoke.headers || echo "  (no ${name} header)"
  fi
}

# expect_json_field <jq_filter> <expected> <label>
expect_json_field() {
  local filter="$1" expected="$2" label="$3"
  local actual
  actual=$(jq -r "$filter" /tmp/.smoke.body 2>/dev/null || echo '<parse-error>')
  if [ "$actual" = "$expected" ]; then
    log_pass "${label} (${filter} == '${expected}')"
  else
    log_fail "${label} (${filter} expected '${expected}' got '${actual}')"
    head -c 200 /tmp/.smoke.body
  fi
}

# expect_body_contains <substring> <label>
expect_body_contains() {
  local needle="$1" label="$2"
  if grep -qF "$needle" /tmp/.smoke.body; then
    log_pass "${label} (body contains '$needle')"
  else
    log_fail "${label} (body does not contain '$needle')"
    head -c 200 /tmp/.smoke.body
  fi
}

# Smoke-test a port by running all checks against it.
smoke_port() {
  local port="$1" label="$2"

  section "${label} (port ${port}) — HTTP methods"

  expect_status "$port" 200 "${label}: GET /anything" \
    -X GET --get --data-urlencode "a=b" --data-urlencode "x=中文" /anything
  expect_json_field '.method' 'GET' "${label}: GET method echoed"

  expect_status "$port" 200 "${label}: POST /anything" \
    -X POST --data-urlencode "k=v" /anything
  expect_json_field '.method' 'POST' "${label}: POST method echoed"

  expect_status "$port" 200 "${label}: PUT /anything" \
    -X PUT --data-urlencode "k=v" /anything
  expect_json_field '.method' 'PUT' "${label}: PUT method echoed"

  expect_status "$port" 200 "${label}: PATCH /anything" \
    -X PATCH --data-urlencode "k=v" /anything
  expect_json_field '.method' 'PATCH' "${label}: PATCH method echoed"

  expect_status "$port" 200 "${label}: DELETE /anything" \
    -X DELETE --data-urlencode "k=v" /anything
  expect_json_field '.method' 'DELETE' "${label}: DELETE method echoed"

  section "${label} — Request inspection"

  expect_status "$port" 200 "${label}: GET /ip" -X GET /ip
  expect_json_field '.origin | type' 'string' "${label}: /ip returns origin"

  expect_status "$port" 200 "${label}: GET /user-agent" \
    -H 'User-Agent: smoke/1.0' /user-agent
  expect_json_field '.["user-agent"]' 'smoke/1.0' "${label}: user-agent echoed"

  expect_status "$port" 200 "${label}: GET /headers" \
    -H 'x-smoke: yes' /headers
  expect_json_field '.headers["x-smoke"]' 'yes' "${label}: custom header echoed"

  section "${label} — Status codes"

  expect_status "$port" 200 "${label}: GET /status/200" /status/200
  expect_status "$port" 404 "${label}: GET /status/404" /status/404
  expect_status "$port" 500 "${label}: GET /status/500" /status/500

  section "${label} — Auth (basic + bearer)"

  # 401 challenge
  expect_status "$port" 401 "${label}: GET /basic-auth without creds" /basic-auth/user/pass
  expect_header 'WWW-Authenticate' 'Basic' "${label}: Basic challenge header"

  # 200 with valid creds
  expect_status "$port" 200 "${label}: GET /basic-auth with creds" \
    -u 'user:pass' /basic-auth/user/pass
  expect_json_field '.authenticated' 'true' "${label}: basic auth authenticated"
  expect_json_field '.user' 'user' "${label}: basic auth user echoed"

  # 200 with bearer
  expect_status "$port" 200 "${label}: GET /bearer with token" \
    -H 'Authorization: Bearer smoke-token' /bearer
  expect_json_field '.authenticated' 'true' "${label}: bearer authenticated"
  expect_json_field '.token' 'smoke-token' "${label}: bearer token echoed"

  # Digest: 401 + WWW-Authenticate Digest
  expect_status "$port" 401 "${label}: GET /digest-auth without creds" \
    /digest-auth/auth/tomcat/passwd/MD5
  expect_header 'WWW-Authenticate' 'Digest' "${label}: Digest challenge header"

  section "${label} — Cookies"

  expect_status "$port" 200 "${label}: GET /cookies" -c /tmp/.smoke.cookies /cookies
  # /cookies/set redirects (302) to /cookies by design
  expect_status "$port" 302 "${label}: GET /cookies/set?smoke=v" \
    -c /tmp/.smoke.cookies '/cookies/set?smoke=v'

  section "${label} — Redirects"

  expect_status "$port" 302 "${label}: GET /redirect/2" /redirect/2
  expect_header 'Location' '/redirect/' "${label}: /redirect Location header"

  section "${label} — Anything (request body echo)"

  expect_status "$port" 200 "${label}: POST /anything (form)" \
    -X POST --data-urlencode 'k1=v1' --data-urlencode 'k2=v2' /anything
  expect_json_field '.form.k1' 'v1' "${label}: form field echoed"

  section "${label} — Response formats"

  expect_status "$port" 200 "${label}: GET /uuid" /uuid
  expect_json_field '.uuid | length' '36' "${label}: uuid length 36"

  # base64: SFRUUEJJTiBpcyBhd2Vzb21l -> "HTTPBIN is awesome"
  expect_status "$port" 200 "${label}: GET /base64 (decoded)" \
    /base64/SFRUUEJJTiBpcyBhd2Vzb21l
  expect_body_contains 'HTTPBIN is awesome' "${label}: base64 decoded payload"

  # Compression: gzip
  expect_status "$port" 200 "${label}: GET /gzip" -H 'Accept: application/json' /gzip
  expect_header 'Content-Encoding' 'gzip' "${label}: /gzip Content-Encoding"
  # decode and confirm it's JSON
  if gunzip -c /tmp/.smoke.body 2>/dev/null | jq -e . > /dev/null 2>&1; then
    log_pass "${label}: /gzip body decodes to valid JSON"
  else
    log_fail "${label}: /gzip body did not decode to JSON"
  fi

  # Compression: brotli
  expect_status "$port" 200 "${label}: GET /brotli" -H 'Accept: application/json' /brotli
  expect_header 'Content-Encoding' 'br' "${label}: /brotli Content-Encoding"
  if brotli -d -c /tmp/.smoke.body 2>/dev/null | jq -e . > /dev/null 2>&1; then
    log_pass "${label}: /brotli body decodes to valid JSON"
  else
    log_fail "${label}: /brotli body did not decode to JSON"
  fi

  # Compression: deflate
  expect_status "$port" 200 "${label}: GET /deflate" -H 'Accept: application/json' /deflate
  expect_header 'Content-Encoding' 'deflate' "${label}: /deflate Content-Encoding"

  section "${label} — Cache / ETag / Range"

  # ETag: 1st request returns ETag header; 2nd with If-None-Match should be 304
  expect_status "$port" 200 "${label}: GET /etag/x first" /etag/x
  etag=$(grep -i '^ETag:' /tmp/.smoke.headers | awk '{print $2}' | tr -d '\r')
  if [ -n "$etag" ]; then
    expect_status "$port" 304 "${label}: GET /etag/x with If-None-Match" \
      -H "If-None-Match: ${etag}" /etag/x
  else
    log_fail "${label}: no ETag header returned"
  fi

  # Range: partial content
  expect_status "$port" 206 "${label}: GET /range/100 with Range" \
    -H 'Range: bytes=0-9' /range/100
  expect_header 'Content-Range' 'bytes 0-9' "${label}: Content-Range header"

  section "${label} — Dynamic data (delay + stream)"

  expect_status "$port" 200 "${label}: GET /delay/1" /delay/1
  expect_status "$port" 200 "${label}: GET /stream/2" /stream/2
}

echo "${YELLOW}smoke.sh starting at $(date -Is)${RESET}"
echo "servlet port:  ${SERVLET_PORT}"
echo "reactive port: ${REACTIVE_PORT}"

# Quick reachability probe so we fail fast with a clear message
for port in "$SERVLET_PORT" "$REACTIVE_PORT"; do
  if ! curl -fsS --max-time 3 "http://127.0.0.1:${port}/ip" > /dev/null 2>&1; then
    echo "${RED}ERROR:${RESET} http://127.0.0.1:${port}/ip is not reachable — is the app running?"
    exit 2
  fi
done

smoke_port "$SERVLET_PORT" "SERVLET"
smoke_port "$REACTIVE_PORT" "REACTIVE"

echo
echo "==============================================="
echo "  passed: ${GREEN}${PASS}${RESET}"
echo "  failed: ${RED}${FAIL}${RESET}"
if [ "$FAIL" -gt 0 ]; then
  echo
  echo "Failed checks:"
  for name in "${FAIL_NAMES[@]}"; do
    echo "  - $name"
  done
  exit 1
fi
echo "All smoke checks passed."
