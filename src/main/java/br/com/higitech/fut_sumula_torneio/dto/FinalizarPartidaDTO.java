package br.com.higitech.fut_sumula_torneio.dto;

import java.util.List;

public class FinalizarPartidaDTO {
    private Long partidaId;
    private Integer scoreHome;
    private Integer scoreAway;
    private String summaryHTML;
    private List<EventoDTO> eventos;

    // --- GETTERS E SETTERS MANUAIS (Crucial para receber os dados) ---

    public Long getPartidaId() { return partidaId; }
    public void setPartidaId(Long partidaId) { this.partidaId = partidaId; }

    public Integer getScoreHome() { return scoreHome; }
    public void setScoreHome(Integer scoreHome) { this.scoreHome = scoreHome; }

    public Integer getScoreAway() { return scoreAway; }
    public void setScoreAway(Integer scoreAway) { this.scoreAway = scoreAway; }

    public String getSummaryHTML() { return summaryHTML; }
    public void setSummaryHTML(String summaryHTML) { this.summaryHTML = summaryHTML; }

    public List<EventoDTO> getEventos() { return eventos; }
    public void setEventos(List<EventoDTO> eventos) { this.eventos = eventos; }

    // CLASSE INTERNA ESTÁTICA
    public static class EventoDTO {
        private String type; // GOAL, YELLOW, RED, FOUL
        private Long playerId;
        private Long teamId;
        private String time;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }
}