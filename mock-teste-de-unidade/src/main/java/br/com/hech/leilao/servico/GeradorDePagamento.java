package br.com.hech.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.dominio.Pagamento;
import br.com.hech.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.hech.leilao.infra.dao.RepositorioDePagamentos;
import br.com.hech.leilao.infra.relogio.Relogio;
import br.com.hech.leilao.infra.relogio.RelogioDoSistema;

public class GeradorDePagamento {

	private RepositorioDeLeiloes leiloes;
	private Avaliador avaliador;
	private RepositorioDePagamentos pagamentos;
	private Relogio relogio;

	public GeradorDePagamento(RepositorioDeLeiloes leiloes, 
			RepositorioDePagamentos pagamentos, 
			Avaliador avaliador,
			Relogio relogio) {
		this.leiloes = leiloes;
		this.pagamentos = pagamentos;
		this.avaliador = avaliador;
		this.relogio = relogio;
	}
	
	public GeradorDePagamento(RepositorioDeLeiloes leiloes, 
			RepositorioDePagamentos pagamentos, 
			Avaliador avaliador) {
		this(leiloes, pagamentos, avaliador, new RelogioDoSistema());
	}
	
	public void gera() {
		List<Leilao> leiloesEncerrados = this.leiloes.encerrados();
		
		leiloesEncerrados.forEach(leilao -> {
			this.avaliador.avalia(leilao);
			
			Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), primeiroDiaUtil());
			this.pagamentos.salva(novoPagamento);
		});
	}

	private Calendar primeiroDiaUtil() {
		Calendar data = relogio.hoje();
		int diaDaSemana = data.get(Calendar.DAY_OF_WEEK);
		if(diaDaSemana == Calendar.SATURDAY) data.add(Calendar.DAY_OF_MONTH, 2);
		else if(diaDaSemana == Calendar.SUNDAY) data.add(Calendar.DAY_OF_MONTH, 1);
		return data;
	}
}
