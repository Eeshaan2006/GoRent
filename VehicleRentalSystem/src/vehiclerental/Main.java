package vehiclerental;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** GoRent - starts a small web server and opens the browser. */
public class Main {
    private static final int PORT = 8080;
    private static final RentalService service = new RentalService();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/vehicles", Main::handleVehicles);
        server.createContext("/api/quote", Main::handleQuote);
        server.createContext("/api/rent", Main::handleRent);
        server.createContext("/api/return", Main::handleReturn);
        server.createContext("/api/rentals", Main::handleRentals);
        server.createContext("/", Main::handleStatic);
        server.start();

        String url = "http://localhost:" + PORT;
        System.out.println("GoRent is running at " + url + "  (press the red stop button in Eclipse to quit)");
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            System.out.println("Open " + url + " in your browser.");
        }
    }

    // ---------------------------------------------------------------- API

    private static void handleVehicles(HttpExchange ex) throws IOException {
        try {
            if ("POST".equals(ex.getRequestMethod())) {
                Map<String, String> p = parse(body(ex));
                double rate = parseDouble(p.get("rate"));
                Vehicle v = service.addVehicle(p.get("reg"), p.get("model"), p.get("type"), rate);
                json(ex, 200, vehicleJson(v));
            } else {
                StringBuilder sb = new StringBuilder("[");
                List<Vehicle> list = service.getVehicles();
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(',');
                    sb.append(vehicleJson(list.get(i)));
                }
                json(ex, 200, sb.append(']').toString());
            }
        } catch (Exception e) {
            error(ex, e);
        }
    }

    private static void handleQuote(HttpExchange ex) throws IOException {
        try {
            Map<String, String> p = parse(ex.getRequestURI().getRawQuery());
            Vehicle v = service.getVehicle(p.get("reg"));
            int days = (int) parseDouble(p.get("days"));
            if (days < 1 || days > 90) throw new IllegalArgumentException("Days must be between 1 and 90.");
            double base = v.getRatePerDay() * days;
            double total = v.calculateRent(days);
            json(ex, 200, "{\"days\":" + days + ",\"base\":" + num(base) + ",\"total\":" + num(total)
                    + ",\"note\":" + str(v.getChargeNote()) + "}");
        } catch (Exception e) {
            error(ex, e);
        }
    }

    private static void handleRent(HttpExchange ex) throws IOException {
        try {
            Map<String, String> p = parse(body(ex));
            Rental r = service.rent(p.get("reg"), p.get("customer"), p.get("phone"), (int) parseDouble(p.get("days")));
            json(ex, 200, rentalJson(r));
        } catch (Exception e) {
            error(ex, e);
        }
    }

    private static void handleReturn(HttpExchange ex) throws IOException {
        try {
            Map<String, String> p = parse(body(ex));
            Rental r = service.returnVehicle(p.get("reg"));
            json(ex, 200, rentalJson(r));
        } catch (Exception e) {
            error(ex, e);
        }
    }

    private static void handleRentals(HttpExchange ex) throws IOException {
        try {
            StringBuilder sb = new StringBuilder("[");
            List<Rental> list = service.getRentals();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(rentalJson(list.get(i)));
            }
            json(ex, 200, sb.append(']').toString());
        } catch (Exception e) {
            error(ex, e);
        }
    }

    // ------------------------------------------------------------ static files

    private static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        if (path.contains("..")) { send(ex, 403, "text/plain", "Forbidden".getBytes(StandardCharsets.UTF_8)); return; }
        try (InputStream in = Main.class.getResourceAsStream("/web" + path)) {
            if (in == null) {
                send(ex, 404, "text/plain", "Not found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            send(ex, 200, contentType(path), in.readAllBytes());
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    // ---------------------------------------------------------------- helpers

    private static String vehicleJson(Vehicle v) {
        return "{\"reg\":" + str(v.getRegNo()) + ",\"model\":" + str(v.getModel())
                + ",\"type\":" + str(v.getType()) + ",\"rate\":" + num(v.getRatePerDay())
                + ",\"available\":" + v.isAvailable() + ",\"note\":" + str(v.getChargeNote()) + "}";
    }

    private static String rentalJson(Rental r) {
        Vehicle v = r.getVehicle();
        return "{\"id\":" + r.getId() + ",\"reg\":" + str(v.getRegNo()) + ",\"model\":" + str(v.getModel())
                + ",\"type\":" + str(v.getType()) + ",\"rate\":" + num(v.getRatePerDay())
                + ",\"note\":" + str(v.getChargeNote()) + ",\"customer\":" + str(r.getCustomer())
                + ",\"phone\":" + str(r.getPhone()) + ",\"days\":" + r.getDays()
                + ",\"base\":" + num(r.getBase()) + ",\"amount\":" + num(r.getAmount())
                + ",\"rentedAt\":" + str(fmt(r.getRentedAt()))
                + ",\"returnedAt\":" + (r.getReturnedAt() == null ? "null" : str(fmt(r.getReturnedAt())))
                + ",\"active\":" + r.isActive() + "}";
    }

    private static String fmt(LocalDateTime t) { return t.format(FMT); }

    private static String num(double d) { return String.format(Locale.ROOT, "%.2f", d); }

    private static String str(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                case '\t': sb.append(' '); break;
                default: sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s == null ? "" : s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }
    }

    private static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parse(String s) {
        Map<String, String> m = new HashMap<>();
        if (s == null || s.isEmpty()) return m;
        for (String part : s.split("&")) {
            int i = part.indexOf('=');
            String k = i < 0 ? part : part.substring(0, i);
            String v = i < 0 ? "" : part.substring(i + 1);
            m.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return m;
    }

    private static void json(HttpExchange ex, int code, String body) throws IOException {
        send(ex, code, "application/json; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    private static void error(HttpExchange ex, Exception e) throws IOException {
        int code = (e instanceof IllegalStateException) ? 409 : 400;
        String msg = e.getMessage() == null ? "Something went wrong." : e.getMessage();
        json(ex, code, "{\"error\":" + str(msg) + "}");
    }

    private static void send(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}
