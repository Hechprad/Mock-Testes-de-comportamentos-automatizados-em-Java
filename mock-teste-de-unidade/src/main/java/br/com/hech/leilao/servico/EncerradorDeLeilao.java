package br.com.hech.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.hech.leilao.dominio.Leilao;
import br.com.hech.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.hech.leilao.infra.email.Carteiro;

public class EncerradorDeLeilao {

	private int total = 0;
//	private final LeilaoDao dao;
	private final RepositorioDeLeiloes dao;
	private final Carteiro carteiro;

	public EncerradorDeLeilao(RepositorioDeLeiloes dao, Carteiro carteiro) {
		this.dao = dao;
		this.carteiro = carteiro;
	}

//	public EncerradorDeLeilao(LeilaoDao dao) {
//		this.dao = dao;
//	}

	public void encerra() {
		List<Leilao> todosLeiloesCorrentes = dao.correntes();

//		for (Leilao leilao : todosLeiloesCorrentes) {
//			if (comecouSemanaPassada(leilao)) {
//				leilao.encerra();
//				total++;
//				dao.atualiza(leilao);
//			}
//		}

		// lambda
		todosLeiloesCorrentes.forEach(leilao -> {
			try {
				if (comecouSemanaPassada(leilao)) {
					leilao.encerra();
					total++;
					dao.atualiza(leilao);
					carteiro.envia(leilao);
				}
			} catch (Exception e) {
				// pega a exceção e o loop continua...
			}
		});

	}

	private boolean comecouSemanaPassada(Leilao leilao) {
		return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}

		return diasNoIntervalo;
	}

	public int getTotalEncerrados() {
		return total;
	}
}
