package br.com.higitech.fut_sumula_torneio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.higitech.fut_sumula_torneio.model.Jogador;
import br.com.higitech.fut_sumula_torneio.model.Usuario;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Long> {

    // Busca todos os jogadores APENAS do organizador logado
    List<Jogador> findAllByOrganizador(Usuario organizador);

    // Verifica duplicidade dentro do time do organizador
    boolean existsByNomeAndNumeroCamisaAndOrganizador(String nome, String numeroCamisa, Usuario organizador);
    
    // Conta quantos jogadores esse organizador tem (para o limite de 330)
    long countByOrganizador(Usuario organizador);
}