package br.com.hech.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.hech.leilao.builder.CriadorDeLeilao;
import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {
	
	private RepositorioDeLeiloes daoFalso;
	private EnviadorDeEmail carteiroFalso;
	private Calendar dataTeste;
	
	@Before
	public void setUp() {
		this.daoFalso = mock(RepositorioDeLeiloes.class);
		this.carteiroFalso = mock(EnviadorDeEmail.class);
		this.dataTeste = Calendar.getInstance();
	}

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		dataTeste.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de led").naData(dataTeste).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataTeste).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		when(daoFalso.correntes()).thenReturn(leiloesAntigos);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramOntem() {
		dataTeste.add(Calendar.DAY_OF_MONTH, -1);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de led").naData(dataTeste).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataTeste).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());

		// garantindo que o método não foi invocado nenhuma vez com o
		// método auxiliar never() no verify do mockito
		verify(daoFalso, never()).atualiza(leilao1);
		verify(daoFalso, never()).atualiza(leilao2);
		// Ainda podemos passar atLeastOnce(),
		// atLeast(numero) e atMost(numero) para o verify
	}

	@Test
	public void naoDeveEncerrarLeilaoCasoNaoHajaNenhum() {
		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {
		dataTeste.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de led").naData(dataTeste).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// verify do mockito garante que um método foi invocado
		verify(daoFalso, times(1)).atualiza(leilao1);
	}

}
