package com.example.pdv_galeteria.util;

import com.example.pdv_galeteria.dto.PedidoResumoDTO;
import com.example.pdv_galeteria.dto.ProdutoMaisVendidoDTO;
import com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO;
import com.example.pdv_galeteria.dto.RelatorioVendasDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class GeradorRelatorioPDF {

    private static final float MARGIN = 50;
    private static final float MARGIN_LEFT = MARGIN;
    private static final float MARGIN_RIGHT = MARGIN;
    private static final float MARGIN_TOP = MARGIN;
    private static final float MARGIN_BOTTOM = MARGIN;

    private static final float LINE_HEIGHT = 14;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float SUBTITLE_FONT_SIZE = 14;
    private static final float NORMAL_FONT_SIZE = 10;
    private static final float SMALL_FONT_SIZE = 9;

    private static final PDType1Font FONT_HELVETICA = PDType1Font.HELVETICA;
    private static final PDType1Font FONT_HELVETICA_BOLD = PDType1Font.HELVETICA_BOLD;

    public static void gerarRelatorioVendas(
            RelatorioVendasDTO relatorio,
            List<PedidoResumoDTO> pedidos,
            List<ProdutoMaisVendidoDTO> produtosMaisVendidos,
            Map<String, BigDecimal> distribuicaoPagamento,
            LocalDate dataInicio,
            LocalDate dataFim,
            Path caminhoArquivo
    ) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float currentY = pageHeight - MARGIN_TOP;

            contentStream.setFont(FONT_HELVETICA_BOLD, TITLE_FONT_SIZE);
            writeCenteredText(contentStream, "RELATÓRIO DE VENDAS", pageWidth / 2, currentY);
            currentY -= LINE_HEIGHT * 2;

            contentStream.setFont(FONT_HELVETICA_BOLD, NORMAL_FONT_SIZE);
            writeCenteredText(contentStream, "PDV Galeteria", pageWidth / 2, currentY);
            currentY -= LINE_HEIGHT * 1.5f;

            contentStream.setFont(FONT_HELVETICA, NORMAL_FONT_SIZE);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String periodo = String.format("Período: %s a %s",
                    dataInicio.format(dateFormatter),
                    dataFim.format(dateFormatter));
            writeCenteredText(contentStream, periodo, pageWidth / 2, currentY);
            currentY -= LINE_HEIGHT * 2;

            contentStream.setFont(FONT_HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
            writeText(contentStream, "RESUMO DE VENDAS", MARGIN_LEFT, currentY);
            currentY -= LINE_HEIGHT * 1.5f;

            contentStream.setFont(FONT_HELVETICA, NORMAL_FONT_SIZE);

            writeText(contentStream, "Total de Vendas:", MARGIN_LEFT + 20, currentY);
            writeText(contentStream, String.format("R$ %.2f", relatorio.getTotalVendas().doubleValue()),
                    pageWidth - MARGIN_RIGHT - 100, currentY);
            currentY -= LINE_HEIGHT;

            writeText(contentStream, "Total de Pedidos:", MARGIN_LEFT + 20, currentY);
            writeText(contentStream, String.valueOf(relatorio.getTotalPedidos()),
                    pageWidth - MARGIN_RIGHT - 100, currentY);
            currentY -= LINE_HEIGHT;

            writeText(contentStream, "Total de Itens Vendidos:", MARGIN_LEFT + 20, currentY);
            writeText(contentStream, String.valueOf(relatorio.getQuantidadeItens()),
                    pageWidth - MARGIN_RIGHT - 100, currentY);
            currentY -= LINE_HEIGHT;

            if (relatorio.getTotalPedidos() > 0) {
                double ticketMedio = relatorio.getTotalVendas().doubleValue() / relatorio.getTotalPedidos();
                writeText(contentStream, "Ticket Médio:", MARGIN_LEFT + 20, currentY);
                writeText(contentStream, String.format("R$ %.2f", ticketMedio),
                        pageWidth - MARGIN_RIGHT - 100, currentY);
                currentY -= LINE_HEIGHT;
            }

            currentY -= LINE_HEIGHT;

            if (distribuicaoPagamento != null && !distribuicaoPagamento.isEmpty()) {
                contentStream.setFont(FONT_HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                writeText(contentStream, "DISTRIBUIÇÃO DE PAGAMENTO", MARGIN_LEFT, currentY);
                currentY -= LINE_HEIGHT * 1.5f;

                contentStream.setFont(FONT_HELVETICA, NORMAL_FONT_SIZE);

                writeText(contentStream, "Forma de Pagamento", MARGIN_LEFT + 20, currentY);
                writeText(contentStream, "Percentual", pageWidth - MARGIN_RIGHT - 100, currentY);
                currentY -= LINE_HEIGHT;

                for (Map.Entry<String, BigDecimal> entry : distribuicaoPagamento.entrySet()) {
                    if (currentY < MARGIN_BOTTOM + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = pageHeight - MARGIN_TOP;
                    }

                    writeText(contentStream, entry.getKey(), MARGIN_LEFT + 20, currentY);
                    writeText(contentStream, String.format("%.1f%%", entry.getValue().doubleValue()),
                            pageWidth - MARGIN_RIGHT - 100, currentY);
                    currentY -= LINE_HEIGHT;
                }

                currentY -= LINE_HEIGHT;
            }

            if (produtosMaisVendidos != null && !produtosMaisVendidos.isEmpty()) {
                if (currentY < MARGIN_BOTTOM + 150) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    currentY = pageHeight - MARGIN_TOP;
                }

                contentStream.setFont(FONT_HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                writeText(contentStream, "PRODUTOS MAIS VENDIDOS", MARGIN_LEFT, currentY);
                currentY -= LINE_HEIGHT * 1.5f;

                contentStream.setFont(FONT_HELVETICA_BOLD, NORMAL_FONT_SIZE);

                writeText(contentStream, "Produto", MARGIN_LEFT + 20, currentY);
                writeText(contentStream, "Qtd", MARGIN_LEFT + 200, currentY);
                writeText(contentStream, "Total (R$)", pageWidth - MARGIN_RIGHT - 100, currentY);
                currentY -= LINE_HEIGHT;

                contentStream.setFont(FONT_HELVETICA, NORMAL_FONT_SIZE);

                int limiteProdutos = Math.min(produtosMaisVendidos.size(), 10);
                for (int i = 0; i < limiteProdutos; i++) {
                    if (currentY < MARGIN_BOTTOM + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = pageHeight - MARGIN_TOP;
                    }

                    ProdutoMaisVendidoDTO produto = produtosMaisVendidos.get(i);
                    String nomeResumido = produto.getNomeProduto();
                    if (nomeResumido.length() > 30) {
                        nomeResumido = nomeResumido.substring(0, 27) + "...";
                    }

                    writeText(contentStream, nomeResumido, MARGIN_LEFT + 20, currentY);
                    writeText(contentStream, String.valueOf(produto.getQuantidadeVendida()),
                            MARGIN_LEFT + 200, currentY);
                    writeText(contentStream, String.format("%.2f", produto.getValorTotal().doubleValue()),
                            pageWidth - MARGIN_RIGHT - 100, currentY);
                    currentY -= LINE_HEIGHT;
                }

                currentY -= LINE_HEIGHT;
            }

            if (pedidos != null && !pedidos.isEmpty()) {
                if (currentY < MARGIN_BOTTOM + 150) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    currentY = pageHeight - MARGIN_TOP;
                }

                contentStream.setFont(FONT_HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                writeText(contentStream, "ÚLTIMOS PEDIDOS", MARGIN_LEFT, currentY);
                currentY -= LINE_HEIGHT * 1.5f;

                contentStream.setFont(FONT_HELVETICA_BOLD, NORMAL_FONT_SIZE);
                float[] columnPositions = {
                        MARGIN_LEFT + 20,
                        MARGIN_LEFT + 50,
                        MARGIN_LEFT + 150,
                        pageWidth - MARGIN_RIGHT - 120,
                        pageWidth - MARGIN_RIGHT - 70
                };

                writeText(contentStream, "ID", columnPositions[0], currentY);
                writeText(contentStream, "Cliente", columnPositions[1], currentY);
                writeText(contentStream, "Produtos", columnPositions[2], currentY);
                writeText(contentStream, "Valor", columnPositions[3], currentY);
                writeText(contentStream, "Pagamento", columnPositions[4], currentY);
                currentY -= LINE_HEIGHT;

                contentStream.setFont(FONT_HELVETICA, SMALL_FONT_SIZE);

                int limitePedidos = Math.min(pedidos.size(), 15);
                for (int i = 0; i < limitePedidos; i++) {
                    if (currentY < MARGIN_BOTTOM + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = pageHeight - MARGIN_TOP;

                        contentStream.setFont(FONT_HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                        writeText(contentStream, "ÚLTIMOS PEDIDOS (cont.)", MARGIN_LEFT, currentY);
                        currentY -= LINE_HEIGHT * 1.5f;

                        contentStream.setFont(FONT_HELVETICA_BOLD, NORMAL_FONT_SIZE);
                        writeText(contentStream, "ID", columnPositions[0], currentY);
                        writeText(contentStream, "Cliente", columnPositions[1], currentY);
                        writeText(contentStream, "Produtos", columnPositions[2], currentY);
                        writeText(contentStream, "Valor", columnPositions[3], currentY);
                        writeText(contentStream, "Pagamento", columnPositions[4], currentY);
                        currentY -= LINE_HEIGHT;

                        contentStream.setFont(FONT_HELVETICA, SMALL_FONT_SIZE);
                    }

                    PedidoResumoDTO pedido = pedidos.get(i);
                    String clienteResumido = pedido.getCliente();
                    if (clienteResumido.length() > 15) {
                        clienteResumido = clienteResumido.substring(0, 12) + "...";
                    }

                    String produtosResumido = pedido.getProdutos();
                    if (produtosResumido.length() > 25) {
                        produtosResumido = produtosResumido.substring(0, 22) + "...";
                    }

                    String pagamentoResumido = pedido.getFormaPagamento();
                    if (pagamentoResumido.length() > 10) {
                        pagamentoResumido = pagamentoResumido.substring(0, 7) + "...";
                    }

                    writeText(contentStream, String.valueOf(pedido.getId()), columnPositions[0], currentY);
                    writeText(contentStream, clienteResumido, columnPositions[1], currentY);
                    writeText(contentStream, produtosResumido, columnPositions[2], currentY);
                    writeText(contentStream, pedido.getValorFormatado(), columnPositions[3], currentY);
                    writeText(contentStream, pagamentoResumido, columnPositions[4], currentY);
                    currentY -= LINE_HEIGHT;
                }
            }

            contentStream.close();

            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream footerStream = new PDPageContentStream(document, page);

            try {
                float footerY = pageHeight - MARGIN_TOP;

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                footerStream.setFont(FONT_HELVETICA, SMALL_FONT_SIZE);
                writeCenteredText(footerStream,
                        String.format("Relatório gerado em: %s",
                                LocalDateTime.now().format(dateTimeFormatter)),
                        pageWidth / 2, footerY);
                footerY -= LINE_HEIGHT * 2;

                writeCenteredText(footerStream, "______________________________________", pageWidth / 2, footerY);
                footerY -= LINE_HEIGHT;
                writeCenteredText(footerStream, "PDV Galeteria - Sistema de Gestão", pageWidth / 2, footerY);
            } finally {
                footerStream.close();
            }

            document.save(caminhoArquivo.toFile());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório PDF: " + e.getMessage(), e);
        }
    }

    private static void writeCenteredText(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        float textWidth = FONT_HELVETICA.getStringWidth(text) / 1000 * NORMAL_FONT_SIZE;
        contentStream.beginText();
        contentStream.newLineAtOffset(x - (textWidth / 2), y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private static void writeText(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public static void gerar(List<RelatorioMovimentoCaixaDTO> movimentos, Path caminhoArquivo) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - MARGIN;

            content.setFont(FONT_HELVETICA_BOLD, 16);
            escreverLinha(content, "Relatório de Vendas", MARGIN, y);
            y -= LINE_HEIGHT * 2;

            content.setFont(FONT_HELVETICA, 10);
            escreverLinha(content, "ID | Data | Descrição | Tipo | Valor", MARGIN, y);
            y -= LINE_HEIGHT;

            BigDecimal total = BigDecimal.ZERO;

            for (RelatorioMovimentoCaixaDTO m : movimentos) {
                String linha = String.format(
                        "%d | %s | %s | %s | R$ %s",
                        m.getId(),
                        m.getDataMovimento(),
                        limitarTexto(m.getDescricao(), 25),
                        m.getTipo(),
                        m.getValor()
                );

                escreverLinha(content, linha, MARGIN, y);
                y -= LINE_HEIGHT;

                total = total.add(m.getValor());

                if (y < MARGIN) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }
            }

            y -= LINE_HEIGHT;
            content.setFont(FONT_HELVETICA_BOLD, 12);
            escreverLinha(content, "Total vendido: R$ " + total, MARGIN, y);

            content.close();
            document.save(caminhoArquivo.toFile());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    private static void escreverLinha(PDPageContentStream content, String texto, float x, float y) throws IOException {
        content.beginText();
        content.newLineAtOffset(x, y);
        content.showText(texto);
        content.endText();
    }

    private static String limitarTexto(String texto, int limite) {
        if (texto == null) {
            return "";
        }
        return texto.length() > limite
                ? texto.substring(0, limite - 3) + "..."
                : texto;
    }
}