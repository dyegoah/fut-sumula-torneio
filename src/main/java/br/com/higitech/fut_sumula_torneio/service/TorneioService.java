package br.com.higitech.fut_sumula_torneio.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.higitech.fut_sumula_torneio.model.Partida;
import br.com.higitech.fut_sumula_torneio.model.Time;
import br.com.higitech.fut_sumula_torneio.model.Torneio;
import br.com.higitech.fut_sumula_torneio.repository.PartidaRepository;

@Service
public class TorneioService {

    @Autowired private PartidaRepository partidaRepo;

    @Transactional
    public List<Partida> gerarTabela(Torneio torneio) {
        // Se já existem partidas, retorna elas (não recria)
        List<Partida> existentes = partidaRepo.findByTorneioIdOrderByIdAsc(torneio.getId());
        if (!existentes.isEmpty()) return existentes;

        List<Partida> novas = new ArrayList<>();
        String tipo = torneio.getTipo() != null ? torneio.getTipo().toUpperCase() : "MATA_MATA";

        // LÓGICA ESPECIAL: COPA DE 6 TIMES
        if ("COPA".equals(tipo) && torneio.getQuantidadeTimes() == 6) {
            novas = gerarCopa6Times(torneio);
        } else if ("MATA_MATA".equals(tipo)) {
            novas = gerarMataMata(torneio);
        } else {
            novas = gerarLiga(torneio); // Serve para LIGA e COPA padrão
        }

        return partidaRepo.saveAll(novas);
    }

    private List<Partida> gerarCopa6Times(Torneio t) {
        List<Partida> partidas = new ArrayList<>();
        List<Time> times = t.getTimes();
        if(times.size() < 6) return partidas; // Segurança

        // --- FASE DE GRUPOS ---
        // Grupo A: Times 0, 1, 2
        createMatch(t, times.get(0), times.get(1), "GRUPO A", 1, partidas);
        createMatch(t, times.get(0), times.get(2), "GRUPO A", 1, partidas);
        createMatch(t, times.get(1), times.get(2), "GRUPO A", 1, partidas);

        // Grupo B: Times 3, 4, 5
        createMatch(t, times.get(3), times.get(4), "GRUPO B", 1, partidas);
        createMatch(t, times.get(3), times.get(5), "GRUPO B", 1, partidas);
        createMatch(t, times.get(4), times.get(5), "GRUPO B", 1, partidas);

        // --- MATA-MATA (Placeholders) ---
        // Semifinal 1: 1º A x 2º B
        Partida semi1 = new Partida(); 
        semi1.setTorneio(t); semi1.setFase("SEMIFINAL"); semi1.setRodada(2);
        // Nota: O vínculo automático de Grupos -> Mata-Mata requer lógica de pontos complexa
        // Aqui deixamos sem origem direta (será definido manualmente ou por update futuro)
        partidas.add(semi1);

        // Semifinal 2: 1º B x 2º A
        Partida semi2 = new Partida(); 
        semi2.setTorneio(t); semi2.setFase("SEMIFINAL"); semi2.setRodada(2);
        partidas.add(semi2);

        // Final: Venc. Semi 1 x Venc. Semi 2 (Este vínculo funciona automático!)
        Partida finalMatch = new Partida(); 
        finalMatch.setTorneio(t); finalMatch.setFase("FINAL"); finalMatch.setRodada(3);
        
        // Aponta para os índices das semis na lista 'partidas'
        // Índices: 0-5 (Grupos), 6 (Semi1), 7 (Semi2) -> Final aponta para 6 e 7
        finalMatch.setTempIndexOrigemCasa(6);
        finalMatch.setTempIndexOrigemVisitante(7);
        partidas.add(finalMatch);

        return partidas;
    }

    private void createMatch(Torneio t, Time t1, Time t2, String grupo, int rodada, List<Partida> list) {
        Partida p = new Partida();
        p.setTorneio(t); p.setTimeCasa(t1); p.setTimeVisitante(t2);
        p.setGrupo(grupo); p.setFase("FASE DE GRUPOS"); p.setRodada(rodada);
        list.add(p);
    }

    private List<Partida> gerarMataMata(Torneio t) {
        List<Partida> partidas = new ArrayList<>();
        List<Time> times = t.getTimes();
        int qtdJogosInicial = times.size() / 2;
        
        // Rodada 1
        List<Partida> rodadaAtual = new ArrayList<>();
        for (int i = 0; i < qtdJogosInicial; i++) {
            Partida p = new Partida();
            p.setTorneio(t); p.setRodada(1); p.setFase("RODADA 1");
            if (i*2 < times.size()) p.setTimeCasa(times.get(i*2));
            if (i*2+1 < times.size()) p.setTimeVisitante(times.get(i*2+1));
            partidas.add(p);
            rodadaAtual.add(p);
        }

        // Rodadas Seguintes
        int rodadaNum = 2;
        while (rodadaAtual.size() > 1) {
            List<Partida> proximaRodada = new ArrayList<>();
            for (int i = 0; i < rodadaAtual.size(); i += 2) {
                if (i+1 >= rodadaAtual.size()) break;
                Partida p = new Partida();
                p.setTorneio(t); p.setRodada(rodadaNum);
                p.setFase(rodadaAtual.size() == 2 ? "FINAL" : "RODADA " + rodadaNum);
                p.setTempIndexOrigemCasa(partidas.indexOf(rodadaAtual.get(i)));
                p.setTempIndexOrigemVisitante(partidas.indexOf(rodadaAtual.get(i+1)));
                partidas.add(p);
                proximaRodada.add(p);
            }
            rodadaAtual = proximaRodada;
            rodadaNum++;
        }
        return partidas;
    }

    private List<Partida> gerarLiga(Torneio t) {
        List<Partida> partidas = new ArrayList<>();
        List<Time> times = new ArrayList<>(t.getTimes());
        if(times.size() % 2 != 0) times.add(null);
        int numRodadas = times.size() - 1;
        int metade = times.size() / 2;

        for (int r = 0; r < numRodadas; r++) {
            for (int i = 0; i < metade; i++) {
                Time t1 = times.get(i);
                Time t2 = times.get(times.size()-1-i);
                if(t1!=null && t2!=null) {
                    Partida p = new Partida();
                    p.setTorneio(t); p.setRodada(r+1); p.setFase("RODADA " + (r+1));
                    p.setTimeCasa(t1); p.setTimeVisitante(t2);
                    partidas.add(p);
                }
            }
            Collections.rotate(times.subList(1, times.size()), 1);
        }
        return partidas;
    }
}