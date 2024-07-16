package forum.hub.forum.hub.api.service;

import forum.hub.forum.hub.api.dto.DadosCadastroTopico;
import forum.hub.forum.hub.api.dto.DadosDetalhamentoTopico;
import forum.hub.forum.hub.api.exception.UsuarioSemPerimssaoException;
import forum.hub.forum.hub.api.exception.ValidacaoException;
import forum.hub.forum.hub.api.model.Topico;
import forum.hub.forum.hub.api.model.Usuario;
import forum.hub.forum.hub.api.repository.CursoRepository;
import forum.hub.forum.hub.api.repository.TopicoRepository;
import forum.hub.forum.hub.api.repository.UsuarioRepository;
import forum.hub.forum.hub.api.security.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicoService {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Transactional
    public DadosDetalhamentoTopico salvar(DadosCadastroTopico dados, String token){
        validarDados(dados);

        var autor = pegarAutorPeloToken(token);
        var curso = cursoRepository.getReferenceById(dados.curso_id());
        var topico = new Topico(dados.titulo(), dados.mensagem(), curso, autor);

        topico = topicoRepository.save(topico);

        var dadosDetalhementoTopico = new DadosDetalhamentoTopico(topico);

        return dadosDetalhementoTopico;
    }

    public DadosDetalhamentoTopico buscar(Long id){
        return new DadosDetalhamentoTopico(topicoRepository.getReferenceById(id));
    }

    public List<DadosDetalhamentoTopico> listar(){
        List<Topico>topicos = topicoRepository.findAllByOrderByDataCriacaoDesc();
        return topicos.stream().map(t -> new DadosDetalhamentoTopico(t)).toList();
    }

    @Transactional
    public DadosDetalhamentoTopico atualizar(Long id, DadosCadastroTopico dados, String token) {
        var topico = topicoRepository.getReferenceById(id);
        var autor = pegarAutorPeloToken(token);

        if (!autor.equals(topico.getAutor())){
            throw new UsuarioSemPerimssaoException("Usuario sem permissão");
        }

        validarDados(dados);

        topico.setTitulo(dados.titulo());
        topico.setMensagem(dados.mensagem());

        var curso = cursoRepository.getReferenceById(dados.curso_id());
        topico.setCurso(curso);

        return new DadosDetalhamentoTopico(topico);
    }

    @Transactional
    public void remover(Long id, String token) {
        var topico = topicoRepository.getReferenceById(id);
        var autor = pegarAutorPeloToken(token);

        if (!autor.equals(topico.getAutor())){
            throw new UsuarioSemPerimssaoException("Usuario sem permissão");
        }

        topicoRepository.deleteById(id);
    }

    private void validarDados(DadosCadastroTopico dados) {
        if (!cursoRepository.existsById(dados.curso_id())){
            throw new ValidacaoException("Curso não encontado");
        }
        var jaTemEsseTitulo = topicoRepository.existsByTitulo(dados.titulo());
        var jaTemEssaMensagem = topicoRepository.existsByMensagem(dados.mensagem());

        if (jaTemEsseTitulo || jaTemEssaMensagem){
            throw new ValidacaoException("Já existe esse topico");
        }
    }

    private Usuario pegarAutorPeloToken(String token){
        var email = tokenService.getSubject(token.replace("Bearer ", "").trim());
        return (Usuario) usuarioRepository.findByEmail(email);
    }
}
