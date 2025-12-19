package com.example.pdv_galeteria.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO;

public class GeradorRelatorioPDF {

    private static final float MARGIN = 40;
    private static final float LEADING = 14;

    private GeradorRelatorioPDF() {
    }

    public static void gerar(
            List<RelatorioMovimentoCaixaDTO> movimentos,
            Path caminhoArquivo
    ) {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - MARGIN;

            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            escreverLinha(content, "Relatório de Vendas", MARGIN, y);
            y -= LEADING * 2;

            content.setFont(PDType1Font.HELVETICA, 10);
            escreverLinha(content, "ID | Data | Descrição | Tipo | Valor", MARGIN, y);
            y -= LEADING;

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
                y -= LEADING;

                total = total.add(m.getValor());

                if (y < MARGIN) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }
            }

            y -= LEADING;
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            escreverLinha(content, "Total vendido: R$ " + total, MARGIN, y);

            content.close();
            document.save(caminhoArquivo.toFile());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    private static void escreverLinha(
            PDPageContentStream content,
            String texto,
            float x,
            float y
    ) throws IOException {

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
