package br.com.hech.leilao.infra.dao;

import br.com.hech.leilao.dominio.Pagamento;

public interface RepositorioDePagamentos {
	void salva(Pagamento pagamento);
}
