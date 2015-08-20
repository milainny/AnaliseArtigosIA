/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analiseartigosia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import sun.awt.FontDescriptor;

/**
 *
 * @author Lailla
 */
public class AnaliseArtigosIA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String caminhoEntrada = "arquivos/4.pdf";
        String identificador = caminhoEntrada.substring(caminhoEntrada.lastIndexOf("/") + 1, caminhoEntrada.lastIndexOf("."));
        String caminhoArquivoStopWords = "stop_words/lista_stop_words.txt";
        String caminhoSaidaSemStopWords = "saidas/" + identificador + ".txt";
        String textoCompleto = extraiTextoDoPDF(caminhoEntrada);
        String textoSemStopWords = extraiStopWords(textoCompleto, caminhoArquivoStopWords);
        gravaTextoSemStopWords(caminhoSaidaSemStopWords, textoSemStopWords);
        exibeTermosMaisCitados(textoSemStopWords);
        identificaInstituicoes(textoCompleto);
    }

    public static String extraiTextoDoPDF(String caminho) throws IOException {
        PDDocument pdfDocument = null;
        pdfDocument = PDDocument.load(caminho);
        PDFTextStripper stripper = new PDFTextStripper();
        String texto = stripper.getText(pdfDocument);
        PrintStream saidaPadrao = System.out;
        PrintStream saida = new PrintStream(new FileOutputStream(new File("saidas/texto" + 19 + ".txt")));
        System.setOut(saida);
        System.out.println(texto);
        System.setOut(saidaPadrao);
        pdfDocument.close();
        return texto;
    }

    public static String extraiStopWords(String textoCompleto, String caminhoArquivoStopWords) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(caminhoArquivoStopWords));
        ArrayList<String> listaStopWords = new ArrayList();
        while (scanner.hasNext()) {
            listaStopWords.add(scanner.next());
        }
        String textoCompletoMin = textoCompleto.toLowerCase();
        textoCompletoMin = textoCompletoMin.replace("\r", "");
        textoCompletoMin = textoCompletoMin.replaceAll("\\W", " ");
        textoCompletoMin = textoCompletoMin.replaceAll("  ", " ");
        textoCompletoMin = textoCompletoMin.replaceAll("  ", " ");
        for (String stopWord : listaStopWords) {
            textoCompletoMin = textoCompletoMin.replace(" " + stopWord + " ", " ");
        }
        return textoCompletoMin;
    }

    public static void gravaTextoSemStopWords(String caminhoArquivoStopWords, String textoSemStopWords) throws FileNotFoundException {
        PrintStream saidaPadrao = System.out;
        PrintStream saida = new PrintStream(new FileOutputStream(new File(caminhoArquivoStopWords)));
        System.setOut(saida);
        System.out.println(textoSemStopWords);
        System.setOut(saidaPadrao);
        System.out.println("Arquivo do texto sem stop words gerado com sucesso. Veja a saida em " + caminhoArquivoStopWords);
    }

    public static void exibeTermosMaisCitados(String textoSemStopWords) {
        int indexReferencia = textoSemStopWords.lastIndexOf(" reference ") != -1 ? textoSemStopWords.lastIndexOf(" reference ") : textoSemStopWords.lastIndexOf(" references ");
        String textoSemReferencias = textoSemStopWords.substring(0, indexReferencia);
        String todosTermos[] = textoSemReferencias.split(" ");
        Map<String, Integer> termos = new HashMap();
        for (String termo : todosTermos) {
            if (termos.get(termo) == null) {
                termos.put(termo, 1);
            } else {
                int valor = termos.get(termo);
                termos.put(termo, ++valor);
            }
        }
        PriorityQueue<Termo> termosOrdenados = new PriorityQueue<>();
        Set<String> chaves = termos.keySet();
        for (String chave : chaves) {
            termosOrdenados.add(new Termo(termos.get(chave), chave));
        }
        System.out.println("--- Listando os 10 termos mais utilizados ---");
        int i = 0;
        while (i < 10) {
            Termo t = termosOrdenados.poll();
            if (t.getTermo().length() > 1) {
                System.out.println("Termo: " + t.getTermo() + " - Frequencia: " + t.getFrequencia());
                i++;
            }
        }
    }

    private static void identificaInstituicoes(String textoCompleto) {
        String instituicoes = "";
        int posicaoAbs = textoCompleto.lastIndexOf("Abstract");
        String textoBusca = textoCompleto;
        if (posicaoAbs != -1) {
            textoBusca = textoCompleto.substring(0, textoCompleto.lastIndexOf("Abstract"));
        }
        int posicao = textoBusca.indexOf("Department");
        if (posicao == -1) {
            if (instituicoes.equals("")) {
                textoBusca = textoCompleto;
                posicao = textoBusca.indexOf("Department");
            }
        }
        while (posicao != -1) {
            while (true) {
                if ("\n".equals(textoBusca.substring(posicao, posicao + 1))) {
                    break;
                }
                posicao--;
            }
            posicao++;
            int posInicio = posicao;
            while (true) {
                if ("\n".equals(textoBusca.substring(posicao, posicao + 1))) {
                    break;
                }
                posicao++;
            }
            instituicoes = instituicoes + textoBusca.substring(posInicio, posicao) + "\n ";
            textoBusca = textoBusca.substring(posicao, textoBusca.length());
            posicao = textoBusca.indexOf("Department");
        }
        System.out.println("\n--- Listando as instituições dos autores ---");
        System.out.println(instituicoes);
    }

}
