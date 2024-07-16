package forum.hub.forum.hub.api.dto;

import forum.hub.forum.hub.api.model.Topico;

import java.time.LocalDateTime;

public record DadosDetalhamentoTopico(
        Long Id,
        String titulo,
        String mensagem,
        LocalDateTime dataCriacao,
        String status,
        String nomeCurso,
        String nomeAutor) {

    public DadosDetalhamentoTopico(Topico topico){
        this(topico.getId(),
                topico.getTitulo(),
                topico.getMensagem(),
                topico.getDataCriacao(),
                topico.getStatus(),
                topico.getCurso().getNome(),
                topico.getAutor().getNome());
    }

}
