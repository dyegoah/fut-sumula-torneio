package br.com.higitech.fut_sumula_torneio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.higitech.fut_sumula_torneio.model.Torneio;
import br.com.higitech.fut_sumula_torneio.model.Usuario;

public interface TorneioRepository extends JpaRepository<Torneio, Long> {
    // Busca apenas os torneios do dono
    List<Torneio> findAllByOrganizador(Usuario organizador);
    java.util.Optional<Torneio> findByCodigoPublico(String codigoPublico);
}