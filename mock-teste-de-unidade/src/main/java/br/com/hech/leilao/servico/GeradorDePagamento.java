package br.com.hech.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.dominio.Pagamento;
import br.com.hech.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.hech.leilao.infra.dao.RepositorioDePagamentos;

public class GeradorDePagamento {

	private RepositorioDeLeiloes leiloes;
	private Avaliador avaliador;
	private RepositorioDePagamentos pagamentos;

	public GeradorDePagamento(RepositorioDeLeiloes leiloes, 
			RepositorioDePagamentos pagamentos, Avaliador avaliador) {
		this.leiloes = leiloes;
		this.pagamentos = pagamentos;
		this.avaliador = avaliador;
	}
	
	public void gera() {
		List<Leilao> leiloesEncerrados = this.leiloes.encerrados();
		
		leiloesEncerrados.forEach(leilao -> {
			this.avaliador.avalia(leilao);
			
			Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), Calendar.getInstance());
			this.pagamentos.salva(novoPagamento);
		});
	}
}
