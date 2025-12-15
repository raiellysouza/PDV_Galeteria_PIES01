package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.Pedido;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final Color COR_PRIMARIA = new DeviceRgb(124, 0, 0); // #7C0000

    public File gerarReciboPedido(Pedido pedido, String caminhoArquivo) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(caminhoArquivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        document.setMargins(40, 40, 40, 40);
        adicionarCabecalho(document, pedido);
        adicionarInformacoesPedido(document, pedido);
        adicionarItensPedido(document, pedido);
        adicionarTotal(document, pedido);
        adicionarRodape(document);

        document.close();
        return new File(caminhoArquivo);
    }

    private void adicionarCabecalho(Document document, Pedido pedido) {
        Paragraph titulo = new Paragraph("RECIBO DE PEDIDO")
                .setFontSize(24)
                .setBold()
                .setFontColor(COR_PRIMARIA)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);

        Paragraph empresa = new Paragraph("GALETERIA PDV")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);

        document.add(titulo);
        document.add(empresa);

        Paragraph linhaDivisoria = new Paragraph("________________________________________________")
                .setFontColor(COR_PRIMARIA)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(linhaDivisoria);
    }

    private void adicionarInformacoesPedido(Document document, Pedido pedido) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Paragraph infoPedido = new Paragraph()
                .add(new Text("Nº do Pedido: ").setBold())
                .add(String.format("#%03d\n", pedido.getId()))
                .add(new Text("Data: ").setBold())
                .add(pedido.getCriadoEm().format(dtf) + "\n")
                .add(new Text("Cliente: ").setBold())
                .add(pedido.getCliente() + "\n")
                .add(new Text("Status: ").setBold())
                .add(pedido.getStatus().toString() + "\n")
                .add(new Text("Pagamento: ").setBold())
                .add(pedido.getFormaPagamento() + "\n")
                .add(new Text("Entrega: ").setBold())
                .add(pedido.getTipoEntrega() + "\n\n")
                .setMarginBottom(20);

        document.add(infoPedido);
    }

    private void adicionarItensPedido(Document document, Pedido pedido) {
        Paragraph tituloItens = new Paragraph("ITENS DO PEDIDO")
                .setBold()
                .setFontSize(14)
                .setFontColor(COR_PRIMARIA)
                .setMarginBottom(10);

        document.add(tituloItens);

        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            for (ItemPedido item : pedido.getItens()) {
                double subtotal = item.getQuantidade() * item.getPrecoUnitario();

                Paragraph itemPara = new Paragraph()
                        .add(String.format("%dx ", item.getQuantidade()))
                        .add(item.getProduto() + " - ")
                        .add(String.format("R$ %.2f cada", item.getPrecoUnitario()))
                        .add(" | Subtotal: ")
                        .add(new Text(String.format("R$ %.2f", subtotal)).setBold())
                        .setMarginBottom(5);

                document.add(itemPara);
            }
        }

        document.add(new Paragraph("\n"));
    }

    private void adicionarTotal(Document document, Pedido pedido) {
        Paragraph linha = new Paragraph("________________________________________________")
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(linha);

        Paragraph total = new Paragraph()
                .add(new Text("TOTAL: ").setBold().setFontSize(16))
                .add(new Text(String.format("R$ %.2f", pedido.getTotal()))
                        .setBold()
                        .setFontSize(16)
                        .setFontColor(COR_PRIMARIA))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(30);

        document.add(total);
    }

    private void adicionarRodape(Document document) {
        Paragraph agradecimento = new Paragraph("Obrigado pela preferência!")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setMarginTop(20);

        document.add(agradecimento);
    }
}
