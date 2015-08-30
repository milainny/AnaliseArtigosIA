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
import org.apache.pdfbox.pdmodel.PDPage;
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
        String caminhoEntrada = "arquivos/12.pdf";
        String identificador = caminhoEntrada.substring(caminhoEntrada.lastIndexOf("/") + 1, caminhoEntrada.lastIndexOf("."));
        String caminhoArquivoStopWords = "stop_words/lista_stop_words.txt";
        String caminhoSaidaSemStopWords = "saidas/" + identificador + ".txt";
        String textoCompleto = extraiTextoDoPDF(caminhoEntrada);
        String textoSemStopWords = extraiStopWords(textoCompleto, caminhoArquivoStopWords);
        gravaTextoSemStopWords(caminhoSaidaSemStopWords, textoSemStopWords);
        String referencias = extraiReferencias(textoSemStopWords);
        exibeTermosMaisCitados(textoSemStopWords);
        identificaInstituicoes(caminhoEntrada);
        identificaObjetivo(caminhoEntrada, caminhoArquivoStopWords);
    }

    public static String extraiTextoDoPDF(String caminho) throws IOException {
        PDDocument pdfDocument = null;
        pdfDocument = PDDocument.load(caminho);
        PDFTextStripper stripper = new PDFTextStripper();
        String texto = stripper.getText(pdfDocument);
//        PrintStream saidaPadrao = System.out;
//        PrintStream saida = new PrintStream(new FileOutputStream(new File("saidas/texto" + 19 + ".txt")));
//        System.setOut(saida);
//        System.out.println(texto);
//        System.setOut(saidaPadrao);
        pdfDocument.close();
        return texto;
    }

    public static String extraiStopWords(String textoCompleto, String caminhoArquivoStopWords) throws FileNotFoundException {
        ArrayList<String> listaStopWords = preencheLista(caminhoArquivoStopWords);
        String textoCompletoMin = textoCompleto.toLowerCase();
        textoCompletoMin = textoCompletoMin.replace("\r", "");
        textoCompletoMin = textoCompletoMin.replace("\"", "");
        textoCompletoMin = textoCompletoMin.replace("\n", " \n ");
        textoCompletoMin = textoCompletoMin.replace(".", " . ");
        textoCompletoMin = textoCompletoMin.replace("?", " ? ");
        textoCompletoMin = textoCompletoMin.replace("!", " ! ");
        textoCompletoMin = textoCompletoMin.replace(":", " : ");
        textoCompletoMin = textoCompletoMin.replace(",", " , ");
        textoCompletoMin = textoCompletoMin.replace(";", " ; ");
        textoCompletoMin = textoCompletoMin.replace("   ", " ");
        textoCompletoMin = textoCompletoMin.replace("  ", " ");
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
            if (t.getTermo().length() > 2) {
                System.out.println("Termo: " + t.getTermo() + " - Frequencia: " + t.getFrequencia());
                i++;
            }
        }
    }

    private static void identificaInstituicoes(String caminho) throws IOException {
        String textoPrimeirasPaginas = leitorDePaginas(caminho, 2);

//        PrintStream saidaPadrao = System.out;
//        PrintStream saida = new PrintStream(new FileOutputStream(new File("saidas/texto2pg" + 12 + ".txt")));
//        System.setOut(saida);
//        System.out.println(textoPrimeirasPaginas);
//        System.setOut(saidaPadrao);
        String caminhoArquivoInstuicoes = "stop_words/instituicoes.txt";
        String instituicoes = "";
        ArrayList<String> listaInstiuicoes = preencheLista(caminhoArquivoInstuicoes);
        for (String inst : listaInstiuicoes) {
            String textoBusca = textoPrimeirasPaginas;
            int posicaoAbs = textoPrimeirasPaginas.lastIndexOf("Abstract");
            if (posicaoAbs != -1) {
                textoBusca = textoPrimeirasPaginas.substring(0, textoPrimeirasPaginas.lastIndexOf("Abstract"));
            }
            int posicao = textoBusca.indexOf(inst);
            if (posicao == -1) {
                if (instituicoes.equals("")) {
                    textoBusca = textoPrimeirasPaginas;
                    posicao = textoBusca.indexOf(inst);
                }
            }
            while (posicao != -1) {
                posicao = buscaInicioParagrafo(textoBusca, posicao, "\n");
                int posInicio = posicao;
                posicao = buscaFimParagrafo(textoBusca, posicao, "\n");
                instituicoes = instituicoes + textoBusca.substring(posInicio, posicao) + "\n ";
                textoBusca = textoBusca.substring(posicao, textoBusca.length());
                posicao = textoBusca.indexOf(inst);
            }
        }
        System.out.println("\n--- Listando as instituições dos autores ---");
        System.out.println(instituicoes);
    }

    private static int buscaFimParagrafo(String textoBusca, int posicao, String operador) {
        while (true) {
            if (operador.equals(textoBusca.substring(posicao, posicao + 1))) {
                break;
            }
            posicao++;
        }
        return posicao;
    }

    private static int buscaInicioParagrafo(String textoBusca, int posicao, String operador) {
        while (true) {
            if (operador.equals(textoBusca.substring(posicao, posicao + 1))) {
                break;
            }
            posicao--;
        }
        posicao++;
        return posicao;
    }

    private static ArrayList<String> preencheLista(String caminhoArquivo) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(caminhoArquivo));
        ArrayList<String> lista = new ArrayList();
        while (scanner.hasNext()) {
            lista.add(scanner.next());
        }
        return lista;
    }

    private static ArrayList<String> preencheListaEDivide(String caminhoArquivo) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(caminhoArquivo));
        String arquivo = "";
        ArrayList<String> lista = new ArrayList();
        while (scanner.hasNext()) {
            arquivo += scanner.next() + " ";
        }
        String[] resultado = arquivo.split(" - ");
        for (String s : resultado) {
            lista.add(s.trim());
        }
        return lista;
    }

    private static String leitorDePaginas(String caminho, int quantidadePaginas) throws IOException {
        PDDocument pdfDocument = null;
        pdfDocument = PDDocument.load(caminho);
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setEndPage(quantidadePaginas);
        String textoPrimeirasPaginas = stripper.getText(pdfDocument);
        pdfDocument.close();
        return textoPrimeirasPaginas;
    }

    private static void identificaObjetivo(String caminho, String caminhoArquivoSemStopWords) throws IOException {
        String textoPrimeirasPaginas = leitorDePaginas(caminho, 5);
        String textoSemStopWords = extraiStopWords(textoPrimeirasPaginas, caminhoArquivoSemStopWords);
        textoPrimeirasPaginas = textoSemStopWords.toLowerCase();
        String caminhoArquivoObjetivo = "stop_words/objetivo.txt";
        ArrayList<String> listaObjetivo = preencheListaEDivide(caminhoArquivoObjetivo);
        ArrayList<String> naoReptir = new ArrayList<>();

        String resultado = "";
        for (String obj : listaObjetivo) {
            int posicao = textoSemStopWords.indexOf(obj);
            if (posicao != -1) {
                posicao = retornaPosicaoMenor(textoSemStopWords,posicao);
                int posInicio = posicao;
                posicao = buscaFimParagrafo(textoSemStopWords, posicao, ".");
                resultado = textoSemStopWords.substring(posInicio, posicao);
                if (!naoReptir.contains(resultado)) {
                    naoReptir.add(resultado);
                }
            }
        }
        System.out.println("\n--- Listando o objetivo do artigo ---");
        for (String s : naoReptir) {
            System.out.println(s);
            System.out.println("");
        }

    }

    private static String extraiReferencias(String textoSemStopWords) {
        return null;
    }

    private static int retornaPosicaoMenor(String textoSemStopWords, int posicao) {
        int pos1, pos2;
         pos1 = buscaInicioParagrafo(textoSemStopWords, posicao, ".");
         pos2 = buscaInicioParagrafo(textoSemStopWords, posicao, "\n");
         if (pos2 > pos1){
             return pos2;
         }
         return pos1;
         
    }

}
