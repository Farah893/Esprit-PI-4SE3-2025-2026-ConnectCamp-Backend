package tn.esprit.projetintegre.enums;

public enum QualityBadge {
    EXCELLENT(80, "Excellent", "gold"),
    GOOD(60, "Bon", "silver"),
    AVERAGE(40, "Moyen", "bronze"),
    NEEDS_IMPROVEMENT(0, "À améliorer", "gray");

    private final int minScore;
    private final String label;
    private final String color;

    QualityBadge(int minScore, String label, String color) {
        this.minScore = minScore;
        this.label = label;
        this.color = color;
    }

    public int getMinScore() {
        return minScore;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public static QualityBadge fromScore(int score) {
        for (QualityBadge badge : values()) {
            if (score >= badge.minScore) return badge;
        }
        return NEEDS_IMPROVEMENT;
    }
}
