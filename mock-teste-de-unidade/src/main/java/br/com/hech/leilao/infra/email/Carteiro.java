package br.com.hech.leilao.infra.email;

import br.com.hech.leilao.dominio.Leilao;

public interface Carteiro {
	void envia(Leilao leilao);
}
