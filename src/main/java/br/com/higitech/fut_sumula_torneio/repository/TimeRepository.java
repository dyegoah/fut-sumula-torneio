package br.com.higitech.fut_sumula_torneio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.higitech.fut_sumula_torneio.model.Time;
import br.com.higitech.fut_sumula_torneio.model.Usuario;

public interface TimeRepository extends JpaRepository<Time, Long> {
    
    // Lista apenas do dono
    List<Time> findAllByOrganizador(Usuario organizador);
    
    // Verifica nome duplicado apenas na conta do dono
    boolean existsByNomeIgnoreCaseAndOrganizador(String nome, Usuario organizador);
    
    // Método antigo (pode manter ou remover se não usar mais)
    boolean existsByNomeIgnoreCase(String nome);
}