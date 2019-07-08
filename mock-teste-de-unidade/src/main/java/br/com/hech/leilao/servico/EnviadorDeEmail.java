package br.com.hech.leilao.servico;

import br.com.hech.leilao.dominio.Leilao;

public interface EnviadorDeEmail {
	void envia(Leilao leilao);
}
