package br.com.hech.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import br.com.hech.leilao.builder.CriadorDeLeilao;
import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.infra.dao.LeilaoDaoFalso;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de led")
				.naData(antiga)
				.constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
				.naData(antiga)
				.constroi();
		
		LeilaoDaoFalso dao = new LeilaoDaoFalso();
		dao.salva(leilao1);
		dao.salva(leilao2);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao();
		encerrador.encerra();
		
		List<Leilao> encerrados = dao.encerrados();
		
		assertEquals(2, encerrados.size());
		assertTrue(encerrados.get(0).isEncerrado());
		assertTrue(encerrados.get(1).isEncerrado());
	}
}
