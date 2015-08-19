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

/**
 *
 * @author Lailla
 */
public class AnaliseArtigosIA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        String caminhoEntrada = "arquivos/1.pdf";
        String identificador = caminhoEntrada.substring(caminhoEntrada.lastIndexOf("/") + 1, caminhoEntrada.lastIndexOf("."));
        String caminhoArquivoStopWords = "stop_words/lista_stop_words.txt";
        String caminhoSaidaSemStopWords = "saidas/" + identificador + ".txt";
        String textoCompleto = extraiTextoDoPDF(caminhoEntrada);
        String textoSemStopWords = extraiStopWords(textoCompleto, caminhoArquivoStopWords);
        gravaTextoSemStopWords(caminhoSaidaSemStopWords, textoSemStopWords);
        exibeTermosMaisCitados(textoSemStopWords);
    }

    public static String extraiTextoDoPDF(String caminho) throws IOException {
        PDDocument pdfDocument = null;
        pdfDocument = PDDocument.load(caminho);
        PDFTextStripper stripper = new PDFTextStripper();
        String texto = stripper.getText(pdfDocument);
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
        String textoSemReferencias = textoSemStopWords.substring(0, textoSemStopWords.lastIndexOf(" references "));
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

}
