package com.dyrnq.httpbin.component;

import com.dyrnq.httpbin.api.entity.AuthInfo;
import com.dyrnq.httpbin.api.entity.Digest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.AuthService}.
 *
 * <p>Covers Basic / Hidden-Basic / Bearer and the full Digest flow.
 */
@Component
public class AuthService extends ReactiveBaseService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // Digest auth constants
    private static final String DIGEST_AUTH_DEFAULT_STALE_AFTER = "never";
    private static final List<String> DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS =
            Arrays.asList("1", "t", "true");
    private static final List<String> DIGEST_AUTH_ALGORITHM_LIST =
            Arrays.asList("md5", "sha-256", "sha-512");
    private static final List<String> DIGEST_AUTH_QOP_LIST =
            Arrays.asList("auth", "auth-int", "auth,auth-int");
    private static final Random rand = new Random(Calendar.getInstance().getTimeInMillis());
    private static final String DIGEST_AUTH_DEFAULT_ALGORITHM = "md5";

    // =========================================================================
    // Basic
    // =========================================================================

    public Mono<Void> basicAuth(ServerWebExchange exchange, String user, String passwd) {
        return drainBody(exchange)
                .then(Mono.defer(() -> {
                    String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
                    if (authorization == null) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders()
                                .set("WWW-Authenticate", "Basic realm=\"Realm\"");
                        return Mono.empty();
                    }
                    return authenticateBasic(exchange, authorization,
                            user, passwd, HttpStatus.UNAUTHORIZED);
                }));
    }

    public Mono<Void> hiddenBasicAuth(ServerWebExchange exchange, String user, String passwd) {
        return drainBody(exchange)
                .then(Mono.defer(() -> {
                    String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
                    if (authorization == null) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders()
                                .set("WWW-Authenticate", "Basic realm=\"Realm\"");
                        return Mono.empty();
                    }
                    return authenticateBasic(exchange, authorization,
                            user, passwd, HttpStatus.NOT_FOUND);
                }));
    }

    public Mono<Void> bearer(ServerWebExchange exchange, String authorization, String token) {
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            String headerToken = null;
            String header = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (header != null) {
                String[] tokenHolder = StringUtils.splitByWholeSeparator(header, " ");
                if (tokenHolder.length >= 2
                        && StringUtils.equalsIgnoreCase(tokenHolder[0], "Bearer")) {
                    headerToken = tokenHolder[1];
                }
            }
            if (headerToken == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return null;
            }
            JSONObject response = new JSONObject();
            response.put("authenticated", true);
            response.put("token", headerToken);
            return response;
        })).flatMap(response -> response == null ? Mono.empty() : rsOk(exchange, response));
    }

    private Mono<Void> authenticateBasic(ServerWebExchange exchange,
                                         String header,
                                         String user,
                                         String passwd,
                                         HttpStatus failureStatus) {
        return Mono.fromCallable(() -> {
            if (header == null || !header.startsWith("Basic ")) {
                exchange.getResponse().setStatusCode(failureStatus);
                return null;
            }
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(header.substring("Basic ".length()));
            } catch (IllegalArgumentException e) {
                exchange.getResponse().setStatusCode(failureStatus);
                return null;
            }
            String[] parts = new String(bytes, StandardCharsets.UTF_8).split(":", 2);
            String[] auth = new String[]{user, passwd};
            if (auth.length != 2 || !Arrays.equals(auth, parts)) {
                exchange.getResponse().setStatusCode(failureStatus);
                return null;
            }
            JSONObject response = new JSONObject();
            response.put("authenticated", true);
            response.put("user", parts[0]);
            return response;
        }).flatMap(response -> response == null ? Mono.empty() : rsOk(exchange, response));
    }

    // =========================================================================
    // Digest
    // =========================================================================

    /**
     * Reactive equivalent of servlet {@code digestAuth(...)}.
     *
     * <p>The flow:
     * <ol>
     *   <li>Read the body (reactive {@link #readBody(ServerWebExchange)});</li>
     *   <li>Parse {@code Authorization: Digest ...} into
     *       {@link AuthInfo} → {@link Digest};</li>
     *   <li>Validate the response hash against HA1/HA2 computed with
     *       {@code user} / {@code passwd};</li>
     *   <li>Track nonce via the {@code last_nonce} cookie to handle
     *       the {@code staleAfter} option.</li>
     * </ol>
     */
    public Mono<Void> digestAuth(ServerWebExchange exchange,
                                 String qop,
                                 String user,
                                 String passwd,
                                 String algorithm,
                                 String staleAfter) {
        return readBody(exchange).flatMap(body -> Mono.fromCallable(() -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            boolean requireCookieHandling = DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS.contains(
                    Optional.ofNullable(request.getQueryParams().getFirst("require-cookie")).orElse(""));

            final String effectiveQop = (qop == null || !DIGEST_AUTH_QOP_LIST.contains(qop))
                    ? "auth,auth-int" : qop;
            final String effectiveAlgorithm =
                    (algorithm == null || algorithm.isEmpty()
                            || !DIGEST_AUTH_ALGORITHM_LIST.contains(algorithm))
                            ? DIGEST_AUTH_DEFAULT_ALGORITHM : algorithm;
            final String effectiveStaleAfter =
                    (staleAfter == null || staleAfter.isEmpty())
                            ? DIGEST_AUTH_DEFAULT_STALE_AFTER : staleAfter;

            String authHeader = request.getHeaders().getFirst("Authorization");
            logger.debug("authHeader={}", authHeader);
            AuthInfo authInfo = AuthInfo.create(authHeader);
            Digest digest = Digest.create(authInfo);

            if (digest == null || (requireCookieHandling && !hasCookie(request, null))) {
                addCookie(response, "stale_after", effectiveStaleAfter);
                addCookie(response, "fake", "fake_value");
                digestUnauthorizedResponse(exchange, effectiveQop, effectiveAlgorithm, false);
                return null;
            }

            String fakeCookie = readCookie(request, "fake");
            if (fakeCookie == null || fakeCookie.isEmpty()) {
                fakeCookie = "fake_value";
            }
            if (requireCookieHandling && !"fake_value".equals(fakeCookie)) {
                JSONObject errBody = new JSONObject();
                errBody.put("errors", new JSONArray(Arrays.asList("missing cookie set on challenge")));
                return writeJsonThen(exchange, errBody, HttpStatus.FORBIDDEN);
            }

            String currentNonce = digest.getNonce();
            String staleAfterCookie = readCookie(request, "stale_after");
            String lastNonceValueCookie = readCookie(request, "last_nonce");

            if (currentNonce.equals(lastNonceValueCookie) || "0".equals(staleAfterCookie)) {
                addCookie(response, "stale_after", effectiveStaleAfter);
                addCookie(response, "fake", "fake_value");
                addCookie(response, "last_nonce", currentNonce);
                digestUnauthorizedResponse(exchange, effectiveQop, effectiveAlgorithm, true);
                return null;
            }

            boolean check = checkDigestAuth(exchange, digest, user, passwd,
                    effectiveAlgorithm, body);
            logger.debug("check={}", check);
            if (!check) {
                digestUnauthorizedResponse(exchange, effectiveQop, effectiveAlgorithm, false);
                return null;
            }

            if (staleAfterCookie != null && !staleAfterCookie.isEmpty()) {
                addCookie(response, "stale_after", nextStaleAfterValue(staleAfterCookie));
            }
            addCookie(response, "fake", "fake_value");
            addCookie(response, "last_nonce", currentNonce);

            JSONObject ok = new JSONObject();
            ok.put("authenticated", true);
            ok.put("user", user);
            return writeJsonThen(exchange, ok, HttpStatus.OK);
        })).flatMap(m -> m == null ? Mono.empty() : m);
    }

    /**
     * Helper used by {@link #digestAuth}: writes a JSON body with the
     * given status code and returns a {@code Mono<Void>} that completes
     * after writing.
     */
    private Mono<Void> writeJsonThen(ServerWebExchange exchange, JSONObject obj, HttpStatus code) {
        return rsJson(exchange, obj, code.value());
    }

    private String nextStaleAfterValue(String staleAfterValue) {
        try {
            return String.valueOf(Integer.parseInt(staleAfterValue) - 1);
        } catch (Throwable t) {
            return DIGEST_AUTH_DEFAULT_STALE_AFTER;
        }
    }

    private boolean checkDigestAuth(ServerWebExchange exchange,
                                    Digest digest,
                                    String user,
                                    String passwd,
                                    String algorithm,
                                    String body) {
        ServerHttpRequest request = exchange.getRequest();
        Map<String, String> requestInfo = new TreeMap<>();
        requestInfo.put("uri", request.getURI().getRawPath());
        requestInfo.put("body", body != null ? body : "");
        requestInfo.put("method", request.getMethod() != null
                ? request.getMethod().name() : "");

        String serverResponse = makeResponseHash(digest, user, passwd, algorithm, requestInfo);
        logger.debug("serverResponse={}, digest.getResponse()={}, uri={}, algorithm={}",
                serverResponse, digest.getResponse(), requestInfo.get("uri"), algorithm);
        return StringUtils.equalsIgnoreCase(serverResponse, digest.getResponse());
    }

    private String makeResponseHash(Digest digest,
                                    String user,
                                    String passwd,
                                    String algorithm,
                                    Map<String, String> requestInfo) {
        String hash;
        String ha1 = HA1(digest.getRealm(), user, passwd, algorithm);
        String ha2 = HA2(digest, requestInfo, algorithm);

        String qop = digest.getQop();
        if (qop == null || qop.isEmpty()) {
            hash = H(joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    ha1.getBytes(StandardCharsets.UTF_8),
                    digest.getNonce().getBytes(StandardCharsets.UTF_8),
                    ha2.getBytes(StandardCharsets.UTF_8)), algorithm);
        } else if ("auth".equalsIgnoreCase(qop) || "auth-int".equalsIgnoreCase(qop)) {
            String nonce = digest.getNonce();
            String nc = digest.getNc();
            String cnonce = digest.getCnonce();
            if (nonce == null || nonce.isEmpty()
                    || nc == null || nc.isEmpty()
                    || cnonce == null || cnonce.isEmpty()) {
                throw new RuntimeException("'nonce, nc, cnonce' required for response H");
            }
            hash = H(joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    ha1.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    nc.getBytes(StandardCharsets.UTF_8),
                    cnonce.getBytes(StandardCharsets.UTF_8),
                    qop.getBytes(StandardCharsets.UTF_8),
                    ha2.getBytes(StandardCharsets.UTF_8)), algorithm);
        } else {
            throw new RuntimeException("qop value are wrong");
        }
        return hash;
    }

    private String HA1(String realm, String username, String passwd, String algorithm) {
        if (realm == null || realm.isEmpty()) {
            realm = "";
        }
        return H(joinByteArrays(
                ":".getBytes(StandardCharsets.UTF_8),
                username.getBytes(StandardCharsets.UTF_8),
                realm.getBytes(StandardCharsets.UTF_8),
                passwd.getBytes(StandardCharsets.UTF_8)), algorithm);
    }

    private String HA2(Digest digest, Map<String, String> requestInfo, String algorithm) {
        if (digest.getQop() == null || digest.getQop().isEmpty()
                || "auth".equalsIgnoreCase(digest.getQop())) {
            return H(joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    requestInfo.get("method").getBytes(StandardCharsets.UTF_8),
                    requestInfo.get("uri").getBytes(StandardCharsets.UTF_8)), algorithm);
        } else if ("auth-int".equalsIgnoreCase(digest.getQop())) {
            return H(joinByteArrays(
                            ":".getBytes(StandardCharsets.UTF_8),
                            requestInfo.get("method").getBytes(StandardCharsets.UTF_8),
                            requestInfo.get("uri").getBytes(StandardCharsets.UTF_8),
                            H(requestInfo.get("body").getBytes(StandardCharsets.UTF_8),
                                    algorithm).getBytes(StandardCharsets.UTF_8)),
                    algorithm);
        }
        return "";
    }

    private String H(byte[] inputs, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm.toUpperCase());
            md.update(inputs);
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String hash(String data, String algorithm) {
        switch (algorithm) {
            case "sha-256":
                return sha256Hex(data);
            case "sha-512":
                return sha512Hex(data);
            default:
                return md5Hex(data);
        }
    }

    private String md5Hex(String data) {
        return H(data.getBytes(StandardCharsets.UTF_8), "md5");
    }

    private String sha256Hex(String data) {
        return H(data.getBytes(StandardCharsets.UTF_8), "sha-256");
    }

    private String sha512Hex(String data) {
        return H(data.getBytes(StandardCharsets.UTF_8), "sha-512");
    }

    private void digestUnauthorizedResponse(ServerWebExchange exchange,
                                            String qop,
                                            String algorithm,
                                            boolean stale) {
        String nonce = hash(String.join(":", remoteAddress(exchange),
                String.valueOf(Instant.now().toEpochMilli()),
                String.valueOf(rand.nextInt())), algorithm);
        String opaque = hash(String.valueOf(rand.nextInt()), algorithm);
        String value = String.join(",",
                String.format("realm=\"%s\"", "me@kennethreitz.com"),
                String.format("nonce=\"%s\"", nonce),
                String.format("opaque=\"%s\"", opaque),
                String.format("qop=%s", qop),
                String.format("algorithm=\"%s\"", algorithm),
                String.format("stale=%s", stale));
        exchange.getResponse().getHeaders().set("WWW-Authenticate", "Digest " + value);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    private String remoteAddress(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        var remote = req.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress() : "0.0.0.0";
    }

    // =========================================================================
    // Cookie helpers (reactive replacements for JakartaServletUtil.addCookie)
    // =========================================================================

    /** Reactive Set-Cookie writer. */
    private void addCookie(ServerHttpResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value).path("/").build();
        response.addCookie(cookie);
    }

    /** Read a single request cookie value, or {@code null}. */
    private String readCookie(ServerHttpRequest request, String name) {
        var cookies = request.getCookies();
        if (cookies == null || name == null) {
            return null;
        }
        var cookie = cookies.getFirst(name);
        return cookie != null ? cookie.getValue() : null;
    }

    /** Are there any cookies at all? (replaces {@code servletRequest.getHeader("Cookie") != null}). */
    private boolean hasCookie(ServerHttpRequest request, String ignore) {
        var cookies = request.getCookies();
        return cookies != null && !cookies.isEmpty();
    }

    // =========================================================================
    // joinByteArrays (port of servlet Helpers.joinByteArrays)
    // =========================================================================

    private static byte[] joinByteArrays(byte[] delimiter, byte[]... byteArrays) {
        int totalLength = delimiter.length * (byteArrays.length - 1);
        for (byte[] array : byteArrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (int i = 0; i < byteArrays.length; i++) {
            if (i > 0) {
                System.arraycopy(delimiter, 0, result, offset, delimiter.length);
                offset += delimiter.length;
            }
            byte[] array = byteArrays[i];
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
