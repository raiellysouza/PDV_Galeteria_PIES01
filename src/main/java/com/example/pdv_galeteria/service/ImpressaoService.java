package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ImpressaoService {

    @Value("${impressora.nome:}")
    private String nomeImpressoraSistema;

    @Value("${impressora.nomeComanda:PDV Galeteria}")
    private String nomeImpressora;

    @Value("${impressora.auto:true}")
    private boolean impressaoAutomatica;


    public ImpressaoService() {
        this.nomeImpressoraSistema = "";
        this.nomeImpressora = "PDV Galeteria";
        this.impressaoAutomatica = true;
    }
    

    public void setNomeImpressoraSistema(String nome) {
        this.nomeImpressoraSistema = nome;
    }

    private static final byte[] ESC = {0x1B};
    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};
    private static final byte[] FEED_LINE = {0x0A};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};

    public byte[] gerarComandaEscPos(Pedido pedido) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(INIT);
        
        outputStream.write(ESC);
        outputStream.write(0x74); 
        outputStream.write(0x02); 

        outputStream.write(ESC);
        outputStream.write(0x61);
        outputStream.write(0x00); 

        escreverLinha(outputStream, "-".repeat(32));
        String nomeComanda = nomeImpressora.replace("IRMAO", "IRMÃO");
        escreverLinha(outputStream, "     " + nomeComanda);
        escreverLinha(outputStream, "-".repeat(32));
        outputStream.write(FEED_LINE);

        outputStream.write(BOLD_ON);
        escreverLinhaSemPular(outputStream, String.format("PEDIDO #%06d", pedido.getId()));
        outputStream.write(BOLD_OFF);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        escreverLinhaSemPular(outputStream, "Data: " + pedido.getCriadoEm().format(formatter));

        if (pedido.getCliente() != null && !pedido.getCliente().trim().isEmpty()) {
            escreverLinhaSemPular(outputStream, "Cliente: " + pedido.getCliente());
        }

        if (pedido.getTelefone() != null && !pedido.getTelefone().trim().isEmpty()) {
            escreverLinhaSemPular(outputStream, "Telefone: " + pedido.getTelefone());
        }
        
        if (pedido.getEndereco() != null && !pedido.getEndereco().trim().isEmpty()) {
            String enderecoCompleto = pedido.getEndereco();
            if (pedido.getPontoReferencia() != null && !pedido.getPontoReferencia().trim().isEmpty()) {
                enderecoCompleto += " - " + pedido.getPontoReferencia();
            }
            escreverTextoQuebradoSemPular(outputStream, "Entrega: ", enderecoCompleto, 23);
            
            if (pedido.getTelefone() != null && !pedido.getTelefone().trim().isEmpty()) {
                escreverLinhaSemPular(outputStream, "Telefone: " + pedido.getTelefone());
            }
        }
        
        if (pedido.getTempoEstimado() != null && !pedido.getTempoEstimado().trim().isEmpty()) {
            escreverLinhaSemPular(outputStream, "Tempo estimado: " + pedido.getTempoEstimado());
        }
        
        outputStream.write(FEED_LINE);
        escreverLinhaSemPular(outputStream, "-".repeat(32));
        
        outputStream.write(BOLD_ON);
        escreverLinhaSemPular(outputStream, String.format("%-16s %3s %8s", "PRODUTO", "QTD", "TOTAL"));
        outputStream.write(BOLD_OFF);
        escreverLinhaSemPular(outputStream, "-".repeat(32));

        List<ItemPedido> itens = pedido.getItens();
        for (ItemPedido item : itens) {
            String produto = item.getProduto();
            if (produto.length() > 16) {
                produto = produto.substring(0, 16);
            }

            double subtotal = item.getQuantidade() * item.getPrecoUnitario();
            String linha = String.format("%-16s %3d R$ %7.2f",
                    produto, item.getQuantidade(), subtotal);
            escreverLinhaSemPular(outputStream, linha);
        }
        
        if (pedido.getTaxaEntrega() != null && pedido.getTaxaEntrega() > 0) {
            escreverLinhaSemPular(outputStream, String.format("%-20s R$ %7.2f",
                    "taxa de entrega", pedido.getTaxaEntrega()));
        }
        
        outputStream.write(FEED_LINE);
        
        if (pedido.getObservacoes() != null && !pedido.getObservacoes().trim().isEmpty()) {
            escreverTextoQuebradoSemPular(outputStream, "obs: ", pedido.getObservacoes(), 28);
            outputStream.write(FEED_LINE);
        }

        escreverLinhaSemPular(outputStream, "-".repeat(32));

        double totalComTaxa = pedido.getTotal();
        if (pedido.getTaxaEntrega() != null) {
            totalComTaxa += pedido.getTaxaEntrega();
        }
        outputStream.write(BOLD_ON);
        escreverLinhaSemPular(outputStream, String.format("TOTAL: R$ %.2f", totalComTaxa));
        outputStream.write(BOLD_OFF);
        
        if (pedido.getFormaPagamento() != null && !pedido.getFormaPagamento().trim().isEmpty()) {
            escreverLinhaSemPular(outputStream, "Pagamento: " + pedido.getFormaPagamento());
        }
        
        if (pedido.getValorPago() != null && pedido.getValorPago() > 0) {
            escreverLinhaSemPular(outputStream, String.format("Valor Pago: R$ %.2f", pedido.getValorPago()));
        }
        
        if (pedido.getTroco() != null && pedido.getTroco() > 0) {
            escreverLinhaSemPular(outputStream, String.format("Troco: R$ %.2f", pedido.getTroco()));
        }

        escreverLinhaSemPular(outputStream, "-".repeat(32));
        escreverLinhaSemPular(outputStream, "  Obrigado pela preferência!");
        escreverLinhaSemPular(outputStream, "-".repeat(32));
        outputStream.write(FEED_LINE);
        outputStream.write(FEED_LINE);
        outputStream.write(FEED_LINE);

        outputStream.write(CUT);

        return outputStream.toByteArray();
    }
    
    private void escreverLinha(ByteArrayOutputStream outputStream, String texto) throws IOException {
        byte[] bytes = converterParaCP860(texto);
        outputStream.write(bytes);
        outputStream.write(FEED_LINE);
    }
    
    private void escreverLinhaSemPular(ByteArrayOutputStream outputStream, String texto) throws IOException {
        byte[] bytes = converterParaCP860(texto);
        outputStream.write(bytes);
        outputStream.write(FEED_LINE);
    }
    
    private byte[] converterParaCP860(String texto) {
        try {
            java.nio.charset.Charset charset = java.nio.charset.Charset.forName("IBM860");
            return texto.getBytes(charset);
        } catch (Exception e) {
            try {
                return texto.getBytes("CP850");
            } catch (Exception e2) {
                try {
                    return texto.getBytes("ISO-8859-1");
                } catch (Exception e3) {
                    return texto.getBytes(StandardCharsets.UTF_8);
                }
            }
        }
    }
    
    private void escreverTextoQuebrado(ByteArrayOutputStream outputStream, String prefixo, String texto, int larguraMaxima) throws IOException {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        
        int larguraPrimeiraLinha = larguraMaxima - prefixo.length();
        
        if (texto.length() <= larguraPrimeiraLinha) {
            escreverLinha(outputStream, prefixo + texto);
            return;
        }
        
        String[] palavras = texto.split(" ");
        StringBuilder linhaAtual = new StringBuilder();
        boolean primeiraLinha = true;
        
        for (String palavra : palavras) {
            int larguraLinha = primeiraLinha ? larguraPrimeiraLinha : larguraMaxima;
            
            if (linhaAtual.length() + palavra.length() + 1 <= larguraLinha) {
                if (linhaAtual.length() > 0) {
                    linhaAtual.append(" ");
                }
                linhaAtual.append(palavra);
            } else {
                if (primeiraLinha) {
                    escreverLinha(outputStream, prefixo + linhaAtual);
                    primeiraLinha = false;
                } else {
                    escreverLinha(outputStream, linhaAtual.toString());
                }
                linhaAtual = new StringBuilder(palavra);
            }
        }

        if (linhaAtual.length() > 0) {
            if (primeiraLinha) {
                escreverLinha(outputStream, prefixo + linhaAtual);
            } else {
                escreverLinha(outputStream, linhaAtual.toString());
            }
        }
    }

    private void escreverTextoQuebradoSemPular(ByteArrayOutputStream outputStream, String prefixo, String texto, int larguraMaxima) throws IOException {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        
        int larguraPrimeiraLinha = larguraMaxima - prefixo.length();
        
        if (texto.length() <= larguraPrimeiraLinha) {
            escreverLinhaSemPular(outputStream, prefixo + texto);
            return;
        }
        
        String[] palavras = texto.split(" ");
        StringBuilder linhaAtual = new StringBuilder();
        boolean primeiraLinha = true;
        
        for (String palavra : palavras) {
            int larguraLinha = primeiraLinha ? larguraPrimeiraLinha : larguraMaxima;
            
            if (linhaAtual.length() + palavra.length() + 1 <= larguraLinha) {
                if (linhaAtual.length() > 0) {
                    linhaAtual.append(" ");
                }
                linhaAtual.append(palavra);
            } else {
                if (primeiraLinha) {
                    escreverLinhaSemPular(outputStream, prefixo + linhaAtual);
                    primeiraLinha = false;
                } else {
                    escreverLinhaSemPular(outputStream, linhaAtual.toString());
                }
                linhaAtual = new StringBuilder(palavra);
            }
        }
        
        if (linhaAtual.length() > 0) {
            if (primeiraLinha) {
                escreverLinhaSemPular(outputStream, prefixo + linhaAtual);
            } else {
                escreverLinhaSemPular(outputStream, linhaAtual.toString());
            }
        }
    }

    public File salvarArquivoImpressao(Pedido pedido) throws IOException {
        byte[] dadosEscPos = gerarComandaEscPos(pedido);

        File diretorioImpressoes = new File("impressoes");
        if (!diretorioImpressoes.exists()) {
            diretorioImpressoes.mkdirs();
        }

        String nomeArquivo = String.format("comanda_%d_%s.escpos",
                pedido.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        File arquivo = new File(diretorioImpressoes, nomeArquivo);

        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            fos.write(dadosEscPos);
        }

        return arquivo;
    }

    public boolean imprimirComanda(Pedido pedido) {
        try {
            byte[] dadosEscPos = gerarComandaEscPos(pedido);

            if (imprimirViaPrintService(dadosEscPos, pedido)) {
                return true;
            } else {
                salvarArquivoImpressao(pedido);
                System.out.println("ℹ Impressão não realizada. Arquivo salvo em disco.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Erro ao gerar comanda: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean imprimirViaPrintService(byte[] dadosEscPos, Pedido pedido) {
        try {
            PrintService impressora = buscarImpressora();
            
            if (impressora == null) {
                System.err.println("Nenhuma impressora encontrada.");
                System.err.println("Verifique se a impressora está instalada e configurada.");
                listarImpressorasDisponiveis();
                return false;
            }

            System.out.println("Impressora encontrada: " + impressora.getName());

            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(dadosEscPos, flavor, null);

            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new Copies(1));
            DocPrintJob printJob = impressora.createPrintJob();
            System.out.println("Enviando dados para impressão...");
            printJob.print(doc, attributes);
            System.out.println("Dados enviados para a impressora com sucesso!");
            
            return true;
            
        } catch (PrintException e) {
            System.err.println("Erro ao imprimir: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao imprimir: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private PrintService buscarImpressora() {
        PrintService[] servicos = PrintServiceLookup.lookupPrintServices(null, null);
        
        if (nomeImpressoraSistema != null && !nomeImpressoraSistema.trim().isEmpty()) {
            String nomeBusca = nomeImpressoraSistema.trim();
            System.out.println("Buscando impressora: " + nomeBusca);
            
            for (PrintService servico : servicos) {
                String nomeServico = servico.getName();
                if (nomeServico.toLowerCase().contains(nomeBusca.toLowerCase())) {
                    return servico;
                }
            }
            
            System.err.println("Impressora '" + nomeBusca + "' não encontrada.");
            System.err.println("Impressoras disponíveis:");
            for (PrintService servico : servicos) {
                System.err.println("    - " + servico.getName());
            }
            return null;
        }
        
        PrintService padrao = PrintServiceLookup.lookupDefaultPrintService();
        if (padrao != null) {
            System.out.println("Usando impressora padrão: " + padrao.getName());
            return padrao;
        }
        
        return null;
    }

    private void listarImpressorasDisponiveis() {
        PrintService[] servicos = PrintServiceLookup.lookupPrintServices(null, null);
        
        if (servicos.length == 0) {
            System.err.println("Nenhuma impressora encontrada no sistema.");
        } else {
            System.err.println("Impressoras disponíveis:");
            for (PrintService servico : servicos) {
                System.err.println("    - " + servico.getName());
            }
        }
    }

    public void imprimirAutomaticamente(Pedido pedido) {
        if (impressaoAutomatica) {
            try {
                boolean sucesso = imprimirComanda(pedido);
                if (sucesso) {
                    System.out.println("Comanda impressa automaticamente para o pedido #" + pedido.getId());
                } else {
                    File arquivo = salvarArquivoImpressao(pedido);
                    System.out.println("Arquivo de impressão salvo em: " + arquivo.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Erro na impressão automática: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public String gerarPreviewComanda(Pedido pedido) {
        StringBuilder preview = new StringBuilder();
        
        preview.append("-".repeat(32)).append("\n");
        preview.append("\n");
        String nomeComanda = nomeImpressora.replace("IRMAO", "IRMÃO");
        preview.append("     ").append(nomeComanda).append("\n");
        preview.append("\n");
        preview.append("-".repeat(32)).append("\n");
        preview.append("\n");
        preview.append(String.format("PEDIDO #%06d", pedido.getId())).append("\n");
        preview.append("\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        preview.append("Data: ").append(pedido.getCriadoEm().format(formatter)).append("\n");
        preview.append("\n");
        
        if (pedido.getCliente() != null && !pedido.getCliente().trim().isEmpty()) {
            preview.append("Cliente: ").append(pedido.getCliente()).append("\n");
            preview.append("\n");
        }
        
        if (pedido.getTelefone() != null && !pedido.getTelefone().trim().isEmpty()) {
            preview.append("Telefone: ").append(pedido.getTelefone()).append("\n");
            preview.append("\n");
        }
        
        if (pedido.getEndereco() != null && !pedido.getEndereco().trim().isEmpty()) {
            String enderecoCompleto = pedido.getEndereco();
            if (pedido.getPontoReferencia() != null && !pedido.getPontoReferencia().trim().isEmpty()) {
                enderecoCompleto += " - " + pedido.getPontoReferencia();
            }
            preview.append("Entrega: ").append(enderecoCompleto).append("\n");
            preview.append("\n");
            
            if (pedido.getTelefone() != null && !pedido.getTelefone().trim().isEmpty()) {
                preview.append("Telefone: ").append(pedido.getTelefone()).append("\n");
                preview.append("\n");
            }
        }
        
        if (pedido.getTempoEstimado() != null && !pedido.getTempoEstimado().trim().isEmpty()) {
            preview.append("Tempo estimado: ").append(pedido.getTempoEstimado()).append("\n");
            preview.append("\n");
        }
        
        preview.append("-".repeat(35)).append("\n");
        preview.append("\n");
        preview.append(String.format("%-20s %3s %10s", "PRODUTO", "QTD", "TOTAL")).append("\n");
        preview.append("\n");
        preview.append("-".repeat(35)).append("\n");
        
    
        List<ItemPedido> itens = pedido.getItens();
        for (ItemPedido item : itens) {
            String produto = item.getProduto();
            if (produto.length() > 20) {
                produto = produto.substring(0, 20);
            }
            
            double subtotal = item.getQuantidade() * item.getPrecoUnitario();
            String linha = String.format("%-20s %3d R$ %10.2f",
                    produto, item.getQuantidade(), subtotal);
            preview.append(linha).append("\n");
        }
        
    
        if (pedido.getTaxaEntrega() != null && pedido.getTaxaEntrega() > 0) {
            preview.append(String.format("%-24s R$ %10.2f",
                    "taxa de entrega", pedido.getTaxaEntrega())).append("\n");
        }
        
        preview.append("\n");
        preview.append("\n");
        
    
        if (pedido.getObservacoes() != null && !pedido.getObservacoes().trim().isEmpty()) {
            preview.append("obs: ").append(pedido.getObservacoes()).append("\n");
        }
        
        preview.append("\n");
        preview.append("-".repeat(35)).append("\n");
        
        preview.append("\n");
        
    
        double totalComTaxa = pedido.getTotal();
        if (pedido.getTaxaEntrega() != null) {
            totalComTaxa += pedido.getTaxaEntrega();
        }
        preview.append(String.format("TOTAL: R$ %.2f", totalComTaxa)).append("\n");
        preview.append("\n");
        
        if (pedido.getFormaPagamento() != null && !pedido.getFormaPagamento().trim().isEmpty()) {
            preview.append("Pagamento: ").append(pedido.getFormaPagamento()).append("\n");
            preview.append("\n");
        }
        
        if (pedido.getValorPago() != null && pedido.getValorPago() > 0) {
            preview.append(String.format("Valor Pago: R$ %.2f", pedido.getValorPago())).append("\n");
            preview.append("\n");
        }
        
        if (pedido.getTroco() != null && pedido.getTroco() > 0) {
            preview.append(String.format("Troco: R$ %.2f", pedido.getTroco())).append("\n");
            preview.append("\n");
        }
        
        preview.append("-".repeat(35)).append("\n");
        preview.append("\n");
        preview.append("  Obrigado pela preferência!").append("\n");
        preview.append("\n");
        preview.append("-".repeat(35)).append("\n");
        
        return preview.toString();
    }

    public File salvarPreviewComanda(Pedido pedido) throws IOException {
        String preview = gerarPreviewComanda(pedido);
        
        File diretorioImpressoes = new File("impressoes");
        if (!diretorioImpressoes.exists()) {
            diretorioImpressoes.mkdirs();
        }

        String nomeArquivo = String.format("preview_comanda_%d_%s.txt",
                pedido.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        File arquivo = new File(diretorioImpressoes, nomeArquivo);
        
        try (FileWriter writer = new FileWriter(arquivo, StandardCharsets.UTF_8)) {
            writer.write(preview);
        }
        
        return arquivo;
    }

    private String centralizarTexto(String texto, int largura) {
        if (texto.length() >= largura) {
            return texto.substring(0, largura);
        }
        int espacos = (largura - texto.length()) / 2;
        return " ".repeat(espacos) + texto;
    }
}

