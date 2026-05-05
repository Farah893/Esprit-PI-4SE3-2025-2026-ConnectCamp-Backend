package tn.esprit.projetintegre.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class GeoLocationService {

    public String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    public String getCountryFromIp(String ip) {

        if (ip == null) return "FR";

        if (ip.startsWith("8.") || ip.startsWith("3.")) return "US";
        if (ip.startsWith("41.")) return "TN";
        if (ip.startsWith("2.") || ip.startsWith("5.")) return "FR";
        if (ip.startsWith("18.")) return "DE";
        if (ip.startsWith("80.")) return "ES";
        if (ip.startsWith("79.")) return "IT";
        if (ip.startsWith("51.")) return "GB";
        if (ip.startsWith("24.")) return "CA";
        if (ip.startsWith("36.")) return "CN";
        if (ip.startsWith("27.")) return "JP";
        if (ip.startsWith("94.")) return "AE";

        return "EU_OTHER";
    }
}
