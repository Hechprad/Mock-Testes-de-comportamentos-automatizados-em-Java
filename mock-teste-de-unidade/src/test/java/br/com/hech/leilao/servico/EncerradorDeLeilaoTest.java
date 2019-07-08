package br.com.hech.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.hech.leilao.builder.CriadorDeLeilao;
import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.hech.leilao.infra.email.Carteiro;

public class EncerradorDeLeilaoTest {
	
	private Calendar dataTeste;
	private RepositorioDeLeiloes daoFalso;
	private Carteiro carteiroFalso;
	private EncerradorDeLeilao encerrador;
	
	@Before
	public void setUp() {
		this.dataTeste = Calendar.getInstance();
		this.daoFalso = mock(RepositorioDeLeiloes.class);
		this.carteiroFalso = mock(Carteiro.class);
		this.encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
	}

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		dataTeste.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de led").naData(dataTeste).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataTeste).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		when(daoFalso.correntes()).thenReturn(leiloesAntigos);

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

		encerrador.encerra();

		// verify do mockito garante que um método foi invocado
		verify(daoFalso, times(1)).atualiza(leilao1);
	}

	@Test
    public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
		dataTeste.set(1999, 1, 20);
	
		Leilao leilao1 = new CriadorDeLeilao().para("Game boy roxo").naData(dataTeste).constroi();
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		encerrador.encerra();
		
		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
		inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoDaoFalha() {
		dataTeste.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("PC desktop").naData(dataTeste).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Teclado gamer").naData(dataTeste).constroi();
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		// simulando problema no BD com o primeiro leilão no método atualiza()
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
		encerrador.encerra();
		
		verify(carteiroFalso, never()).envia(leilao1);
		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoCarteiroFalha() {
		dataTeste.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("Monitor").naData(dataTeste).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Mesa").naData(dataTeste).constroi();
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		doThrow(new RuntimeException()).when(carteiroFalso).envia(leilao1);
		
		encerrador.encerra();

		verify(daoFalso, times(1)).atualiza(leilao2);
		verify(carteiroFalso, times(1)).envia(leilao2);
	}
	
	@Test
	public void deveDesistiSeDaoFalhaSempre() {
		dataTeste.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("Monitor").naData(dataTeste).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Mesa").naData(dataTeste).constroi();
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao2);
		
		encerrador.encerra();

		verify(carteiroFalso, never()).envia(leilao1);
		verify(carteiroFalso, never()).envia(leilao2);
	}
}
