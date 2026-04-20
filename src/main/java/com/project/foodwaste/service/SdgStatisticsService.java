package com.project.foodwaste.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class SdgStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(SdgStatisticsService.class);
    private final DataSource dataSource;

    @Autowired
    public SdgStatisticsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> getSdgMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS total FROM donations");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) metrics.put("totalDonations", rs.getLong("total"));
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(quantity),0) AS meals FROM donations WHERE status=?")) {
                ps.setString(1, "PICKED_UP");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long kg = rs.getLong("meals");
                        metrics.put("mealsSaved", kg * 2);
                        metrics.put("foodKgSaved", kg);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT ngo_id) AS ngos FROM requests WHERE status=?")) {
                ps.setString(1, "ACCEPTED");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) metrics.put("ngosServed", rs.getLong("ngos"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT donor_id) AS donors FROM donations");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) metrics.put("activeDonors", rs.getLong("donors"));
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS pending FROM requests WHERE status=?")) {
                ps.setString(1, "PENDING");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) metrics.put("pendingRequests", rs.getLong("pending"));
                }
            }
            StringBuilder mL = new StringBuilder(), mC = new StringBuilder(), mQ = new StringBuilder();
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT DATE_FORMAT(created_at,'%Y-%m') AS month,COUNT(*) AS count,COALESCE(SUM(quantity),0) AS qty FROM donations GROUP BY DATE_FORMAT(created_at,'%Y-%m') ORDER BY month DESC LIMIT 6");
                 ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) { mL.append(","); mC.append(","); mQ.append(","); }
                    mL.append(rs.getString("month"));
                    mC.append(rs.getLong("count"));
                    mQ.append(rs.getLong("qty"));
                    first = false;
                }
            }
            metrics.put("monthLabels", mL.toString());
            metrics.put("monthCounts", mC.toString());
            metrics.put("monthQuantities", mQ.toString());
        } catch (SQLException e) {
            logger.error("Error fetching SDG metrics via JDBC", e);
            metrics.put("error", "Unable to fetch metrics");
        }
        return metrics;
    }
}
