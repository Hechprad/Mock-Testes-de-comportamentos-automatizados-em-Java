package br.com.hech.leilao.servico;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Calendar;

import br.com.hech.leilao.builder.CriadorDeLeilao;
import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.dominio.Pagamento;
import br.com.hech.leilao.dominio.Usuario;
import br.com.hech.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.hech.leilao.infra.dao.RepositorioDePagamentos;
import br.com.hech.leilao.infra.relogio.Relogio;

public class GeradorDePagamentoTest {

	@Test
	public void deveGerarPagamentoParaUmLeilaoEncerrado() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		
		Leilao leilao = new CriadorDeLeilao()
				.para("Teclado mecânico")
				.lance(new Usuario("José da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0)
				.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador());
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
	}
	
	@Test
	public void deveEmpurrarParaOProximoDiaUtil() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);
		
		Leilao leilao = new CriadorDeLeilao()
				.para("Teclado mecânico")
				.lance(new Usuario("José da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0)
				.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		Calendar sabado = Calendar.getInstance();
		sabado.set(2012, Calendar.APRIL, 7);
		
		when(relogio.hoje()).thenReturn(sabado);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}
	
	@Test
	public void deveEmpurrarPagamentoNoDomingoParaOProximoDiaUtil() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);
		
		Leilao leilao = new CriadorDeLeilao()
				.para("Teclado mecânico")
				.lance(new Usuario("José da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0)
				.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		Calendar domingo = Calendar.getInstance();
		domingo.set(2012, Calendar.APRIL, 8);
		
		when(relogio.hoje()).thenReturn(domingo);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}
}
