package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Usuario;
import com.example.pdv_galeteria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    @Autowired
    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }


    public Usuario cadastrar(String login, String senhaPlain) {
        if (login == null || login.isBlank() || senhaPlain == null || senhaPlain.isBlank()) {
            throw new IllegalArgumentException("login e senha são obrigatórios");
        }

        if (repo.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("login já existe");
        }

        String hash = hashSenha(senhaPlain);
        Usuario u = new Usuario(login, hash);
        return repo.save(u);
    }

    public boolean autenticar(String login, String senhaDigitada) {
        Optional<Usuario> opt = repo.findByLogin(login);
        if (opt.isEmpty()) return false;

        Usuario u = opt.get();
        return BCrypt.checkpw(senhaDigitada, u.getSenha());
    }


    public boolean alterarSenha(String login, String senhaAtual, String novaSenha) {
        Optional<Usuario> opt = repo.findByLogin(login);
        if (opt.isEmpty()) return false;

        Usuario u = opt.get();
        if (!BCrypt.checkpw(senhaAtual, u.getSenha())) return false;

        u.setSenha(hashSenha(novaSenha));
        repo.save(u);
        return true;
    }


    public String regenerarCodigo(String login) {
        Optional<Usuario> opt = repo.findByLogin(login);
        if (opt.isEmpty()) return null;

        Usuario u = opt.get();
        String novo = u.regenerarCodigo();
        repo.save(u);
        return novo;
    }


    public boolean recuperarSenhaPorCodigo(String login, String codigo, String novaSenha) {
        Optional<Usuario> opt = repo.findByLogin(login);
        if (opt.isEmpty()) return false;

        Usuario u = opt.get();
        if (!u.getCodigoRecuperacao().equals(codigo)) return false;

        u.setSenha(hashSenha(novaSenha));
        u.regenerarCodigo();
        repo.save(u);
        return true;
    }


    private String hashSenha(String senhaPlain) {
        int workload = 12;
        String salt = BCrypt.gensalt(workload);
        return BCrypt.hashpw(senhaPlain, salt);
    }
    
    
}
